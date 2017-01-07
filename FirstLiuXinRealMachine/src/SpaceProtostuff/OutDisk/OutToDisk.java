package SpaceProtostuff.OutDisk;

import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import SpaceProtostuff.NameAndValueOfOneColumn;
import SpaceProtostuff.OneLineInDBTable;

public class OutToDisk {
	private final byte[] byteLock = new byte[0];
	private final ConcurrentHashMap<Class<?>, Schema<?>> CachedSchema = new ConcurrentHashMap<Class<?>, Schema<?>>();
	private boolean hasCreateWriter_State = false;
	private boolean hasClose_State = false;
	private volatile String dirNameBetweenRootAndFile = null;
	private volatile OutputStream outStream = null;
	private final OutToDiskConfig outToDiskConfig;
	private final AtomicLong howManyHasWrite = new AtomicLong(0);
	private final AtomicInteger fileNumWhenCreateNewFile = new AtomicInteger(0);
	private final AtomicInteger dirNumWhenCreateNewDir = new AtomicInteger(0);
	private final String rootPathOfOneTask;

	private OutToDisk(final String rootPathOfOneTask, final OutToDiskConfig outToDiskConfig) {
		this.rootPathOfOneTask = rootPathOfOneTask;
		this.outToDiskConfig = outToDiskConfig;
	}

	public static OutToDisk buildOutToDisk(final String rootPathOfOneTask, final OutToDiskConfig outToDiskConfig) {
		OutDiskCheck.checkRootPath(rootPathOfOneTask);
		OutToDisk outToDisk = new OutToDisk(rootPathOfOneTask, outToDiskConfig);
		return outToDisk;
	}

	public void createOutToDiskWriter() {
		synchronized (byteLock) {
			if (isStateError()) {
				return;
			}
			createWriterInner();
		}
	}

	private boolean isStateError() {
		boolean state = false;
		if (hasClose_State == true) {
			System.out.println("outdiskwriter has closed");
			state = true;
		}
		if (hasCreateWriter_State == true) {
			System.out.println("outdiskwriter has opened");
			state = true;
		}
		return state;
	}

	private void createWriterInner() {
		final int partSize = outToDiskConfig.getSizeOfOneFile();
		final boolean isOpenGZIP = outToDiskConfig.isOpenGZip();
		if (partSize < 0 || partSize == 0) {
			throw new IllegalArgumentException("partSize must not be < =0,must >0.");
		}
		final File file = makeFile();
		makeOutStream(file, isOpenGZIP);
	}

	private File makeFile() {
		OutDiskCheck.checkFileIsADir(rootPathOfOneTask);
		final String rootPath = OutToDiskFilesMake.makeRightRootPath(rootPathOfOneTask);
		makeDirWhichStoreByteFile();
		final String dirPath = rootPath + dirNameBetweenRootAndFile;
		OutToDiskFilesMake.makeDirWhichStoreByteFile0(dirPath);
		final String fileName = OutToDiskFilesMake.makeRightFileName(fileNumWhenCreateNewFile.getAndIncrement());
		final String fullPath = rootPath + dirNameBetweenRootAndFile + fileName;
		File file = OutToDiskFilesMake.realMakeFile(fullPath);
		return file;
	}

	private void makeDirWhichStoreByteFile() {
		if (null == dirNameBetweenRootAndFile) {
			final String dirName = OutToDiskFilesMake.makeRightDirBetweenRootAndFileName(dirNumWhenCreateNewDir
					.getAndIncrement());
			dirNameBetweenRootAndFile = dirName;
		} else {
			if (fileNumWhenCreateNewFile.get() >= outToDiskConfig.getNumFileInOneDir()) {
				final String dirName = OutToDiskFilesMake.makeRightDirBetweenRootAndFileName(dirNumWhenCreateNewDir
						.getAndIncrement());
				dirNameBetweenRootAndFile = dirName;
				fileNumWhenCreateNewFile.set(0);
			}
		}
	}

	private OutputStream makeOutStream(final File file, final boolean isOpenZip) {
		if (null != outStream) {
			return outStream;
		} else {
			if (isOpenZip) {
				outStream = OutToDiskFilesMake.makeGZipStream(file);
				return outStream;
			} else {
				outStream = OutToDiskFilesMake.makeNormalStream(file);
				return outStream;
			}
		}
	}

	private void partFile() {
		if (null != outStream) {
			try {
				outStream.flush();
				outStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			outStream = null;
		}
		File file = makeFile();
		boolean isOpenZip = outToDiskConfig.isOpenGZip();
		outStream = makeOutStream(file, isOpenZip);
	}

	private boolean closeAll() {
		boolean isOK = false;
		if (null != outStream) {
			try {
				outStream.flush();
				outStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			outStream = null;
			howManyHasWrite.set(0);
			CachedSchema.clear();
			isOK = true;
		}
		return isOK;
	}

	@SuppressWarnings("unchecked")
	private <T> int serializePrivate(T obj) {
		int size = 0;
		Class<T> cls = (Class<T>) obj.getClass();
		byte[] bytes = new byte[LinkedBuffer.DEFAULT_BUFFER_SIZE];
		LinkedBuffer buffer = LinkedBuffer.use(bytes);
		Schema<T> schema = getSchema(cls);
		try {
			size = realSerialize(obj, schema, buffer);
			canChangeOutStream(size);
		} finally {
			cleanAfterSeri(buffer, bytes, schema);
		}
		return size;
	}

	private <T> int realSerialize(final T obj, final Schema<T> schema, final LinkedBuffer buffer) {
		int size = 0;
		try {
			size = ProtostuffIOUtil.writeDelimitedTo(outStream, obj, schema, buffer);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return size;
	}

	private void canChangeOutStream(final int hsaWriteSize) {
		howManyHasWrite.set(howManyHasWrite.get() + hsaWriteSize);
		final int fileSizeInConfig = outToDiskConfig.getSizeOfOneFile() * OutToDiskStructure.oneMB;
		if ((howManyHasWrite.get()) >= (fileSizeInConfig)) {
			howManyHasWrite.set(0);
			partFile();
		}
	}

	private <T> void cleanAfterSeri(LinkedBuffer buffer, byte[] bytes, Schema<T> schema) {
		if (buffer != null) {
			buffer.clear();
			buffer = null;
		}
		if (null != bytes) {
			bytes = null;
		}
		if (null != schema) {
			schema = null;
		}
	}

	@SuppressWarnings("unchecked")
	private <T> Schema<T> getSchema(Class<T> cls) {
		Schema<T> schema = (Schema<T>) CachedSchema.get(cls);
		if (schema == null) {
			schema = RuntimeSchema.createFrom(cls);
			if (schema != null) {
				CachedSchema.putIfAbsent(cls, schema);
			}
		}
		return schema;
	}

	public <T> long serializePublic(T obj) {
		return serializePrivate(obj);
	}

	public boolean close() {
		synchronized (byteLock) {
			if (hasClose_State == true) {
				System.out.println("has closeed");
				return false;
			} else {
				return closeAll();
			}
		}
	}

	public static void main(String args[]) {
		String path = "/home/liuxin/work/liuxinAllTest/taskname/whichline/threadname";
		String nameString = "name";
		String valueString = "value";
		int howManyColumn = 20;
		OneLineInDBTable oneLineInDBTable = OneLineInDBTable.init(howManyColumn);
		NameAndValueOfOneColumn nameAndValueOFOneColumn = null;
		ArrayList<OneLineInDBTable> list = new ArrayList<OneLineInDBTable>(1000000);
		for (int k = 0; k < 1000000; k++) {
			for (int i = 0; i < howManyColumn; i++) {
				nameAndValueOFOneColumn = NameAndValueOfOneColumn.init(nameString + i, valueString + i);
				oneLineInDBTable.putColumnInMap(nameAndValueOFOneColumn);
			}
			oneLineInDBTable.setLineNum(k);
			list.add(oneLineInDBTable);
		}
		OutToDiskConfig outToDiskConfig = OutToDiskConfig.createOutToDiskConfig(false, 1, 3);
		OutToDisk serializationOutToDisk = OutToDisk.buildOutToDisk(path, outToDiskConfig);
		serializationOutToDisk.createOutToDiskWriter();
		for (int j = 0; j < list.size(); j++) {
			serializationOutToDisk.serializePublic(list.get(j));
		}
		serializationOutToDisk.close();
	}
}

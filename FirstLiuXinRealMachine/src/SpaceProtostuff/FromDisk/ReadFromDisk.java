package SpaceProtostuff.FromDisk;

import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.GZIPInputStream;

import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

import SpaceProtostuff.NameAndValueOfOneColumn;
import SpaceProtostuff.OneLineInDBTable;

public class ReadFromDisk {
	private final String path;
	private InputStream inutStream;
	private final Objenesis objenesis = new ObjenesisStd(true);
	private final AtomicLong howManyLineHasRead = new AtomicLong(0);
	private final ConcurrentLinkedQueue<File> fileListQueue = new ConcurrentLinkedQueue<>();
	private final AtomicBoolean isOpenZip = new AtomicBoolean(true);
	private static final ConcurrentHashMap<Class<?>, Schema<?>> CACHEDSCHEMA=new ConcurrentHashMap<Class<?>, Schema<?>>();

	private ReadFromDisk(final String path) {
		super();
		this.path = path;
	}

	@SuppressWarnings("unchecked")
    private  <T> Schema<T> getSchema(Class<T> cls) {
        Schema<T> schema = (Schema<T>) CACHEDSCHEMA.get(cls);
        if (schema == null) {
            schema = RuntimeSchema.createFrom(cls);
            if (schema != null) {
            	CACHEDSCHEMA.putIfAbsent(cls, schema);
            }
        }
        return schema;
    }
	
	
	
	public static ReadFromDisk init(final String path) {
		ReadFromDiskCheck.checkPath(path);
		ReadFromDisk readFromDisk = new ReadFromDisk(path);
		return readFromDisk;
	}

	

	private boolean createFileList() {
		boolean result = false;
		File file = new File(path);
		if (file.isFile()) {
			throw new IllegalArgumentException("path: " + path + " ,must not be a file.");
		}
		File[] fileList=file.listFiles(new FileFilter() {
			
			@Override
			public boolean accept(File pathname) {
				if (pathname.getName().endsWith(".lx")) {
	                return true;
	            }
				return false;
			}
		});
		//File[] fileList = file.listFiles();
		
		int i = 0;
		try {
			if (fileList == null) {
				throw new NullPointerException("fileList: " + fileList + " ,must not be null.");
			}
			i = fileList.length;
			if (i == 0) {
				throw new IllegalArgumentException("there is no file in this path:" + path + ".");
			}
			for (int j = 0; j < i; j++) {
				if (fileList[j].isFile()&&fileList[j].getName().endsWith(".lx")) {
					fileListQueue.add(fileList[j]);
				} else {
					// 递归调用createFileList()
				}
			}
			result = true;
		} finally {
			if (null != fileList) {
				for (int k = 0; k < i; k++) {
					fileList[k] = null;
				}
				fileList = null;
			}
		}
		return result;
	}

	public boolean prepareForBegin() {
		boolean isDone = false;
		if (createFileList()) {
			File file = fileListQueue.poll();
			if (file == null) {
				throw new NullPointerException("file: " + file + " ,must not be null get from queue.");
			} else {
				this.inutStream=createInputStream(file);
				isDone = true;
				return isDone;
			}
		} else {
			isDone = false;
			return isDone;
		}
	}

	private InputStream createInputStream(final File file) {
		if (null != inutStream) {
			try {
				inutStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			inutStream = null;
		}
		if (isOpenZip.get()) {
			try {
				inutStream = new BufferedInputStream(new GZIPInputStream(new FileInputStream(file)));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return inutStream;
		} else {
			try {
				inutStream = new BufferedInputStream(new FileInputStream(file));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			return inutStream;
		}
	}

	private boolean changeInPutStream() {
		boolean isSuccess = false;
		File file = null;
		file = fileListQueue.poll();
		if (null == file) {
			isSuccess = false;
			return isSuccess;
		} else {
			this.inutStream=createInputStream(file);
			isSuccess = true;
			return isSuccess;
		}
	}

	private boolean closeAll() {
		boolean isClose = false;
		if (null != inutStream) {
			try {
				inutStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			inutStream = null;
			howManyLineHasRead.set(0);
			fileListQueue.clear();
			CACHEDSCHEMA.clear();
			isClose = true;
			return isClose;
		} else {
			inutStream = null;
			howManyLineHasRead.set(0);
			fileListQueue.clear();
			CACHEDSCHEMA.clear();
			isClose = true;
			return isClose;
		}
	}

	private <T> T deserializePrivate(Class<T> cls) {
		T message = (T) objenesis.newInstance(cls);
		Schema<T> schema = getSchema(cls);
		long byteSizeReadFromInputStream = 0;
		try {
			byteSizeReadFromInputStream = ProtostuffIOUtil.mergeDelimitedFrom(inutStream, message, schema);
			} catch (IOException e) {
				if("mergeDelimitedFrom".equals(e.getMessage())){
					//LockSupport.parkNanos(TimeUnit.NANOSECONDS.convert(3, TimeUnit.SECONDS));
				}else {
					e.printStackTrace();
				}
		}
			if(byteSizeReadFromInputStream<=0){
				if(changeInPutStream()){
					message=deserializePrivate(cls);
				}else {
					message=null;
				}
			}
		return message;
	}

	public void close() {
		closeAll();
	}

	public <T> T deserialize(Class<T> obj) {
		return deserializePrivate(obj);
	}

	public static void main(String args[]) {
		String path = "/home/liuxin/worker/liuxinAllTest/testforprotostuff";
		ReadFromDisk serializationInFromDisk = ReadFromDisk.init(path);
		serializationInFromDisk.prepareForBegin();

		ArrayList<OneLineInDBTable> list = new ArrayList<>();
		OneLineInDBTable oneLineInDBTable = null;
		while (true) {
			oneLineInDBTable = serializationInFromDisk.deserialize(OneLineInDBTable.class);
			if (null == oneLineInDBTable) {
				break;
			} else {
				list.add(oneLineInDBTable);
			}
		}
		System.out.println("list.size():"+list.size());

		try {
			TimeUnit.SECONDS.sleep(60);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		NameAndValueOfOneColumn nameAndValueOFOneColumn = null;

		ConcurrentHashMap<String, NameAndValueOfOneColumn> map = new ConcurrentHashMap<String, NameAndValueOfOneColumn>();

		map = list.get(39).getLineConcurrentHashMap();

		int size = map.size();
		for (int i = 0; i < size; i++) {
			nameAndValueOFOneColumn = map.get("name" + i);
			System.out.println("name:" + nameAndValueOFOneColumn.getName() + ",value:"
					+ nameAndValueOFOneColumn.getValue());

		}
		map.clear();
		list.clear();
		serializationInFromDisk.close();
	}
}

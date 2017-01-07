package SpaceProtostuff.OutDisk;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

public final class OutToDiskFilesMake {

	private OutToDiskFilesMake() {

	}

	protected static String makeRightRootPath(final String rootPathOfOneTask) {
		String rootPath = "";
		if (rootPathOfOneTask.endsWith(File.separator)) {
			rootPath = rootPathOfOneTask;
		} else if (!rootPathOfOneTask.endsWith(File.separator)) {
			rootPath = rootPathOfOneTask + File.separator;
		}
		return rootPath;
	}

	protected static String makeRightDirBetweenRootAndFileName(final int dirNumWhenCreateNewDir) {
		final String dirName = OutToDiskStructure.dirNameWhenMakeNewOne + dirNumWhenCreateNewDir + File.separator;
		return dirName;
	}

	protected static void makeDirWhichStoreByteFile0(final String dirPath) {
		File file = new File(dirPath);
		file.setWritable(true);
		if (file.exists()) {
			return;
		} else {
			if (file.isFile()) {
				throw new IllegalStateException();
			}
			file.mkdirs();
		}
	}

	protected static String makeRightFileName(final int fileNumWhenCreateNewFile) {
		final String fileName = OutToDiskStructure.fileName + fileNumWhenCreateNewFile + OutToDiskStructure.fileTail;
		return fileName;
	}

	protected static File realMakeFile(final String fullPath) {
		File file = new File(fullPath);
		file.setWritable(true);
		if (file.exists()) {
			file.delete();
		}
		try {
			file.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return file;
	}

	protected static OutputStream makeNormalStream(final File file) {
		return makeStream(file, false);
	}

	protected static OutputStream makeGZipStream(final File file) {
		return makeStream(file, true);
	}

	private static OutputStream makeStream(final File file, final boolean isGZIP) {
		OutputStream outputStream = null;
		try {
			if (isGZIP) {
				outputStream = new BufferedOutputStream(new GZIPOutputStream(new FileOutputStream(file)));
			} else {
				outputStream = new BufferedOutputStream(new FileOutputStream(file));
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return outputStream;
	}

}

package SpaceProtostuff.OutDisk;

import java.io.File;

public final class OutDiskCheck {

	private OutDiskCheck() {

	}

	protected static void checkRootPath(final String path) {
		if (null == path) {
			throw new NullPointerException("path must not be null.");
		}
		final String pathInnerString = path.trim();
		if (pathInnerString.length() == 0 || "".equals(pathInnerString) || " ".equals(pathInnerString)) {
			throw new IllegalArgumentException("path must not be empty.");
		}
		File file = new File(path);
		if (!file.exists()) {
			throw new IllegalStateException("file or dir can not be found ");
		}
		if (file.isFile()) {
			throw new IllegalArgumentException("file in this path " + path + " must not be a file.");
		}
	}

	protected static void checkFileIsADir(final String rootPathOfOneTask) {
		File file = new File(rootPathOfOneTask);
		if (!file.isDirectory()) {
			throw new IllegalArgumentException("the path: " + rootPathOfOneTask + ",  must be a directory.");
		}
	}

}

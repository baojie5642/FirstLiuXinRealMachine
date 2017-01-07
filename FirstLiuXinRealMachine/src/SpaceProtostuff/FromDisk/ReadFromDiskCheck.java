package SpaceProtostuff.FromDisk;

import java.io.File;

public class ReadFromDiskCheck {

	private ReadFromDiskCheck(){
		
	}

	protected static void checkPath(final String path) {
		if (null == path) {
			throw new NullPointerException("path: " + path + " ,must not be null.");
		}
		String innerString = path.trim();
		if (innerString.length() == 0 || "".equals(innerString) || " ".equals(innerString)) {
			throw new IllegalArgumentException("path: " + path + " ,must not be empty.");
		}
		File file = new File(path);
		if (!file.exists()) {
			throw new IllegalArgumentException("path: " + path + ", not exist.");
		}
		if (file.isFile()) {
			throw new IllegalArgumentException("path: " + path + " ,must not be a file.");
		}
	}
	
	protected static void checkFile(){
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}

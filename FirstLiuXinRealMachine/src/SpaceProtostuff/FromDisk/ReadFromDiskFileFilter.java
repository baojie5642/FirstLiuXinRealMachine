package SpaceProtostuff.FromDisk;

import java.io.File;
import java.io.FileFilter;

public class ReadFromDiskFileFilter implements FileFilter {
	private ReadFromDiskFileFilter() {
	}

	protected static final ReadFromDiskFileFilter Defult_File_Filter = new ReadFromDiskFileFilter();

	@Override
	public boolean accept(final File pathname) {
		if (pathname.isFile()) {
			String fileName = pathname.getName();
			if (fileName.endsWith(ReadFromDiskStructure.FileTail)) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

}

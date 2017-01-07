package SpaceProtostuff.FromDisk;

import java.io.File;
import java.io.FileFilter;

public class ReadFromDiskDirFilter implements FileFilter {
	private ReadFromDiskDirFilter() {
	}

	protected static final ReadFromDiskDirFilter Defult_Dir_Filter = new ReadFromDiskDirFilter();

	@Override
	public boolean accept(final File pathname) {
		if (pathname.isDirectory()) {
			String fileName = pathname.getName();
			if (!fileName.endsWith(ReadFromDiskStructure.FileTail)) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}
}

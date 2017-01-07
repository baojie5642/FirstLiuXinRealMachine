package SpaceProtostuff.OutDisk;

public final class OutToDiskStructure {
	private OutToDiskStructure() {
	}

	protected static final String fileName = "lx-file";
	protected static final String fileTail = ".lx";
	protected static final String dirNameWhenMakeNewOne = "fileDirCreatedByLX";
	protected static final int oneKB = 1024;
	protected static final int oneMB = oneKB * oneKB;
	protected static final int defultFileNum = 1024;
	protected static final int defultFileSize = 1024;
	protected static final boolean isOpenGZip = true;
}

package SpaceProtostuff.OutDisk;


public class OutToDiskConfig {
	private final boolean isOpenGZip;
	private final int sizeOfOneFile;
	private final int numFileInOneDir;

	public static final OutToDiskConfig Defult_OutToDiskConfig = new OutToDiskConfig() {
		@Override
		public boolean isOpenGZip() {
			return OutToDiskStructure.isOpenGZip;
		}

		@Override
		public int getSizeOfOneFile() {
			return OutToDiskStructure.defultFileSize;
		}

		@Override
		public int getNumFileInOneDir() {
			return OutToDiskStructure.defultFileNum;
		}
	};

	private OutToDiskConfig() {
		this.isOpenGZip = OutToDiskStructure.isOpenGZip;
		this.sizeOfOneFile = OutToDiskStructure.defultFileSize;
		this.numFileInOneDir = OutToDiskStructure.defultFileNum;
	}

	private OutToDiskConfig(final boolean isOpenGZip) {
		this.isOpenGZip = isOpenGZip;
		this.sizeOfOneFile = OutToDiskStructure.defultFileSize;
		this.numFileInOneDir = OutToDiskStructure.defultFileNum;
	}

	private OutToDiskConfig(final int sizeOfOneFile, final int numFileInOneDir) {
		this.isOpenGZip = OutToDiskStructure.isOpenGZip;
		this.sizeOfOneFile = sizeOfOneFile;
		this.numFileInOneDir = numFileInOneDir;
	}

	private OutToDiskConfig(final boolean isOpenGZip, final int sizeOfOneFile, final int numFileInOneDir) {
		this.isOpenGZip = isOpenGZip;
		this.sizeOfOneFile = sizeOfOneFile;
		this.numFileInOneDir = numFileInOneDir;
	}

	public static OutToDiskConfig createOutToDiskConfig() {
		return Defult_OutToDiskConfig;
	}

	public static OutToDiskConfig createOutToDiskConfig(final boolean isOpenGZip) {
		OutToDiskConfig outToDiskConfig = new OutToDiskConfig(isOpenGZip);
		return outToDiskConfig;
	}

	public static OutToDiskConfig createOutToDiskConfig(final int sizeOfOneFile, final int numFileInOneDir) {
		OutToDiskConfig outToDiskConfig = new OutToDiskConfig(sizeOfOneFile, numFileInOneDir);
		return outToDiskConfig;
	}

	public static OutToDiskConfig createOutToDiskConfig(final boolean isOpenGZip, final int sizeOfOneFile,
			final int numFileInOneDir) {
		OutToDiskConfig outToDiskConfig = new OutToDiskConfig(isOpenGZip, sizeOfOneFile, numFileInOneDir);
		return outToDiskConfig;
	}

	public boolean isOpenGZip() {
		return isOpenGZip;
	}

	public int getSizeOfOneFile() {
		return sizeOfOneFile;
	}

	public int getNumFileInOneDir() {
		return numFileInOneDir;
	}

}

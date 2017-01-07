package StampOfTaskInformation;

import java.io.Serializable;

public class RealSourceForTask implements Serializable {
	private static final long serialVersionUID = 2016011307498303285L;
	private final String extratcTableNameString;
	private final String exportTableNameString;

	private RealSourceForTask(final String extString, final String expString) {
		super();
		this.extratcTableNameString = extString;
		this.exportTableNameString = expString;
	}

	public static RealSourceForTask init(final String extString, final String expString) {
		RealSourceForTask realSourceForTask = new RealSourceForTask(extString, expString);
		return realSourceForTask;
	}

	public String getExtratcTableNameString() {
		return extratcTableNameString;
	}

	public String getExportTableNameString() {
		return exportTableNameString;
	}
}

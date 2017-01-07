package TaskFactory;

import StampOfTaskInformation.LineStamp;
import TaskRunner.ExportTaskRunner;

public class ExportTaskFactory {
	public ExportTaskFactory() {
		super();
	}

	public ExportTaskRunner buildExtractRunner(final LineStamp lineStamp, final int whichThreadInLineStamp) {
		ExportTaskRunner exportTaskRunner = ExportTaskRunner.init(lineStamp, whichThreadInLineStamp);
		return exportTaskRunner;
	}
}

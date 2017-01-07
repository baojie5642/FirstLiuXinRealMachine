package TaskFactory;

import StampOfTaskInformation.LineStamp;
import TaskRunner.TransferTaskRunner;

public class TransferTaskFactory {
	public TransferTaskFactory() {
		super();
	}

	public TransferTaskRunner buildExtractRunner(final LineStamp lineStamp) {
		TransferTaskRunner transferTaskRunner = TransferTaskRunner.init(lineStamp);
		return transferTaskRunner;
	}
}

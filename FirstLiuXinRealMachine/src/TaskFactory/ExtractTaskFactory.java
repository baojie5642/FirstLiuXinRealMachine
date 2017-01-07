package TaskFactory;

import StampOfTaskInformation.LineStamp;
import TaskRunner.ExtractTaskRunner;

public class ExtractTaskFactory {

	public ExtractTaskFactory() {
		super();
	}

	// 这个 写的 很简单 具体 还要详细设计 这里只是一个例子
	public ExtractTaskRunner buildExtractRunner(final LineStamp lineStamp, final int whichThreadInLineStamp) {
		ExtractTaskRunner extractTaskRunner = ExtractTaskRunner.init(lineStamp, whichThreadInLineStamp);
		return extractTaskRunner;
	}
}

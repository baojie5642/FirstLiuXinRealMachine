package QueueOfAllTask;

public class StaticQueueForAutoMachine {

	public static final TaskStampQueue TASK_STAMP_QUEUE = TaskStampQueue
			.initTaskStampQueue();

	public static final LineStampQueue EXT_QUEUE_WHICH_STORE_LINE_STAMP = LineStampQueue
			.initLineStampQueue();

	public static final LineStampQueue TRANS_QUEUE_FROM_EXT = LineStampQueue.initLineStampQueue();

	public static final LineStampQueue EXP_QUEUE_FROM_TRANS = LineStampQueue.initLineStampQueue();

}

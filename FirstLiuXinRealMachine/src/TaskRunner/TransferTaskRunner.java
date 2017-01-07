package TaskRunner;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import QueueOfAllTask.StaticQueueForAutoMachine;
import StampOfTaskInformation.LineStamp;

public class TransferTaskRunner implements Runnable {
	private final LineStamp stampOfAllTask;

	private TransferTaskRunner(LineStamp stampOfAllTask) {
		super();
		this.stampOfAllTask = stampOfAllTask;
	}

	public static TransferTaskRunner init(final LineStamp stampOfAllTask) {
		TransferTaskRunner transferTaskRunner = new TransferTaskRunner(stampOfAllTask);
		return transferTaskRunner;
	}

	public void run() {
		String taskNameString = null;
		try {
			taskNameString = stampOfAllTask.getTaskName();
			System.out.println("这是任务 " + taskNameString + " 的传输流程，需要30秒钟传输，结束后放入输出队列");
			LockSupport.parkNanos(TimeUnit.NANOSECONDS.convert(30, TimeUnit.SECONDS));
			System.out.println("传输结束，任务" + taskNameString + "放入输出队列");
			StaticQueueForAutoMachine.EXP_QUEUE_FROM_TRANS.addLineStamp(stampOfAllTask);
			System.out.println("任务" + taskNameString + "放入transfer-->export工厂队列成功,传输结束");
		} finally {
			if (null != taskNameString) {
				taskNameString = null;
			}
		}
	}
}

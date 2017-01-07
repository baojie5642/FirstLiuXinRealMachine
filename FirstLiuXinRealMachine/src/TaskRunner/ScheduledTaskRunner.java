package TaskRunner;

import QueueOfAllTask.StaticQueueForAutoMachine;
import StampOfTaskInformation.TaskStamp;

/*
 * thread-safe
 */

public class ScheduledTaskRunner implements Runnable {

	private final TaskStamp stampOfAllTask;

	private ScheduledTaskRunner(final TaskStamp stampOfAllTask) {
		super();
		this.stampOfAllTask = stampOfAllTask;
	}

	public static ScheduledTaskRunner init(final TaskStamp stampOfAllTask) {
		ScheduledTaskRunner scheduledTaskRunner = new ScheduledTaskRunner(stampOfAllTask);
		return scheduledTaskRunner;
	}

	public void run() {
		String taskNameString = null;
		int lineNumInTask = 0;
		try {
			taskNameString = stampOfAllTask.getTaskName();
			lineNumInTask = stampOfAllTask.getHowManyLineInTask();
			stampOfAllTask.acquireAllSem();
			StaticQueueForAutoMachine.TASK_STAMP_QUEUE.addTaskStamp(stampOfAllTask);
			for (int i = 0; i < lineNumInTask; i++) {
				stampOfAllTask.acquireOneSem();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// while ((lineNumInTask !=
			// stampOfAllTask.getSemaphore().availablePermits())) {
			// stampOfAllTask.releaseOneSem();
			// }
			releaseAllSemWhenNotSame();
			if (null != taskNameString) {
				taskNameString = null;
			}
		}
	}

	// 此方法其实可以去掉 具体再看
	private void releaseAllSemWhenNotSame() {
		while ((stampOfAllTask.getHowManyLineInTask() != stampOfAllTask.getSemaphore().availablePermits())) {
			stampOfAllTask.releaseOneSem();
		}
	}
}

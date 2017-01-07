package QueueOfAllTask;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import QueueOfAllTask.Impl.ImplOfTaskStampQueue;
import StampOfTaskInformation.TaskStamp;

public class TaskStampQueue extends ConcurrentLinkedQueue<TaskStamp> implements ImplOfTaskStampQueue {
	private static final long serialVersionUID = 2016032512345678910L;
	private static final Log LOG = LogFactory.getLog(TaskStampQueue.class);
	private final Semaphore semaphore = new Semaphore(1);

	public static TaskStampQueue initTaskStampQueue() {
		TaskStampQueue queueForAllTaskStyle = new TaskStampQueue();
		return queueForAllTaskStyle;
	}

	private TaskStampQueue() {
		super();
		acquireOneSemaphore(semaphore);
	}

	private void acquireOneSemaphore(final Semaphore semaphore) {
		try {
			semaphore.acquire(1);
		} catch (InterruptedException e) {
			e.printStackTrace();
			LOG.info("acquire one semaphore unsuccess when initQueue");
		} finally {
		}
	}

	@Override
	public Semaphore getSemaphoreForInitMachine() {
		return getSemaphoreForInitMachinePrivate();
	}

	private Semaphore getSemaphoreForInitMachinePrivate() {
		return semaphore;
	}

	@Override
	public boolean addTaskStamp(final TaskStamp taskStamp) {
		super.add(taskStamp);
		if (0 == semaphore.availablePermits()) {
			semaphore.release(1);
		}
		return true;
	}

	@Override
	public TaskStamp pollTaskStamp() {
		TaskStamp taskStamp = super.poll();
		if (1 == semaphore.availablePermits()) {
			acquireOneSemaphore(semaphore);
		}
		return taskStamp;
	}
}

package StampOfTaskInformation;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/*
 * thread-safe
 */

public class TaskStamp {
	// 说明一个任务中有几条线
	private final int howManyLineInTask;
	// 这里仅仅是考虑了单种情况，没有混合的，如果实现混合的就是将下面的队列变为队列数组，然后在设置一个标记
	private final ConcurrentLinkedQueue<LineStamp> concurrentLinkedQueue = new ConcurrentLinkedQueue<>();
	private final ReentrantReadWriteLock reentrantReadWriteLock = new ReentrantReadWriteLock();

	private final byte[] lockObject = new byte[0];
	// 任务的类型其实实在这里设置的，流程任务，简单任务，复制任务等，但是这里没有用到，只在stampline中用到了
	private final String taskStyle = "liu cheng task";
	// 当任务中有多条线，其中一个任务异常，
	// 那么整个task也就异常了，但是重新处理时要如何处理呢？？？处理单条线……
	private volatile int taskState = 0;
	private final Semaphore semaphore;
	private final long taskID;
	private final String taskName;

	private final int waitTimeBeforeFirstScheduled;
	private final int scheduledTime;

	public static TaskStamp init(final int howManyLineInTask, final long taskID, final String taskName) {
		TaskStamp stampOfTheTask = new TaskStamp(howManyLineInTask, taskID, taskName);
		return stampOfTheTask;
	}

	private TaskStamp(final int howManyLineInTask, final long taskID, final String taskName) {
		super();
		this.howManyLineInTask = howManyLineInTask;
		this.taskID = taskID;
		this.taskName = taskName;
		this.semaphore = new Semaphore(this.howManyLineInTask);
		this.waitTimeBeforeFirstScheduled = waitTime();
		this.scheduledTime = scheduledTime();
	}

	private int waitTime() {
		int waitTime = 0;
		ThreadLocalRandom random = null;
		try {
			random = ThreadLocalRandom.current();
			waitTime = random.nextInt(600) + 1;
		} finally {
			if (null != random) {
				random = null;
			}
		}
		return waitTime;// waitTime
	}

	private int scheduledTime() {
		int scheduledTime = 0;
		ThreadLocalRandom random = null;
		try {
			random = ThreadLocalRandom.current();
			scheduledTime = random.nextInt(300) + 1;
		} finally {
			if (null != random) {
				random = null;
			}
		}
		return scheduledTime;// scheduledTime
	}

	public void setTaskState(final int state) {
		setTaskStatePriv(state);
	}

	private void setTaskStatePriv(final int state) {
		ReentrantReadWriteLock lock = this.reentrantReadWriteLock;
		try {
			lock.writeLock().lock();
			taskState = state;
		} finally {
			lock.writeLock().unlock();
		}
	}

	public int getTaskState() {
		return getTaskStatePriv();
	}

	private int getTaskStatePriv() {
		ReentrantReadWriteLock lock = this.reentrantReadWriteLock;
		try {
			lock.readLock().lock();
			return taskState;
		} finally {
			lock.readLock().unlock();
		}
	}

	public void acquireOneSem() {
		acquireOneSemPriv();
	}

	private void acquireOneSemPriv() {
		try {
			this.semaphore.acquire(1);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void releaseOneSem() {
		releaseOneSemPriv();
	}

	private void releaseOneSemPriv() {
		this.semaphore.release(1);
	}

	public void acquireAllSem() {
		acquireAllSemPriv();
	}

	private void acquireAllSemPriv() {
		try {
			this.semaphore.acquire(howManyLineInTask);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void releaseAllSem() {
		releaseAllSemPriv();
	}

	private void releaseAllSemPriv() {
		this.semaphore.release(howManyLineInTask);
	}

	public void createLineStampInTaskStamp(final String taskStyle, final RealSourceForTask realSourceForTask,
			final int whichLine) {
		createLineStampInTaskStampPriv(taskStyle, realSourceForTask, whichLine);
	}

	private void createLineStampInTaskStampPriv(final String taskStyle, final RealSourceForTask realSourceForTask,
			final int whichLine) {
		LineStamp stampOfOneLineInTheTask = null;
		String taskStyleString = null;
		RealSourceForTask realSourceForTask2 = null;
		try {
			taskStyleString = taskStyle;
			realSourceForTask2 = realSourceForTask;
			stampOfOneLineInTheTask = LineStamp.init(this.taskName, this.taskID, taskStyleString, realSourceForTask2,
					this.semaphore, whichLine);
			// 对象安全发布
			this.concurrentLinkedQueue.add(stampOfOneLineInTheTask);
		} finally {
			if (null != stampOfOneLineInTheTask) {
				stampOfOneLineInTheTask = null;
			}
			if (null != realSourceForTask2) {
				realSourceForTask2 = null;
			}
			if (null != taskStyleString) {
				taskStyleString = null;
			}
		}
	}

	// 从队列中去除已经初始化好的stampline
	public LineStamp getLineStampInTask() {
		return getLineStampInTaskPriv();
	}

	// 没有判NULL，可以使用另一种写法，防止出现调用null对象的方法,这个方法可以和下面重新放入的方法一起结合
	private LineStamp getLineStampInTaskPriv() {
		final LineStamp stampOfOneLineInTheTask = this.concurrentLinkedQueue.poll();
		return stampOfOneLineInTheTask;
	}

	// 取完初始化后，再放回去
	public void rePutLineStampIntoQueue(final LineStamp stampOfOneLineInTheTask) {
		rePutLineStampIntoQueuePriv(stampOfOneLineInTheTask);
	}

	private void rePutLineStampIntoQueuePriv(final LineStamp stampOfOneLineInTheTask) {
		this.concurrentLinkedQueue.add(stampOfOneLineInTheTask);
	}

	public LineStamp getLineStampThenRePut() {
		return getLineStampThenRePutPriv();
	}

	private LineStamp getLineStampThenRePutPriv() {
		synchronized (lockObject) {
			final LineStamp stampOfOneLineInTheTask = concurrentLinkedQueue.poll();
			concurrentLinkedQueue.add(stampOfOneLineInTheTask);
			return stampOfOneLineInTheTask;
		}
	}

	public int getHowManyLineInTask() {
		return howManyLineInTask;
	}

	public ConcurrentLinkedQueue<LineStamp> getConcurrentLinkedQueue() {
		return concurrentLinkedQueue;
	}

	public ReentrantReadWriteLock getReentrantReadWriteLock() {
		return reentrantReadWriteLock;
	}

	public String getTaskStyle() {
		return taskStyle;
	}

	public Semaphore getSemaphore() {
		return semaphore;
	}

	public long getTaskID() {
		return taskID;
	}

	public String getTaskName() {
		return taskName;
	}

	public int getWaitTimeBeforeFirstScheduled() {
		return waitTimeBeforeFirstScheduled;
	}

	public int getScheduledTime() {
		return scheduledTime;
	}

}

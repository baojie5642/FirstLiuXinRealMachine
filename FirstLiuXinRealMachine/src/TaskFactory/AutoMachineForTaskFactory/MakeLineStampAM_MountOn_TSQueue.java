package TaskFactory.AutoMachineForTaskFactory;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;

import QueueOfAllTask.StaticQueueForAutoMachine;
import StampOfTaskInformation.LineStamp;
import StampOfTaskInformation.TaskStamp;
import TaskFactory.MakeStampLineFactory;
import TaskFactory.AutoMachineImpl.AM_Impl;

/*
 * thread-safe
 */

public class MakeLineStampAM_MountOn_TSQueue implements Runnable, AM_Impl {

	private final ReentrantLock mainLock = new ReentrantLock();
	private final Condition condition;
	private final AtomicBoolean atomicBoolean = new AtomicBoolean(false);
	private final Semaphore semaphoreForInitMachine;
	private final Semaphore semaphoreForPauseAndStartMachine = new Semaphore(1);
	private final MakeStampLineFactory makeStampLineFactory = new MakeStampLineFactory();

	public static MakeLineStampAM_MountOn_TSQueue initAutoMachine(final Semaphore semaphore) {
		MakeLineStampAM_MountOn_TSQueue autoMachineBaseOnQueue = new MakeLineStampAM_MountOn_TSQueue(semaphore);
		return autoMachineBaseOnQueue;
	}

	private MakeLineStampAM_MountOn_TSQueue(final Semaphore semaphoreForInitMachine) {
		super();
		this.semaphoreForInitMachine = semaphoreForInitMachine;
		this.condition = mainLock.newCondition();
		arquireOnePauseAndStartSemaphore();
	}

	private void arquireOnePauseAndStartSemaphore() {
		try {
			semaphoreForPauseAndStartMachine.acquire(1);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/*
	 * @Override public void pauseMachine() { final ReentrantLock lock =
	 * this.mainLock; try { lock.lock(); boolean firstCheck = false; boolean
	 * secondCheck = false; boolean thirdCheck = false; atomicBoolean.set(true);
	 * firstCheck = StaticQueueForAutoMachine.TASK_STAMP_QUEUE.isEmpty();
	 * LockSupport.parkNanos(TimeUnit.NANOSECONDS.convert(1,
	 * TimeUnit.MILLISECONDS)); secondCheck =
	 * StaticQueueForAutoMachine.TASK_STAMP_QUEUE.isEmpty();
	 * LockSupport.parkNanos(TimeUnit.NANOSECONDS.convert(1,
	 * TimeUnit.MILLISECONDS)); thirdCheck =
	 * StaticQueueForAutoMachine.TASK_STAMP_QUEUE.isEmpty(); if (0 ==
	 * semaphoreForInitMachine.availablePermits() &&
	 * (semaphoreForInitMachine.hasQueuedThreads()) && (firstCheck == true) &&
	 * (secondCheck == true) && (thirdCheck == true)) {
	 * semaphoreForInitMachine.release(1); } } catch (Exception e) {
	 * e.printStackTrace(); } finally { lock.unlock(); } }
	 * 
	 * @Override public void startMachine() { final ReentrantLock lock =
	 * this.mainLock; try { lock.lock(); atomicBoolean.set(false);
	 * condition.signal(); } finally { lock.unlock(); } }
	 */
//下面这两个方法不是幂等的，要修改下
	@Override
	public void pauseMachine() {
		atomicBoolean.set(true);
		semaphoreForInitMachine.release(1);
	}

	@Override
	public void startMachine() {
		atomicBoolean.set(false);
		semaphoreForPauseAndStartMachine.release(1);
	}

	@Override
	public void run() {
		final ReentrantLock lock = this.mainLock;
		try {
			lock.lock();
			retry0: for (;;) {
				while (atomicBoolean.get()) {
					System.out.println("machine stopped !");
					arquireOnePauseAndStartSemaphore();
					// condition.await();
					System.out.println("machine start running !");
				}
				for (;;) {
					TaskStamp stampOfAllTask = null;
					LineStamp stampOfOneLineInTheTask = null;
					int lineNum = 0;
					stampOfAllTask = StaticQueueForAutoMachine.TASK_STAMP_QUEUE.pollTaskStamp();
					if (null == stampOfAllTask) {
						arquireOneInitSemaphore();
						continue retry0;
					} else {
						lineNum = stampOfAllTask.getHowManyLineInTask();
						// 取完之后再放回去，保证下次调度的正常运行，此方法可以优化（下面这种方式是实际可行的）
						for (int n = 0; n <= lineNum; n++) {
							stampOfOneLineInTheTask = stampOfAllTask.getLineStampInTask();
							StaticQueueForAutoMachine.EXT_QUEUE_WHICH_STORE_LINE_STAMP
									.addLineStamp(stampOfOneLineInTheTask);
							stampOfAllTask.rePutLineStampIntoQueue(stampOfOneLineInTheTask);
						}
						continue retry0;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			lock.unlock();
		}
	}

	private void arquireOneInitSemaphore() {
		try {
			semaphoreForInitMachine.acquire(1);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}

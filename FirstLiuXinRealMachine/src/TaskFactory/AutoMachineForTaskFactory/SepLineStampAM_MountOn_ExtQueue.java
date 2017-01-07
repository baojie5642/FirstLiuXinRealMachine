package TaskFactory.AutoMachineForTaskFactory;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;

import QueueOfAllTask.StaticQueueForAutoMachine;
import StampOfTaskInformation.LineStamp;
import TaskFactory.ExtractTaskFactory;
import TaskFactory.AutoMachineImpl.AM_Impl;
import TaskRunner.ExtractTaskRunner;
import ThreadPool.StaticForAllThreadPool;

/*
 * thread-safe
 */

public class SepLineStampAM_MountOn_ExtQueue implements Runnable ,AM_Impl{
	private final ReentrantLock mainLock = new ReentrantLock();;
	private final Condition condition;
	private final AtomicBoolean atomicBoolean = new AtomicBoolean(false);
	private final Semaphore semaphore;
	private final ExtractTaskFactory extractFactory = new ExtractTaskFactory();

	public static SepLineStampAM_MountOn_ExtQueue initAutoMachine(Semaphore semaphore) {
		SepLineStampAM_MountOn_ExtQueue autoMachineBaseOnQueue = new SepLineStampAM_MountOn_ExtQueue(semaphore);
		return autoMachineBaseOnQueue;
	}

	private SepLineStampAM_MountOn_ExtQueue(final Semaphore semaphore) {
		super();
		this.semaphore = semaphore;
		this.condition = mainLock.newCondition();
	}
	@Override
	public void pauseMachine() {
		final ReentrantLock lock = this.mainLock;
		try {
			lock.lock();
			boolean firstCheck = false;
			boolean secondCheck = false;
			boolean thirdCheck = false;
			atomicBoolean.set(true);
			firstCheck = StaticQueueForAutoMachine.EXT_QUEUE_WHICH_STORE_LINE_STAMP.isEmpty();
			LockSupport.parkNanos(TimeUnit.NANOSECONDS.convert(1, TimeUnit.MILLISECONDS));
			secondCheck = StaticQueueForAutoMachine.EXT_QUEUE_WHICH_STORE_LINE_STAMP.isEmpty();
			LockSupport.parkNanos(TimeUnit.NANOSECONDS.convert(1, TimeUnit.MILLISECONDS));
			thirdCheck = StaticQueueForAutoMachine.EXT_QUEUE_WHICH_STORE_LINE_STAMP.isEmpty();
			if ((0 == semaphore.availablePermits()) && (semaphore.hasQueuedThreads()) && (firstCheck == true)
					&& (secondCheck == true) && (thirdCheck == true)) {
				semaphore.release(1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			lock.unlock();
		}
	}
	@Override
	public void startMachine() {
		final ReentrantLock lock = this.mainLock;
		try {
			lock.lock();
			atomicBoolean.set(false);
			condition.signal();
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void run() {
		final ReentrantLock lock = this.mainLock;
		try {
			lock.lock();
			retry0: for (;;) {
				while (atomicBoolean.get()) {
					condition.await();
				}
				for (;;) {
					LineStamp lineStamp = null;
					ExtractTaskRunner extractTaskRunner = null;
					lineStamp = StaticQueueForAutoMachine.EXT_QUEUE_WHICH_STORE_LINE_STAMP
							.pollLineStamp();
					if (null == lineStamp) {
						arquireSemaphore();
						continue retry0;
					} else {
						for (int n = 0; n < lineStamp.getExtractThreadNum(); n++) {
							extractTaskRunner = extractFactory.buildExtractRunner(lineStamp, n);
							StaticForAllThreadPool.EXTRACT_TASK_POOL.submit(extractTaskRunner);
						}
						continue retry0;
					}
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			lock.unlock();
		}
	}

	private void arquireSemaphore() {
		try {
			semaphore.acquire(1);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}

package TaskFactory.AutoMachineForTaskFactory;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;
import QueueOfAllTask.StaticQueueForAutoMachine;
import StampOfTaskInformation.LineStamp;
import TaskFactory.ExportTaskFactory;
import TaskFactory.AutoMachineImpl.AM_Impl;
import TaskRunner.ExportTaskRunner;
import ThreadPool.StaticForAllThreadPool;

/*
 * thread-safe
 */

public class ExportAM_MountOn_ExpQFromTransfQ implements Runnable, AM_Impl {
	private final ReentrantLock mainLock = new ReentrantLock();
	private final Condition condition;
	private final AtomicBoolean atomicBoolean = new AtomicBoolean(false);
	private final Semaphore semaphore;
	private final ExportTaskFactory exportTaskFactory = new ExportTaskFactory();

	public static ExportAM_MountOn_ExpQFromTransfQ initAutoMachine(final Semaphore semaphore) {
		ExportAM_MountOn_ExpQFromTransfQ autoMachineBaseOnQueue = new ExportAM_MountOn_ExpQFromTransfQ(semaphore);
		return autoMachineBaseOnQueue;
	}

	private ExportAM_MountOn_ExpQFromTransfQ(final Semaphore semaphore) {
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
			firstCheck = StaticQueueForAutoMachine.EXP_QUEUE_FROM_TRANS.isEmpty();
			LockSupport.parkNanos(TimeUnit.NANOSECONDS.convert(1, TimeUnit.MILLISECONDS));
			secondCheck = StaticQueueForAutoMachine.EXP_QUEUE_FROM_TRANS.isEmpty();
			LockSupport.parkNanos(TimeUnit.NANOSECONDS.convert(1, TimeUnit.MILLISECONDS));
			thirdCheck = StaticQueueForAutoMachine.EXP_QUEUE_FROM_TRANS.isEmpty();
			if (0 == semaphore.availablePermits() && (semaphore.hasQueuedThreads()) && (firstCheck == true)
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
					System.out.println("machine stopped !");
					condition.await();
					System.out.println("machine start running !");
				}
				for (;;) {
					LineStamp stampOfAllTask = null;
					ExportTaskRunner exportTaskRunner = null;
					stampOfAllTask = StaticQueueForAutoMachine.EXP_QUEUE_FROM_TRANS.pollLineStamp();
					if (null == stampOfAllTask) {
						arquireSemaphore();
						continue retry0;
					} else {
						for (int n = 0; n < stampOfAllTask.getExportThreadNum(); n++) {
							exportTaskRunner = exportTaskFactory.buildExtractRunner(stampOfAllTask, n);
							StaticForAllThreadPool.EXPORT_TASK_POOL.submit(exportTaskRunner);
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

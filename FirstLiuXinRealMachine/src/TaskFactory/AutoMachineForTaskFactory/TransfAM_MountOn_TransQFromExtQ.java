package TaskFactory.AutoMachineForTaskFactory;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;

import QueueOfAllTask.StaticQueueForAutoMachine;
import StampOfTaskInformation.LineStamp;
import TaskFactory.TransferTaskFactory;
import TaskFactory.AutoMachineImpl.AM_Impl;
import TaskRunner.TransferTaskRunner;
import ThreadPool.StaticForAllThreadPool;

/*
 * thread-safe
 */

public class TransfAM_MountOn_TransQFromExtQ implements Runnable ,AM_Impl{
	private final ReentrantLock mainLock = new ReentrantLock();
	private final Condition condition;
	private final AtomicBoolean atomicBoolean = new AtomicBoolean(false);
	private final Semaphore semaphore;
	private final TransferTaskFactory transferTaskFactory = new TransferTaskFactory();

	public static TransfAM_MountOn_TransQFromExtQ initAutoMachine(Semaphore semaphore) {
		TransfAM_MountOn_TransQFromExtQ autoMachineBaseOnQueue = new TransfAM_MountOn_TransQFromExtQ(semaphore);
		return autoMachineBaseOnQueue;
	}

	private TransfAM_MountOn_TransQFromExtQ(final Semaphore semaphore) {
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
			firstCheck = StaticQueueForAutoMachine.TRANS_QUEUE_FROM_EXT.isEmpty();
			LockSupport.parkNanos(TimeUnit.NANOSECONDS.convert(1, TimeUnit.MILLISECONDS));
			secondCheck = StaticQueueForAutoMachine.TRANS_QUEUE_FROM_EXT.isEmpty();
			LockSupport.parkNanos(TimeUnit.NANOSECONDS.convert(1, TimeUnit.MILLISECONDS));
			thirdCheck = StaticQueueForAutoMachine.TRANS_QUEUE_FROM_EXT.isEmpty();
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
					TransferTaskRunner transferTaskRunner = null;
					stampOfAllTask = StaticQueueForAutoMachine.TRANS_QUEUE_FROM_EXT.pollLineStamp();
					if (null == stampOfAllTask) {
						arquireSemaphore();
						continue retry0;
					} else {
						transferTaskRunner = transferTaskFactory.buildExtractRunner(stampOfAllTask);
						StaticForAllThreadPool.TRANSFER_TASK_POOL.submit(transferTaskRunner);
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

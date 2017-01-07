package StampOfTaskInformation;

import java.io.Serializable;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import TaskRunner.ExportTaskRunner;

/*
 * thread-safe
 */

public class LineStamp implements Serializable {

	private static final long serialVersionUID = 2015112007498303285L;
	private static final Log LOG = LogFactory.getLog(LineStamp.class);
	private final String taskName;
	private final int whichLineInTask;
	private final long taskID;
	private final String taskStyle;
	private final RealSourceForTask realSourceForTask;

	private final boolean isNeedTransfer;
	private final int extractThreadNum;
	private final int exportThreadNum;
	private final Semaphore semaphoreInOneLineOfTask;

	private final AtomicInteger extractThreadAtomicInteger;
	private final AtomicInteger exportThreadAtomicInteger;

	private final ReentrantReadWriteLock mainLock = new ReentrantReadWriteLock();

	private volatile String fileFullPathEndWithLineStamp = "NULL";

	private static final String WRITETXTDIR_STRING = "/home/liuxin/worker/automachinetxtdir";

	public static String getWriteTxtDir() {
		return WRITETXTDIR_STRING;
	}

	public static LineStamp init(final String taskName, final long taskID, final String taskStyle,
			final RealSourceForTask realSourceForTask, final Semaphore semaphoreFromTheTask, final int whichLine) {
		LineStamp stampOfAllTask = new LineStamp(taskName, taskID, taskStyle, realSourceForTask, semaphoreFromTheTask,
				whichLine);
		return stampOfAllTask;
	}

	private LineStamp(final String taskName, final long taskID, final String taskStyle,
			final RealSourceForTask realSourceForTask, final Semaphore semaphoreFromTheTask, final int whichLine) {
		super();
		this.taskName = taskName;
		this.taskID = taskID;
		this.taskStyle = taskStyle;
		this.isNeedTransfer = makeTransferRight();
		this.realSourceForTask = realSourceForTask;
		this.extractThreadNum = returnThreadNumForExtractAndExport();
		this.exportThreadNum = returnThreadNumForExtractAndExport();
		this.extractThreadAtomicInteger = new AtomicInteger(this.extractThreadNum);
		this.exportThreadAtomicInteger = new AtomicInteger(this.exportThreadNum);
		this.semaphoreInOneLineOfTask = semaphoreFromTheTask;
		this.whichLineInTask = whichLine;
	}

	public boolean isOneThreadExtractFinishedByLock() {
		final ReentrantReadWriteLock lock = this.mainLock;
		boolean flag = false;
		try {
			lock.writeLock().lock();
			int i = 0;
			if (extractThreadAtomicInteger.get() > 0) {
				i = extractThreadAtomicInteger.decrementAndGet();
				if (0 == i) {
					flag = true;
					extractThreadAtomicInteger.set(extractThreadNum);
					LOG.info("last extract thread ");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			lock.writeLock().unlock();
		}
		return flag;
	}

	public boolean isOneThreadExportFinishedByLock() {
		final ReentrantReadWriteLock lock = this.mainLock;
		boolean flag = false;
		int i = 0;
		try {
			lock.writeLock().lock();
			flag = false;
			i = 0;
			if (exportThreadAtomicInteger.get() > 0) {
				i = exportThreadAtomicInteger.decrementAndGet();
				if (0 == i) {
					flag = true;
					exportThreadAtomicInteger.set(exportThreadNum);
					// 输出完成将路径初始化
					// setFileFullPathPriv("NULL");
					fileFullPathEndWithLineStamp = "NULL";
					LOG.info("last export thread ");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			lock.writeLock().unlock();
		}
		return flag;
	}

	private int returnThreadNumForExtractAndExport() {
		int threadNum = 1;
		ThreadLocalRandom random = null;
		try {
			random = ThreadLocalRandom.current();
			threadNum = random.nextInt(8) + 1;
		} finally {
			if (null != random) {
				random = null;
			}
		}
		return 3;// threadNum
	}

	private boolean makeTransferRight() {
		boolean isNeedTransfer = false;
		ThreadLocalRandom random = null;
		try {
			random = ThreadLocalRandom.current();
			isNeedTransfer = random.nextBoolean();
		} finally {
			if (null != random) {
				random = null;
			}
		}
		return false;// isNeedTransfer
	}

	public void releaseOneSemaphore() {
		releaseOneSemaphorePrivate();
	}

	private void releaseOneSemaphorePrivate() {
		semaphoreInOneLineOfTask.release(1);
	}

	public void setFileFullPath(final String fileFullPath) {
		setFileFullPathPriv(fileFullPath);
	}

	// 如果线程多的话 最好消除一下锁的竞争（十个线程应该没有什么必要），设置一个开关
	private void setFileFullPathPriv(final String pathEndWithLineStamp) {
		final ReentrantReadWriteLock lock = this.mainLock;
		try {
			lock.writeLock().lock();
			if ("NULL".equals(fileFullPathEndWithLineStamp)) {
				fileFullPathEndWithLineStamp = pathEndWithLineStamp;
				LOG.info("set  fileFullPathEndWithLineStamp");
			}
		} finally {
			lock.writeLock().unlock();
		}
	}

	public String getFileFullPath() {
		return getFileFullPathPriv();
	}

	private String getFileFullPathPriv() {
		final ReentrantReadWriteLock lock = this.mainLock;
		try {
			lock.readLock().lock();
			return fileFullPathEndWithLineStamp;
		} finally {
			lock.readLock().unlock();
		}
	}

	/*
	 * 之下not-thread-safe
	 */
	public int reduceExtractThreadAtomicNum() {
		return extractThreadAtomicInteger.decrementAndGet();
	}

	public int increaseExtractThreadAtomicNum() {
		return extractThreadAtomicInteger.incrementAndGet();
	}

	public int reduceExportThreadAtomicNum() {
		return exportThreadAtomicInteger.decrementAndGet();
	}

	public int increaseExportThreadAtomicNum() {
		return exportThreadAtomicInteger.incrementAndGet();
	}

	/*
	 * 以上not-thread-safe
	 */

	public Semaphore getTaskSemaphore() {
		return semaphoreInOneLineOfTask;
	}

	public AtomicInteger getExtractThreadAtomicInteger() {
		return extractThreadAtomicInteger;
	}

	public AtomicInteger getExportThreadAtomicInteger() {
		return exportThreadAtomicInteger;
	}

	public int getExtractThreadNum() {
		return extractThreadNum;
	}

	public int getExportThreadNum() {
		return exportThreadNum;
	}

	public String getTaskName() {
		return taskName;
	}

	public long getTaskID() {
		return taskID;
	}

	public String getTaskStyle() {
		return taskStyle;
	}

	public boolean isNeedTransfer() {
		return isNeedTransfer;
	}

	public RealSourceForTask getRealSourceForTask() {
		return realSourceForTask;
	}

	public int getWhichLineInTask() {
		return whichLineInTask;
	}

}

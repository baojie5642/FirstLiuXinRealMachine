package QueueOfAllTask;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import QueueOfAllTask.Impl.ImplOfLineStampQueue;
import StampOfTaskInformation.LineStamp;

public class LineStampQueue extends ConcurrentLinkedQueue<LineStamp> implements ImplOfLineStampQueue {

	private static final long serialVersionUID = 2016032212345678910L;
	private static final Log LOG = LogFactory.getLog(LineStampQueue.class);
	private final Semaphore semaphore = new Semaphore(1);

	public static LineStampQueue initLineStampQueue() {
		LineStampQueue queueForAllTaskStyle = new LineStampQueue();
		return queueForAllTaskStyle;
	}

	private LineStampQueue() {
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
	public boolean addLineStamp(final LineStamp lineStamp) {
		super.add(lineStamp);
		if (0 == semaphore.availablePermits()) {
			semaphore.release(1);
		}
		return true;
	}

	@Override
	public LineStamp pollLineStamp() {
		LineStamp lineStamp = super.poll();
		if (1 == semaphore.availablePermits()) {
			acquireOneSemaphore(semaphore);
		}
		return lineStamp;
	}
}

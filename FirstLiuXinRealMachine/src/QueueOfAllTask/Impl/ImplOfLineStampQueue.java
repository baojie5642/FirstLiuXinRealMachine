package QueueOfAllTask.Impl;

import java.util.concurrent.Semaphore;
import StampOfTaskInformation.LineStamp;

public interface ImplOfLineStampQueue {
	
	public LineStamp pollLineStamp();

	public boolean addLineStamp(final LineStamp lineStamp);

	public Semaphore getSemaphoreForInitMachine();
	
}

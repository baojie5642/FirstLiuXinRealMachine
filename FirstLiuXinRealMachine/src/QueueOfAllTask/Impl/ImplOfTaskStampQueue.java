package QueueOfAllTask.Impl;

import java.util.concurrent.Semaphore;

import StampOfTaskInformation.TaskStamp;

public interface ImplOfTaskStampQueue {
	
	public TaskStamp pollTaskStamp();

	public boolean addTaskStamp(final TaskStamp taskStamp);

	public Semaphore getSemaphoreForInitMachine();
	
}

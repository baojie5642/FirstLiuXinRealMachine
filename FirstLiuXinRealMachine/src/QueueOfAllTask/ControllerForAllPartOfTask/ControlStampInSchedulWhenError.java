package QueueOfAllTask.ControllerForAllPartOfTask;

import java.util.concurrent.ConcurrentHashMap;

import StampOfTaskInformation.LineStamp;

public class ControlStampInSchedulWhenError {
	public final static ConcurrentHashMap<String, LineStamp> controlStampInSchedulWhenErrorMap = new ConcurrentHashMap<String, LineStamp>(
			10000);
}

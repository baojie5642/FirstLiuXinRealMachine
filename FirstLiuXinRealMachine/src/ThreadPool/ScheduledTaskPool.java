package ThreadPool;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import ThreadFactory.ThreadFactoryForThreadPool;
import ThreadRejectMethod.LocalRejectedExecutionHandler;

/*
 * thread-safe
 */

public class ScheduledTaskPool extends ScheduledThreadPoolExecutor {

	public static ScheduledTaskPool initScheduledTaskPool(int corePoolSize) {
		ScheduledTaskPool scheduledTaskPool = new ScheduledTaskPool(corePoolSize);
		return scheduledTaskPool;
	}

	private ScheduledTaskPool(int corePoolSize) {
		super(corePoolSize, ThreadFactoryForThreadPool.init("liuxinScheduledThreadPool"), LocalRejectedExecutionHandler
				.init());
		super.setContinueExistingPeriodicTasksAfterShutdownPolicy(true);
		super.setExecuteExistingDelayedTasksAfterShutdownPolicy(true);
		super.setRemoveOnCancelPolicy(true);
		// super.setKeepAliveTime(200, TimeUnit.SECONDS);
		// super.allowCoreThreadTimeOut(true);
	}
}

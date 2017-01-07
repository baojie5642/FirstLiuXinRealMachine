package ThreadPool;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import ThreadFactory.ThreadFactoryForThreadPool;
import ThreadRejectMethod.LocalRejectedExecutionHandler;

/*
 * thread-safe
 */

public class ExtractTaskPool extends ThreadPoolExecutor {

	public static ExtractTaskPool initExtractTaskPool(int corePoolSize, int maximumPoolSize, long keepAliveTime,
			TimeUnit unit, SynchronousQueue<Runnable> workQueue) {
		ExtractTaskPool extractTaskPool = new ExtractTaskPool(corePoolSize, maximumPoolSize, keepAliveTime, unit,
				workQueue);
		return extractTaskPool;
	}

	private ExtractTaskPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
			SynchronousQueue<Runnable> workQueue) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, ThreadFactoryForThreadPool
				.init("liuxinExtractThreadPool"), LocalRejectedExecutionHandler.init());
		super.allowCoreThreadTimeOut(true);
		super.prestartCoreThread();
	}
}

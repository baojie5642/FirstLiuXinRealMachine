package ThreadRejectMethod;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import ThreadPool.StaticForAllThreadPool;

/*
 * thread-safe
 */

public class LocalRejectedExecutionHandler implements RejectedExecutionHandler {

	public static LocalRejectedExecutionHandler init() {
		return new LocalRejectedExecutionHandler();
	}

	private LocalRejectedExecutionHandler() {
		
	}

	@Override
	public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
		if(executor.getQueue().offer(r)){
			return;
		}else{
			StaticForAllThreadPool.REJECTED_THREAD_POOL.submit(r);
		}
	}
}

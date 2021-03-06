package ThreadFactory;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import Error.ThreadUncaughtExceptionHandler;

/*
 * thread-safe
 */

public class ThreadFactoryForThreadPool implements ThreadFactory {
	private static final AtomicInteger poolNumber = new AtomicInteger(1);
	private final ThreadGroup group;
	private final AtomicInteger threadNumber = new AtomicInteger(1);
	private final String namePrefix;
	private final String factoryName;
	private final UncaughtExceptionHandler unCaughtExceptionHandler = new ThreadUncaughtExceptionHandler();

	private ThreadFactoryForThreadPool(final String name) {
		SecurityManager s = System.getSecurityManager();
		group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
		factoryName = name;
		namePrefix = factoryName + "-" + poolNumber.getAndIncrement() + "-thread-";
	}

	public static ThreadFactoryForThreadPool init(final String name) {
		ThreadFactoryForThreadPool threadFactoryForThreadPool = null;
		try {
			threadFactoryForThreadPool = new ThreadFactoryForThreadPool(name);
		} finally {
		}
		return threadFactoryForThreadPool;
	}

	@Override
	public Thread newThread(Runnable r) {
		Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
		if (t.isDaemon())
			t.setDaemon(false);
		if (t.getPriority() != Thread.NORM_PRIORITY)
			t.setPriority(Thread.NORM_PRIORITY);
		t.setUncaughtExceptionHandler(unCaughtExceptionHandler);
		return t;
	}
}

package ThreadPool;

import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import ThreadFactory.ThreadFactoryForThreadPool;

/*
 * thread-safe
 */

public class StaticForAllThreadPool {

	public static final RejectedThreadPool REJECTED_THREAD_POOL = RejectedThreadPool.initRejectedThreadPool(800, 2048,
			120, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(1024));

	public static final ExtractTaskPool EXTRACT_TASK_POOL = ExtractTaskPool.initExtractTaskPool(1024, 2048, 120,
			TimeUnit.SECONDS, new SynchronousQueue<Runnable>());

	public static final TransferTaskPool TRANSFER_TASK_POOL = TransferTaskPool.initTransferTaskPool(1024, 2048, 120,
			TimeUnit.SECONDS, new SynchronousQueue<Runnable>());

	public static final ExportTaskPool EXPORT_TASK_POOL = ExportTaskPool.initExportTaskPool(1024, 2048, 120,
			TimeUnit.SECONDS, new SynchronousQueue<Runnable>());

	public static final ScheduledTaskPool SCHEDULED_TASK_POOL = ScheduledTaskPool.initScheduledTaskPool(512);

	public static final ThreadPoolExecutor MACHINE_THREAD_POOL = (ThreadPoolExecutor) Executors
			.newCachedThreadPool(ThreadFactoryForThreadPool.init("MachineThreadPool"));

}

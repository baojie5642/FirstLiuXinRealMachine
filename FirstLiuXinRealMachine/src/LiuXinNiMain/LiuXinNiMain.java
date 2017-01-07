package LiuXinNiMain;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import QueueOfAllTask.StaticQueueForAutoMachine;
import StampOfTaskInformation.RealSourceForTask;
import StampOfTaskInformation.LineStamp;
import StampOfTaskInformation.TaskStamp;
import TaskFactory.AutoMachineForTaskFactory.SepLineStampAM_MountOn_ExtQueue;
import TaskFactory.AutoMachineForTaskFactory.TransfAM_MountOn_TransQFromExtQ;
import TaskFactory.AutoMachineForTaskFactory.MakeLineStampAM_MountOn_TSQueue;
import TaskFactory.AutoMachineForTaskFactory.ExportAM_MountOn_ExpQFromTransfQ;
import TaskRunner.ScheduledTaskRunner;
import ThreadPool.StaticForAllThreadPool;

public class LiuXinNiMain {
	public static void main(String args[]) {
		RealSourceForTask realSourceForTask = null;
		LineStamp stampOfAllTask = null;
		ScheduledTaskRunner scheduledTaskRunner = null;

		TaskStamp taskStamp = null;

		try {

			MakeLineStampAM_MountOn_TSQueue makeLineStampAM_MountOn_TSQueue = MakeLineStampAM_MountOn_TSQueue
					.initAutoMachine(StaticQueueForAutoMachine.TASK_STAMP_QUEUE.getSemaphoreForInitMachine());

			SepLineStampAM_MountOn_ExtQueue sepLineStampAM_MountOn_ExtQueue = SepLineStampAM_MountOn_ExtQueue
					.initAutoMachine(StaticQueueForAutoMachine.EXT_QUEUE_WHICH_STORE_LINE_STAMP
							.getSemaphoreForInitMachine());

			// TransfAM_MountOn_TransQFromExtQ transfAM_MountOn_TransQFromExtQ =
			// TransfAM_MountOn_TransQFromExtQ
			// .initAutoMachine(StaticQueueForAutoMachine.TRANS_QUEUE_FROM_EXT.getSemaphoreForInitMachine());

			ExportAM_MountOn_ExpQFromTransfQ exportAM_MountOn_ExpQFromTransfQ = ExportAM_MountOn_ExpQFromTransfQ
					.initAutoMachine(StaticQueueForAutoMachine.EXP_QUEUE_FROM_TRANS.getSemaphoreForInitMachine());

			Thread makeLineStamp = new Thread(makeLineStampAM_MountOn_TSQueue, "makeLineStampAM_MountOn_TSQueue");
			// makeLineStamp.start();
			StaticForAllThreadPool.MACHINE_THREAD_POOL.submit(makeLineStamp);

			Thread sepLineStamp = new Thread(sepLineStampAM_MountOn_ExtQueue, "sepLineStampAM_MountOn_ExtQueue");
			// sepLineStamp.start();
			StaticForAllThreadPool.MACHINE_THREAD_POOL.submit(sepLineStamp);

			// Thread transfAM = new Thread(transfAM_MountOn_TransQFromExtQ);
			// transfAM.start();

			Thread exportAM = new Thread(exportAM_MountOn_ExpQFromTransfQ, "exportAM_MountOn_ExpQFromTransfQ");
			// exportAM.start();
			StaticForAllThreadPool.MACHINE_THREAD_POOL.submit(exportAM);

			realSourceForTask = RealSourceForTask.init("bonecp", "storefrombonecp");

			LockSupport.parkNanos(TimeUnit.NANOSECONDS.convert(3, TimeUnit.SECONDS));

			int howManyLine = 3;
			int whichLineInTask = 0;

			for (int i = 0; i < 200; i++) {

				taskStamp = TaskStamp.init(howManyLine, i, "liuxin" + i);
				for (; whichLineInTask < howManyLine; whichLineInTask++) {
					taskStamp.createLineStampInTaskStamp("liucheng", realSourceForTask, whichLineInTask);
				}
				whichLineInTask = 0;

				scheduledTaskRunner = ScheduledTaskRunner.init(taskStamp);

				StaticForAllThreadPool.SCHEDULED_TASK_POOL.scheduleWithFixedDelay(scheduledTaskRunner,
						taskStamp.getWaitTimeBeforeFirstScheduled() + 1, taskStamp.getScheduledTime() + 1,
						TimeUnit.SECONDS);
				//
				//
			}
		} finally {

		}
	}
}

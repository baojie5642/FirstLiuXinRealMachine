package LiuXinTestForAllThing;

import java.util.concurrent.Semaphore;

public class TestForReleaseSemManyTimes {
	public static void main(String args[]) {
		Semaphore semaphore = new Semaphore(1);
		try {
			semaphore.acquire(1);
			System.out.println("success-acquire");
			semaphore.release(1);
			System.out.println("success-release-onetime");
			semaphore.release(1);
			System.out.println("success-release-twotime");
			semaphore.release(1);
			System.out.println("success-release-threetime");
			semaphore.release(1);
			System.out.println("success-release-fourtime");
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			if (null != semaphore) {
				semaphore.release();
				semaphore = null;
			}
		}
	}
}

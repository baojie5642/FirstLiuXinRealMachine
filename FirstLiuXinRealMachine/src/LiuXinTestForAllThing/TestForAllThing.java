package LiuXinTestForAllThing;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

public class TestForAllThing {
	public static void main(String args[]) {
		Thread thread = null;
		LiuXinRunnable liuXinRunnable = null;

		String a = null;
		String b = null;
		String c = null;

		LiuXinStruction1 liuXinStruction10 = null;
		LiuXinStruction1 liuXinStruction11 = null;
		LiuXinStruction1 liuXinStruction12 = null;

		try {
			liuXinRunnable = new LiuXinRunnable();
			thread = new Thread(liuXinRunnable);
			// thread.start();

			a = "a" + 0;
			b = "b" + 0;
			c = "c" + 0;
			liuXinStruction10 = LiuXinStruction1.init(a + "-10", b + "-10", c + "-10");
			liuXinStruction11 = LiuXinStruction1.init(a + "-11", b + "-11", c + "-11");
			liuXinStruction12 = LiuXinStruction1.init(a + "-12", b + "-12", c + "-12");
			final LiuXinStruction0 liuXinStruction0 = LiuXinStruction0.init(a, b, c, liuXinStruction10,
					liuXinStruction11, liuXinStruction12);
			
			ATestQueue.CONCURRENT_LINKED_QUEUE.add(liuXinStruction0);
			
			System.out.println("Runnable    put   success  !");
			
			System.out.println(liuXinStruction0.getaString() + "   " + liuXinStruction0.getbString() + "   "
					+ liuXinStruction0.getcString() + "   " + liuXinStruction0.getLiuXinStruction10().getcString()
					+ "   " + liuXinStruction0.getLiuXinStruction11().getbString() + "   "
					+ liuXinStruction0.getLiuXinStruction12().getaString());

			LockSupport.parkNanos(TimeUnit.NANOSECONDS.convert(3, TimeUnit.SECONDS));
			
			a = null;
			b = null;
			c = null;
			liuXinStruction12 = null;
			liuXinStruction11 = null;
			liuXinStruction10 = null;
			System.out.println("Runnable    set   null   success  !");
			
			liuXinStruction0.setaString("liuxin+a");
			liuXinStruction0.setbString("liuxin+b");
			liuXinStruction0.setcString("liuxin+c");
			liuXinStruction0.getLiuXinStruction10().setcString("liuxinaabbcc");
			liuXinStruction0.getLiuXinStruction11().setbString("liuxinaabbcc");
			liuXinStruction0.getLiuXinStruction12().setaString("liuxinaabbcc");
			
			LiuXinStruction0 liuXinStruction02=ATestQueue.CONCURRENT_LINKED_QUEUE.poll();
			
			System.out.println(liuXinStruction02.getaString() + "   " + liuXinStruction02.getbString() + "   "
					+ liuXinStruction02.getcString() + "   " + liuXinStruction02.getLiuXinStruction10().getcString()
					+ "   " + liuXinStruction02.getLiuXinStruction11().getbString() + "   "
					+ liuXinStruction02.getLiuXinStruction12().getaString());
			
			
			

		} finally {

		}

	}

}

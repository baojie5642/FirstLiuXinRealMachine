package LiuXinTestForAllThing;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

public class LiuXinRunnable implements Runnable {

	public LiuXinRunnable(){
		super();
	}
	
	
	
	@Override
	public void run() {
		String a = null;
		String b = null;
		String c = null;
		LiuXinStruction0 liuXinStruction0 = null;
		LiuXinStruction1 liuXinStruction10 = null;
		LiuXinStruction1 liuXinStruction11 = null;
		LiuXinStruction1 liuXinStruction12 = null;
		try {
			for (int i = 0; i < 10; i++) {
				a = "a" + i;
				b = "b" + i;
				c = "c" + i;
				liuXinStruction10 = LiuXinStruction1.init(a + "-10", b + "-10", c + "-10");
				liuXinStruction11 = LiuXinStruction1.init(a + "-11", b + "-11", c + "-11");
				liuXinStruction12 = LiuXinStruction1.init(a + "-12", b + "-12", c + "-12");
				liuXinStruction0 = LiuXinStruction0.init(a, b, c, liuXinStruction10, liuXinStruction11,
						liuXinStruction12);
				ATestQueue.CONCURRENT_LINKED_QUEUE.add(liuXinStruction0);
				System.out.println("Runnable    put   success  !");
				a = null;
				b = null;
				c = null;
				liuXinStruction0 = null;
				liuXinStruction12 = null;
				liuXinStruction11 = null;
				liuXinStruction10 = null;
				System.out.println("Runnable    set   null   success  !");
			}
			a = null;
			b = null;
			c = null;
			liuXinStruction0 = null;
			liuXinStruction12 = null;
			liuXinStruction11 = null;
			liuXinStruction10 = null;
		} finally {
			if (null != a) {
				a = null;
			}
			if (null != b) {
				b = null;
			}
			if (null != c) {
				c = null;
			}
			if (null != liuXinStruction0) {
				liuXinStruction0 = null;
			}
			if (null != liuXinStruction12) {
				liuXinStruction12 = null;
			}
			if (null != liuXinStruction11) {
				liuXinStruction11 = null;
			}
			if (null != liuXinStruction10) {
				liuXinStruction10 = null;
			}
		}
	}
	
	public static void main(String args[]){
		LiuXinRunnable liuXinRunnable=null;
		Thread thread=null;
		LiuXinStruction0 liuXinStruction00 =null;
		LiuXinStruction0 liuXinStruction01 =null;
		try{
			liuXinRunnable=new LiuXinRunnable();
			thread=new Thread(liuXinRunnable);
			
			thread.start();
			LockSupport.parkNanos(TimeUnit.NANOSECONDS.convert(3, TimeUnit.SECONDS));
			
			for(int i=0;i<10;i++){
				liuXinStruction00=ATestQueue.CONCURRENT_LINKED_QUEUE.poll();
				System.out.println(liuXinStruction00.getaString());
				ATestQueue.CONCURRENT_LINKED_QUEUE.add(liuXinStruction00);
			}
			
			LockSupport.parkNanos(TimeUnit.NANOSECONDS.convert(3, TimeUnit.SECONDS));
			System.out.println("………………………………………………");
			for(int i=0;i<10;i++){
				liuXinStruction01=ATestQueue.CONCURRENT_LINKED_QUEUE.poll();
				System.out.println(liuXinStruction01.getaString());
			}
			
			
		}finally{
			
			
			
		}
		
		
		
		
	}
	
	
	
	
	
	
	
	

}

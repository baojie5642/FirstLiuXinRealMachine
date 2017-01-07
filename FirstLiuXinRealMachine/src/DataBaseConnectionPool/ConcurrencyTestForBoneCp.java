package DataBaseConnectionPool;

import java.sql.Connection;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Future;

import com.zaxxer.hikari.HikariDataSource;

public class ConcurrencyTestForBoneCp {
	public static void main(String args[]) {

		ThreadPoolExecutor executor = new ThreadPoolExecutor(500, 600, 60, TimeUnit.SECONDS,
				new LinkedBlockingQueue<Runnable>(Integer.MAX_VALUE));
		Future<Integer> future;
		List<Future<Integer>> resultList = new ArrayList<Future<Integer>>();

		HikariCP hikariCP = null;
		LiuXinHikariDS hikariDS=null;
		HikariDataSource hikariDataSource=null;
		Connection connection=null;
		ConcurrencyTaskHikari concurrencyTaskBoneCp=null;
		
		
		Date nowstart = new Date();
		DateFormat datestart = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL); // 显示日期，周，时间（精确到秒）
		String strstart = datestart.format(nowstart);
		System.out.println(strstart);

		long starttime = System.nanoTime();
		System.out.println(starttime);
		try {
			hikariCP=HikariCP.createHikariCP();
			hikariDS=LiuXinHikariDS.createHikariDS();
			hikariDataSource=hikariDS.getHikariDataSource();
			
			for (int i = 0; i < 1; i++) {
				connection=hikariDataSource.getConnection();
				concurrencyTaskBoneCp = new ConcurrencyTaskHikari("Task " + i, connection);
				future = executor.submit(concurrencyTaskBoneCp);
				resultList.add(future);
				System.out.println("*******YES******* " + i);
			}
			for (int i = 0; i < resultList.size(); i++) {
				Future<Integer> resultFuture = resultList.get(i);
				System.out.println("The Task " + i + "is Done " + resultFuture.isDone());
			}
			for (int i = 0; i < resultList.size(); i++) {
				Future<Integer> reFuture = resultList.get(i);
				Integer numInteger = null;

				numInteger = reFuture.get();

				System.out.println(numInteger);
			}
			System.out.println("关闭线程池……");
			executor.shutdown();
			System.out.println("关闭数据库连接池，销毁链接……");
			hikariCP.getHikariPool().shutdown();
			
			long endtime = System.nanoTime();
			System.out.println(endtime);
			System.out.println("耗时毫微秒： " + (endtime - starttime) + " 毫微秒");
			double timemil = (endtime - starttime) / 1000000;
			double timesec = (endtime - starttime) / 1000000000;
			System.out.println("总共用时 " + timemil + " 毫秒……");
			System.out.println("总共用时 " + timesec + " 秒……");
			Date nowend = new Date();
			DateFormat dateend = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL); // 显示日期，周，时间（精确到秒）
			String strend = dateend.format(nowend);
			System.out.println(strend);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

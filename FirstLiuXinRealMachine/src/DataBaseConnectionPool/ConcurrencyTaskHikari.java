package DataBaseConnectionPool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.ReentrantLock;

public class ConcurrencyTaskHikari implements Callable<Integer> {

	private String taskName;
	private final ReentrantLock lock = new ReentrantLock();
	private ThreadLocal<Integer> resultNumLocal = new ThreadLocal<Integer>() {
		public Integer initialValue() {
			return 0;
		}
	};
	private PreparedStatement preparedStatement = null;
	private static final int batchSize = 4;
	private Connection connection;

	public ConcurrencyTaskHikari(String taskName, Connection connection) {
		super();
		this.taskName = taskName;
		this.connection = connection;
	}

	public Integer call() {
		final ReentrantLock lock = this.lock;
		try {
			preparedStatement = connection
					.prepareStatement("insert into bonecp (idname,agename,name,numname,cdma,ttd,wcdma,gms,qwe,qqwe,wwer,eerw,eewq,asde,qqsd,eewqs,qweqwed,ddfre,ssedw,ffer) values(?, ?, ?, ?,?, ?, ?, ?,?, ?, ?, ?,?, ?, ?, ?,?, ?, ?, ?)");
			connection.setAutoCommit(false);
			preparedStatement.clearBatch();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		lock.lock();
		try {
			int j = 0;
			for (int i = 0; i < 27; i++) {
				preparedStatement.setInt(1, i);
				preparedStatement.setString(2, "q");
				preparedStatement.setString(3, "w");
				preparedStatement.setString(4, "e");
				preparedStatement.setString(5, "r");
				preparedStatement.setString(6, "t");
				preparedStatement.setString(7, "y");
				preparedStatement.setString(8, "u");
				preparedStatement.setString(9, "i");
				preparedStatement.setString(10, "o");
				preparedStatement.setString(11, "p");

				preparedStatement.setString(12, "liuxin" + j++);
				preparedStatement.setString(13, "liuxin" + j++);
				preparedStatement.setString(14, "liuxin" + j++);
				preparedStatement.setString(15, "liuxin" + j++);
				preparedStatement.setString(16, "liuxin" + j++);

				preparedStatement.setString(17, "liuxin" + j++);
				preparedStatement.setString(18, "liuxin" + j++);
				preparedStatement.setString(19, "liuxin" + j++);
				preparedStatement.setString(20, "liuxin" + j++);
				preparedStatement.addBatch();
				if ((i + 1) % batchSize == 0) {
					preparedStatement.executeBatch();
					resultNumLocal.set(resultNumLocal.get() + 1);
				}
			}
			// 为了放置最后的数据不能提交
			preparedStatement.executeBatch();
			connection.setAutoCommit(true);
			// 这里的close并没有关闭链接，只是将这个链接放进链接池中
			// 这使用的是BoneCP链接池中的close，并不是真正的把conn关掉，只是放回到连接池中……
			connection.close();
			System.out.println("The num in" + taskName + "is " + resultNumLocal.get());
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (preparedStatement != null) {
					preparedStatement.close();
					preparedStatement = null;
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			lock.unlock();
		}
		return resultNumLocal.get();
	}

}

package DataBaseConnectionPool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

public class PutDataIntoTableForOtter {
	public static void main(String args[]) {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		LiuXinHikariDS liuXinHikariDS = LiuXinHikariDS.createHikariDS();

		try {
			connection = liuXinHikariDS.liuxinGetConnection();
		} catch (SQLException e4) {
			e4.printStackTrace();
		}
		try {
			connection.setAutoCommit(false);
		} catch (SQLException e3) {
			e3.printStackTrace();
		}
		try {
			preparedStatement = connection.prepareStatement("insert into slavetest (id,name,age) values (?,?,?)");
		} catch (SQLException e2) {
			e2.printStackTrace();
		}
		try {
			preparedStatement.clearBatch();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		int total = 0;
		for (;;) {
			try {
				connection.setAutoCommit(false);
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			try {
				for (int j = 0; j < 100; j++) {
					connection.setAutoCommit(false);
					for (int i = 0; i < 64; i++) {
						preparedStatement.setInt(1, total);
						preparedStatement.setString(2, "liuxin" + total);
						preparedStatement.setInt(3, total+1);
						preparedStatement.addBatch();
						total++;
					}
					preparedStatement.executeBatch();
				}
				preparedStatement.executeBatch();
				connection.setAutoCommit(true);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			LockSupport.parkNanos(TimeUnit.NANOSECONDS.convert(1, TimeUnit.SECONDS));
		}
	}
}

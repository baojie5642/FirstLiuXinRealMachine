package DataBaseConnectionPool;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.pool.HikariPool;

public class HikariCP {
	private final HikariPool hikariPool;
	private final HikariConfig hikariConfig;

	private HikariCP() {
		super();
		this.hikariConfig = new HikariConfig();
		this.hikariConfig.setDriverClassName("com.mysql.jdbc.Driver");
		this.hikariConfig.setMaximumPoolSize(1024);
		this.hikariConfig.setUsername("root");
		this.hikariConfig.setPoolName("LiuXinConnectionPool");
		this.hikariConfig.setPassword("1234");
		this.hikariConfig.setMinimumIdle(256);
		this.hikariConfig.setJdbcUrl("jdbc:mysql://10.1.43.24:3306/otterwintest?rewriteBatchedStatements=true");
		this.hikariPool = new HikariPool(hikariConfig);
	}

	public static HikariCP createHikariCP() {
		try {
			final HikariCP hikariCP = new HikariCP();
			return hikariCP;
		} finally {
		}
	}

	private HikariPool getHikariPoolPrivate() {
		return this.hikariPool;
	}

	public HikariPool getHikariPool() {
		return getHikariPoolPrivate();
	}

	public static void main(String args[]) {
		HikariCP hikariCP = null;
		Connection connection = null;
		Statement stmt=null;
		try {
			hikariCP = HikariCP.createHikariCP();
			connection = hikariCP.getHikariPool().getConnection();	
			stmt = connection.createStatement();
			boolean hasResultSet = stmt.execute("truncate table slavetest");
			System.out.println(hasResultSet);
			stmt.close();
			stmt=null;
			connection.close();
			connection=null;
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if(null!=stmt){
				try {
					stmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				stmt=null;
			}
			if(null!=connection){
				try {
					connection.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				connection=null;
			}
		}

	}

}

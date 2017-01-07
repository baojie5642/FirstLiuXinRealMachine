package DataBaseConnectionPool;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class LiuXinHikariDS {
	private static final byte[] byteLock = new byte[0];
	private final HikariDataSource hikariDataSource;
	private final AtomicReference<HikariConfig> hikariConfig = new AtomicReference<HikariConfig>(null);
	private final AtomicBoolean isDestoryed = new AtomicBoolean(false);

	public static LiuXinHikariDS createHikariDS() {
		LiuXinHikariDS hikariDS = new LiuXinHikariDS(null);
		return hikariDS;
	}

	public static LiuXinHikariDS createHikariDS(final HikariConfig hikariConfigForInit) {
		LiuXinHikariDS hikariDS = new LiuXinHikariDS(hikariConfigForInit);
		return hikariDS;
	}

	private LiuXinHikariDS(final HikariConfig hikariConfigForInit) {
		super();
		initHikariConfig(hikariConfigForInit);
		hikariDataSource = new HikariDataSource(hikariConfig.get());
	}

	private void initHikariConfig(final HikariConfig hikariConfigForInit) {
		if (null == hikariConfigForInit) {
			initInner();
		} else {
			hikariConfig.set(hikariConfigForInit);
		}
	}

	private void initInner() {
		HikariConfig innerConfig = new HikariConfig();
		synchronized (byteLock) {
			innerConfig.setMaximumPoolSize(512);
			innerConfig.setMinimumIdle(128);
			innerConfig.setDriverClassName("com.mysql.jdbc.Driver");
			innerConfig.setUsername("root");
			innerConfig.setPassword("1234");
			innerConfig.setJdbcUrl("jdbc:mysql://10.1.43.24:3306/otterwintest?rewriteBatchedStatements=true");//
			innerConfig.setPoolName("LiuXinConnectionPool");
			innerConfig.setIdleTimeout(600000);
			innerConfig.setMaxLifetime(600000);
			innerConfig.addDataSourceProperty("useUnicode", "true");
			innerConfig.addDataSourceProperty("characterEncoding", "utf8");
			innerConfig.addDataSourceProperty("cachePrepStmts", "true");
			innerConfig.addDataSourceProperty("prepStmtCacheSize", "256");
			innerConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "512");
		}
		hikariConfig.set(innerConfig);
	}

	public Connection liuxinGetConnection() throws SQLException {
		return liuxinGetConnectionPri();
	}

	private Connection liuxinGetConnectionPri() throws SQLException {
		return hikariDataSource.getConnection();
	}

	public HikariDataSource getHikariDataSource() {
		return getHikariDataSourcePrivate();
	}

	private HikariDataSource getHikariDataSourcePrivate() {
		return hikariDataSource;
	}

	public HikariConfig liuxinGetHikariConfig() {
		return hikariConfig.get();
	}

	public void destoryLiuXinHikariDS() {
		if (isDestoryed.get()) {
			return;
		} else {
			synchronized (byteLock) {
				if (isDestoryed.get()) {
					return;
				} else {
					innerDestory();
				}
			}
		}
	}

	private void innerDestory() {
		innerDestoryDS();
		innerDestoryConfig();
		isDestoryed.set(true);
	}

	private void innerDestoryDS() {
		HikariDataSource destoryDS = hikariDataSource;
		if (null != destoryDS) {
			destoryDS.close();
			destoryDS=null;
		}
	}

	private void innerDestoryConfig() {
		HikariConfig destoryConfig = hikariConfig.get();
		hikariConfig.set(null);
		if (null != destoryConfig) {
			destoryConfig = null;
		}
	}
	
}

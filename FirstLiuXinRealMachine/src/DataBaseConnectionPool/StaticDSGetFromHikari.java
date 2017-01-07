package DataBaseConnectionPool;

import com.zaxxer.hikari.HikariDataSource;

/*
 * thread-safe
 */

public class StaticDSGetFromHikari {
	public static final HikariDataSource HIKARI_DS=LiuXinHikariDS.createHikariDS().getHikariDataSource();
}

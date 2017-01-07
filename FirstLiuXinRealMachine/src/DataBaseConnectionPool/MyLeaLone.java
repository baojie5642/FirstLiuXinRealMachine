package DataBaseConnectionPool;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class MyLeaLone {

	public static void main(String[] args) throws Exception {
		String url = "jdbc:lealone:tcp://127.0.0.1:5210/test";
		Connection conn = DriverManager.getConnection(url, "lealone", "Hide");
		Statement stmt = conn.createStatement();

		stmt.executeUpdate("CREATE TABLE IF NOT EXISTS testLiuXin (f1 int primary key, f2 long)");
		stmt.executeUpdate("INSERT INTO testLiuXin(f1, f2) VALUES(3, 7)");
		stmt.executeUpdate("UPDATE testLiuXin SET f2 = 2 WHERE f1 = 1");
		ResultSet rs = stmt.executeQuery("SELECT * FROM testLiuXin");
		while (rs.next()) {
			System.out.println("f1=" + rs.getInt(1) + " f2=" + rs.getLong(2));
			System.out.println();
		}
		rs.close();
		//stmt.executeUpdate("DELETE FROM testLiuXin WHERE f1 = 1");
		stmt.executeUpdate("DROP TABLE IF EXISTS test");
		stmt.close();
		conn.close();
	}

}

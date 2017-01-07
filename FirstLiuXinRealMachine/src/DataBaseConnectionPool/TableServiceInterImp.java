package DataBaseConnectionPool;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.apache.commons.io.FileUtils;

public class TableServiceInterImp implements TableServiceInter {

	PreparedStatement pstm = null;
	ResultSet rs = null;
	ResultSetMetaData rsmd = null;

	@Override
	public void table2Txt(String taskName, Connection connection, String table) {
		// TODO Auto-generated method stub
		try {
			// 定义txt存储位置: taskName文件夹下,table名+日期.txt
			String filePath = "/home/liuxin/worker/dbtable2txt" + File.separator + taskName + "_" + table + ".txt";
			int columnCount;
			String content = "";

			String sql = "select * from " + table;
			pstm = connection.prepareStatement(sql);
			rs = pstm.executeQuery();
			rsmd = rs.getMetaData();
			rsmd = rs.getMetaData();
			columnCount = rsmd.getColumnCount();

			while (rs.next()) {// 获取每行记录的字段值，各行记录间换行，一条记录内的个字段值用","分隔
				int i = 1;
				if (rs != null) {
					for (; i < columnCount; i++) {
						content += rs.getString(i) + ",";
						// System.out.println(rs.getString(i));
					}
					content += rs.getString(i);// 最后一个字段值后面，不存在","
					content += "\r\n";
				}
			}
			content2Txt(filePath, content);// 向txt文件中写入数据，是在原文件中的数据后，写入
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (connection != null)
				try {
					connection.close();
					connection = null;
				} catch (SQLException e) {
					e.printStackTrace();
				}
		}
	}

	@Override
	public void txt2Table(String taskName, Connection connection, String table) {
		// 定义txt存储位置: taskName文件夹下,table名+日期.txt
		String filePath = File.separator + "home" + File.separator + "liuxin" + File.separator + "worker"
				 + File.separator + "dbtable2txt" + File.separator + taskName + "_" + table
				+ ".txt";
		try {
			String sql = "load data infile '" + filePath + "' replace into table " + table + " character set"
					+ " utf8 fields terminated by ',' lines terminated by '\\r\\n'";// 关于sql语句中，变量的引入语法，还有待研究
			System.out.println(sql);
			pstm = connection.prepareStatement(sql);
			pstm.execute();
			connection.close();
			connection = null;
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (null != connection)
				try {
					connection.close();
					connection = null;
				} catch (SQLException e) {
					e.printStackTrace();
				}
		}
	}

	/**
	 * 向txt文件中写入数据，是在原文件中的数据后，写入
	 * 
	 * @param filePath
	 * @param content
	 */
	public void content2Txt(String filePath, String content) {

		
		try {
			FileUtils.writeStringToFile(new File(filePath), content, "utf8");
		} catch (IOException e) {
			
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		String str = new String(); // 原有txt内容
//		String s1 = new String();// 内容更新
//		BufferedReader input = null;
//		BufferedWriter output = null;
//		try {
//			File file = new File(filePath);
//			if (!file.exists()) {
//				file.createNewFile();
//			}
//			input = new BufferedReader(new FileReader(file));
//			while ((str = input.readLine()) != null) {
//				s1 += str + "\r\n";// 定位到file的最后一行
//			}
//			input.close();
//			s1 += content;
//			output = new BufferedWriter(new FileWriter(file));
//			output.write(content);// 从file的最后一行开始，把content写入file
//			output.flush();
//			output.close();
//			output = null;
//		} catch (Exception e) {
//			e.printStackTrace();
//		} finally {
//			try {
//				if (null != output) {
//					output.close();
//					output = null;
//				}
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
	}
}

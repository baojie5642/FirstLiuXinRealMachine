package TaskRunner;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.zaxxer.hikari.HikariDataSource;

import DataBaseConnectionPool.StaticDSGetFromHikari;
import QueueOfAllTask.StaticQueueForAutoMachine;
import StampOfTaskInformation.RealSourceForTask;
import StampOfTaskInformation.LineStamp;

public class ExtractTaskRunner implements Runnable {
	private final LineStamp stampOfOneLineInTheTask;
	private final int whichThreadInLineStamp;
	private static final Log LOG = LogFactory.getLog(ExtractTaskRunner.class);
	

	public static ExtractTaskRunner init(final LineStamp stampOfOneLineInTheTask, final int whichThreadInLineStamp) {
		ExtractTaskRunner extractTaskRunner = new ExtractTaskRunner(stampOfOneLineInTheTask, whichThreadInLineStamp);
		return extractTaskRunner;
	}

	private ExtractTaskRunner(final LineStamp stampOfOneLineInTheTask, final int whichThreadInLineStamp) {
		super();
		this.stampOfOneLineInTheTask = stampOfOneLineInTheTask;
		this.whichThreadInLineStamp = whichThreadInLineStamp;
	}

	@Override
	public void run() {
		String taskName = null;
		String tableNameString = null;
		boolean isNeedTransfer = false;
		Connection connection = null;
		try {
			taskName = stampOfOneLineInTheTask.getTaskName();
			tableNameString = getExtractTableName();
			isNeedTransfer = isNeedTransfer();
			connection = StaticDSGetFromHikari.HIKARI_DS.getConnection();
			for (; null == connection;) {
				connection = StaticDSGetFromHikari.HIKARI_DS.getConnection();
				LockSupport.parkNanos(TimeUnit.NANOSECONDS.convert(3, TimeUnit.SECONDS));
			}
			table2Txt(taskName, connection, tableNameString);
			if (stampOfOneLineInTheTask.isOneThreadExtractFinishedByLock()) {
				// if (isNeedTransfer) {
				// StaticQueueForAutoMachine.TRANS_QUEUE_FROM_EXT.addLineStamp(stampOfOneLineInTheTask);
				// } else {
				StaticQueueForAutoMachine.EXP_QUEUE_FROM_TRANS.addLineStamp(stampOfOneLineInTheTask);
				// }
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (null != taskName) {
				taskName = null;
			}
			try {
				if (null != connection) {
					if (!connection.isClosed()) {
						connection.close();
						connection = null;
					} else {
						connection = null;
					}
				}
			} catch (SQLException e2) {
				e2.printStackTrace();
			}

		}
	}

	private void table2Txt(final String taskName, Connection connection, final String tableName) {
		PreparedStatement pstm = null;
		ResultSet rs = null;
		ResultSetMetaData rsmd = null;
		File tempFile = null;
		String filePath = null;
		String sql = null;
		String content = "";
		try {
			filePath = stampOfOneLineInTheTask.getWriteTxtDir() + File.separator + taskName + File.separator
					+ "linestamp" + stampOfOneLineInTheTask.getWhichLineInTask();
			tempFile = new File(filePath);
			tempFile.setWritable(true, false);
			if (!tempFile.exists()) {
				tempFile.mkdirs();
			}
				stampOfOneLineInTheTask.setFileFullPath(filePath);
		
			filePath = filePath + File.separator + "thread" + "-" + whichThreadInLineStamp + ".txt";
			int columnCount;

			sql = "select * from " + tableName;
			pstm = connection.prepareStatement(sql, ResultSet.CONCUR_READ_ONLY);
			rs = pstm.executeQuery();
			rsmd = rs.getMetaData();
			columnCount = rsmd.getColumnCount();
			while (rs.next()) {// 获取每行记录的字段值，各行记录间换行，一条记录内的个字段值用","分隔
				int i = 1;
				if (rs != null) {
					for (; i < columnCount; i++) {
						content += rs.getString(i) + ",";
					}
					content += rs.getString(i);// 最后一个字段值后面，不存在","
					content += "\r\n";
				}
			}
			content2Txt(filePath, content);// 向txt文件中写入数据，是在原文件中的数据后，写入
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (null != content) {
				content = null;
			}
			if (null != tempFile) {
				tempFile = null;
			}
			if (null != rsmd) {
				rsmd = null;
			}
			try {
				if (null != rs) {
					rs.close();
					rs = null;
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			try {
				if (null != pstm) {
					pstm.close();
					pstm = null;
				}
			} catch (SQLException e2) {
				e2.printStackTrace();
			}
		}
	}

	private void content2Txt(final String filePath, final String content) {
		File tempFile = null;
		try {
			try {
				tempFile = new File(filePath);
				tempFile.setWritable(true, false);
				FileUtils.writeStringToFile(tempFile, content, "utf8");
			} finally {
				if (null != tempFile) {
					tempFile = null;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private boolean isNeedTransfer() {
		return stampOfOneLineInTheTask.isNeedTransfer();
	}

	private String getExtractTableName() {
		RealSourceForTask realSourceForTask = null;
		String name = null;
		try {
			realSourceForTask = stampOfOneLineInTheTask.getRealSourceForTask();
			name = realSourceForTask.getExtratcTableNameString();
		} finally {
			if (null != realSourceForTask)
				realSourceForTask = null;
		}
		return name;
	}

	public static void main(String args[]) {
		String string="\"\' ";
		System.out.println(string);
 	}
}

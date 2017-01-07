package TaskRunner;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.zaxxer.hikari.HikariDataSource;

import DataBaseConnectionPool.StaticDSGetFromHikari;
import QueueOfAllTask.LineStampQueue;
import StampOfTaskInformation.RealSourceForTask;
import StampOfTaskInformation.LineStamp;

public class ExportTaskRunner implements Runnable {
	private static final Log LOG = LogFactory.getLog(ExportTaskRunner.class);
	private final LineStamp stampOfAllTask;
	private final int whichThreadInLineStamp;
	private volatile String testFileIsNULL;

	private ExportTaskRunner(final LineStamp stampOfAllTask, final int whichThreadInLineStamp) {
		super();
		this.stampOfAllTask = stampOfAllTask;
		this.whichThreadInLineStamp = whichThreadInLineStamp;
	}

	public static ExportTaskRunner init(final LineStamp stampOfAllTask, final int whichThreadInLineStamp) {
		ExportTaskRunner exportTaskRunner = new ExportTaskRunner(stampOfAllTask, whichThreadInLineStamp);
		return exportTaskRunner;
	}

	@Override
	public void run() {
		String taskNameString = null;
		String tableNameString = null;
		Connection connection = null;
		try {
			taskNameString = stampOfAllTask.getTaskName();
			tableNameString = getExportRealTableName();
			connection = StaticDSGetFromHikari.HIKARI_DS.getConnection();
			for (; null == connection;) {
				connection = StaticDSGetFromHikari.HIKARI_DS.getConnection();
				LockSupport.parkNanos(TimeUnit.NANOSECONDS.convert(3, TimeUnit.SECONDS));
			}
			txt2Table(connection, tableNameString);
			if (stampOfAllTask.isOneThreadExportFinishedByLock()) {
				stampOfAllTask.releaseOneSemaphore();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (null != taskNameString) {
				taskNameString = null;
			}
			if (null != tableNameString) {
				tableNameString = null;
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

	private void txt2Table(Connection connection, final String tableNameString) {
		PreparedStatement preparedStatement = null;
		final int batchSize = 14;
		// 其实可以复用下面的txtFileParentPathString
		String tempString = null;
		String temp1[] = null;
		String txtFileParentPathString = null;
		File file[] = null;
		File tempFile = null;
		File tempFile2 = null;
		String oneLineRead[] = null;
		ArrayList<String> listStrings = new ArrayList<>();
		int howManyThreadInLineStamp = stampOfAllTask.getExportThreadNum();
		int listSize = 0;
		int j = 0;
		int re = 0;
		try {
			txtFileParentPathString = stampOfAllTask.getFileFullPath();
			if ("NULL".equals(txtFileParentPathString)) {
				LOG.info("数据库输出时读取磁盘文件路径为： NULL ，出错返回 ……");
				return;
			}
			for (; ((re != howManyThreadInLineStamp) && (re < howManyThreadInLineStamp));) {
				tempFile = new File(txtFileParentPathString);
				if (tempFile.isDirectory()) {
					file = tempFile.listFiles();
				} else {
					LOG.info("exprot未找到要读取的文件夹……");
				}
				re = file.length;
				if (re != howManyThreadInLineStamp && re < howManyThreadInLineStamp) {
					// System.out.println(re);
					// System.out.println("find : " +
					// tempFile.getAbsolutePath());
					// System.out.println("find : " + tempFile.getParent());
					// System.out.println("whichThreadInLineStamp : " +
					// whichThreadInLineStamp);
					// System.out.println("whichLine : " +
					// stampOfAllTask.getWhichLineInTask());
					// System.out.println("静态类加载流时的初始化问题");
					LockSupport.parkNanos(TimeUnit.NANOSECONDS.convert(1, TimeUnit.SECONDS));
					// Thread.currentThread().yield();
					// System.out.println("线程推辞一次……看一看……");
				}
			}
			for (int i = 0; i < re; i++) {
				tempString = file[i].getName();
				temp1 = tempString.split("-");
				if (temp1[1].startsWith(String.valueOf(whichThreadInLineStamp))) {
					testFileIsNULL = file[i].getAbsolutePath();
				}
			}
			// System.out.println(testFileIsNULL);
			tempFile2 = new File(testFileIsNULL);
			if (tempFile2.isFile()) {
				listStrings = (ArrayList<String>) FileUtils.readLines(tempFile2, "utf8");
				listSize = listStrings.size();
			}
			preparedStatement = connection
					.prepareStatement("insert into "
							+ tableNameString
							+ " (agename,name,numname,cdma,ttd,wcdma,gms,qwe,qqwe,wwer,eerw,eewq,asde,qqsd,eewqs,qweqwed,ddfre,ssedw,ffer) values(?, ?, ?,?, ?, ?, ?,?, ?, ?, ?,?, ?, ?, ?,?, ?, ?, ?)");
			connection.setAutoCommit(false);
			preparedStatement.clearBatch();
			for (int k = 0; k < listSize; k++) {
				++j;
				oneLineRead = listStrings.get(k).split(",");
				preparedStatement.setString(1, oneLineRead[0]);
				preparedStatement.setString(2, oneLineRead[1]);
				preparedStatement.setString(3, oneLineRead[2]);
				preparedStatement.setString(4, oneLineRead[3]);
				preparedStatement.setString(5, oneLineRead[4]);
				preparedStatement.setString(6, oneLineRead[5]);
				preparedStatement.setString(7, oneLineRead[6]);
				preparedStatement.setString(8, oneLineRead[7]);
				preparedStatement.setString(9, oneLineRead[8]);
				preparedStatement.setString(10, oneLineRead[9]);
				preparedStatement.setString(11, oneLineRead[10]);
				preparedStatement.setString(12, oneLineRead[11]);
				preparedStatement.setString(13, oneLineRead[12]);
				preparedStatement.setString(14, oneLineRead[13]);
				preparedStatement.setString(15, oneLineRead[14]);
				preparedStatement.setString(16, oneLineRead[15]);
				preparedStatement.setString(17, oneLineRead[16]);
				preparedStatement.setString(18, oneLineRead[17]);
				preparedStatement.setString(19, oneLineRead[18]);
				// preparedStatement.setString(20, oneLineRead[19]);
				preparedStatement.addBatch();
				if ((j + 1) % batchSize == 0) {
					preparedStatement.executeBatch();
				}
			}
			preparedStatement.executeBatch();
			connection.setAutoCommit(true);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (null != tempFile2) {
				tempFile2 = null;
			}
			if (tempFile != null) {
				tempFile = null;
			}
			if (null != file) {
				for (int k = 0; k < file.length; k++) {
					file[k] = null;
				}
				file = null;
			}
			if (null != oneLineRead) {
				for (int p = 0; p < oneLineRead.length; p++) {
					oneLineRead[p] = null;
				}
				oneLineRead = null;
			}
			if (null != temp1) {
				for (int q = 0; q < temp1.length; q++) {
					temp1[q] = null;
				}
				temp1 = null;
			}
			try {
				if (null != preparedStatement) {
					preparedStatement.close();
					preparedStatement = null;
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			if (null != listStrings) {
				listStrings.clear();
				listStrings = null;
			}
		}
	}

	private String getExportRealTableName() {
		RealSourceForTask realSourceForTask = null;
		String tableNameString = null;
		try {
			realSourceForTask = stampOfAllTask.getRealSourceForTask();
			tableNameString = realSourceForTask.getExportTableNameString();
		} finally {
			if (null != realSourceForTask) {
				realSourceForTask = null;
			}
		}
		return tableNameString;
	}
}

package FieldStructure;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LiuPreparedStatement {

	public static void liuPrepaStatem(final PreparedStatement preparedStatement,
			final WriteToDiskOfOneLine writeToDiskOfOneLine, final Map<String, Integer> columnLocation,
			final ConcurrentHashMap<String, ColumnInLine> columnsConcurrentHashMap) {
		List<WriteToDiskOfOneColumn> columnsList = writeToDiskOfOneLine.getColumnListToWriteDisk();
		int size = columnsList.size();
		if (size <= 0) {
			System.out.println("该条记录中，没有任何字段，出现了某种错误");
			return;
		} else {
			for (int i = 0; i < size; i++) {
				WriteToDiskOfOneColumn writeToDiskOfOneColumn = columnsList.get(i);
				String columnName = writeToDiskOfOneColumn.getColumnName();
				String javaClassName = columnsConcurrentHashMap.get(columnName).getSqlTypeSwapToJavaClassName()
						.toLowerCase();
				int location = columnLocation.get(columnName).intValue();
				if (java.lang.String.class.getName().contains(javaClassName)) {
					String valueInner = (String) writeToDiskOfOneColumn.getValue();
					try {
						preparedStatement.setString(location, valueInner);
					} catch (SQLException e) {
						e.printStackTrace();
					}
				} else if (byte[].class.getName().contains(javaClassName)) {
					byte[] bytes = (byte[]) writeToDiskOfOneColumn.getValue();
					try {
						preparedStatement.setBytes(location, bytes);
					} catch (SQLException e) {
						e.printStackTrace();
					}
				} else if (java.lang.Boolean.class.getName().contains(javaClassName)) {
					Boolean bol = (Boolean) writeToDiskOfOneColumn.getValue();
					try {
						preparedStatement.setBoolean(location, bol.booleanValue());
					} catch (SQLException e) {
						e.printStackTrace();
					}
				} else if (java.lang.Long.class.getName().contains(javaClassName)) {
					Long lo = (Long) writeToDiskOfOneColumn.getValue();
					try {
						preparedStatement.setLong(location, lo.longValue());
					} catch (SQLException e) {
						e.printStackTrace();
					}
				} else if (java.sql.Timestamp.class.getName().contains(javaClassName)) {
					Timestamp timestamp = (Timestamp) writeToDiskOfOneColumn.getValue();
					try {
						preparedStatement.setTimestamp(location, timestamp);
					} catch (SQLException e) {
						e.printStackTrace();
					}
				} else if (java.sql.Date.class.getName().contains(javaClassName)) {
					Date date = (Date) writeToDiskOfOneColumn.getValue();
					try {
						preparedStatement.setDate(location, date);
					} catch (SQLException e) {
						e.printStackTrace();
					}
				} else if (java.lang.Double.class.getName().contains(javaClassName)) {
					Double double1 = (Double) writeToDiskOfOneColumn.getValue();
					try {
						preparedStatement.setDouble(location, double1.doubleValue());
					} catch (SQLException e) {
						e.printStackTrace();
					}
				} else if (java.math.BigDecimal.class.getName().contains(javaClassName)) {
					BigDecimal bigDecimal = (BigDecimal) writeToDiskOfOneColumn.getValue();
					try {
						preparedStatement.setBigDecimal(location, bigDecimal);
					} catch (SQLException e) {
						e.printStackTrace();
					}
				} else if (java.lang.Integer.class.getName().contains(javaClassName)) {
					Integer integer = (Integer) writeToDiskOfOneColumn.getValue();
					try {
						preparedStatement.setInt(location, integer.intValue());
					} catch (SQLException e) {
						e.printStackTrace();
					}
				} else if (java.sql.Time.class.getName().contains(javaClassName)) {
					Time time = (Time) writeToDiskOfOneColumn.getValue();
					try {
						preparedStatement.setTime(location, time);
					} catch (SQLException e) {
						e.printStackTrace();
					}
				} else if (java.sql.Date.class.getName().contains(javaClassName)) {
					Date date = (Date) writeToDiskOfOneColumn.getValue();
					try {
						preparedStatement.setDate(i, date);
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

}

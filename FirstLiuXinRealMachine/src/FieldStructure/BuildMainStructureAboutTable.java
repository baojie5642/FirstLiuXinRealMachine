package FieldStructure;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;

public class BuildMainStructureAboutTable {

	private BuildMainStructureAboutTable() {

	}

	public static MainStrucOfTable buildMainStruc(final Connection connection, final String tableName) {
		checkNullAndEmpty(connection, tableName);
		KeysAboutTable keysAboutTable = BuildLiuXinKeysAboutTable.buildKeysAboutTable(connection, tableName);
		if (null == keysAboutTable) {
			throw new NullPointerException();
		}
		PreparedStatement ps = null;
		ResultSet resultSet = null;
		ResultSetMetaData resultSetMetaData = null;
		try {
			ps = makePreparedStatement(connection, tableName);
			resultSet = makeResultSet(ps);
			resultSetMetaData = makeResultSetMetaData(resultSet);
			int columnNumInTable = -7;
			columnNumInTable = getColumnNumInTable(resultSetMetaData);
			if (columnNumInTable <= 0) {
				System.out.println("从表中获取字段的数量不大于0，这应该是出现了某种错误，直接返回null");
				return null;
			} else {
				ConcurrentHashMap<String, ColumnInLine> oneLineInTable = makeTheColumnsWhichInTable(columnNumInTable,
						resultSetMetaData, keysAboutTable);
				MainStrucOfTable mainStrucOfTable = MainStrucOfTable
						.createMainStrucOfTable(keysAboutTable, oneLineInTable);
				return mainStrucOfTable;
			}
		} finally {
			if (null != resultSetMetaData) {
				resultSetMetaData = null;
			}
			closeResultSet(resultSet);
			if (null != resultSet) {
				resultSet = null;
			}
			closePreparedStatement(ps);
			if (null != ps) {
				ps = null;
			}
		}
	}

	private static ConcurrentHashMap<String, ColumnInLine> makeTheColumnsWhichInTable(final int howManyColumnsInTable,
			final ResultSetMetaData resultSetMetaData, final KeysAboutTable keysAboutTable) {
		ConcurrentHashMap<String, ColumnInLine> oneLineInTable = new ConcurrentHashMap<String, ColumnInLine>();
		String columnName = "";
		String sqlTypeSwapToJavaClassName = "";
		String sqlDataTypeName = "";
		int columnLegth = -1;
		int jdbcTypeAlsoJavaSqlType = -3276;
		for (int i = 1; i <= howManyColumnsInTable; i++) {
			columnName = getColumnName(resultSetMetaData, i);
			sqlTypeSwapToJavaClassName = getColumnClassName(resultSetMetaData, i);
			sqlDataTypeName = getColumnTypeName(resultSetMetaData, i);
			columnLegth = getColumnDisplaySize(resultSetMetaData, i);
			jdbcTypeAlsoJavaSqlType = getColumnType(resultSetMetaData, i);
			initAColumnAndPutIntoMap(columnName, sqlTypeSwapToJavaClassName, sqlDataTypeName, columnLegth,
					jdbcTypeAlsoJavaSqlType, keysAboutTable, oneLineInTable,i);
		}
		return oneLineInTable;
	}

	private static void initAColumnAndPutIntoMap(final String columnName, final String sqlTypeSwapToJavaClassName,
			final String sqlDataTypeName, final int columnLegth, final int jdbcTypeAlsoJavaSqlType,
			final KeysAboutTable keysAboutTable, final ConcurrentHashMap<String, ColumnInLine> oneLineInTable,final int rowNum) {
		boolean existAKey = false;
		int isPriKey = 0;
		int isExpKey = 0;
		existAKey = TellWhichKey.isAnyKeyInTable(keysAboutTable);
		if (existAKey) {
			isExpKey = TellWhichKey.isThisColumnExportedKey(columnName, keysAboutTable);
			isPriKey = TellWhichKey.isThisColumnPrimaryKey(columnName, keysAboutTable);
			ColumnInLine columnInLine = ColumnInLine.createColumnForLiuXin(columnName, sqlDataTypeName,
					jdbcTypeAlsoJavaSqlType, sqlTypeSwapToJavaClassName, existAKey, isPriKey, isExpKey, columnLegth,rowNum);
			oneLineInTable.putIfAbsent(columnName, columnInLine);
		} else {
			ColumnInLine columnInLine = ColumnInLine.createColumnForLiuXin(columnName, sqlDataTypeName,
					jdbcTypeAlsoJavaSqlType, sqlTypeSwapToJavaClassName, existAKey, columnLegth,rowNum);
			oneLineInTable.putIfAbsent(columnName, columnInLine);
		}
	}

	private static PreparedStatement makePreparedStatement(final Connection connection, final String tableName) {
		PreparedStatement ps = null;
		try {
			ps = connection.prepareStatement("select * from " + tableName + " where 1=2");
		} catch (SQLException e) {
			System.out.println("执行 connection.prepareStatement(select * from +tableName+ where 1=2)时出错");
			e.printStackTrace();
		}
		if (null == ps) {
			throw new NullPointerException();
		}
		return ps;
	}

	private static ResultSet makeResultSet(final PreparedStatement ps) {
		ResultSet resultSet = null;
		try {
			resultSet = ps.executeQuery();
		} catch (SQLException e) {
			System.out.println("执行resultSet=ps.executeQuery()时出错");
			e.printStackTrace();
		}
		if (null == resultSet) {
			throw new NullPointerException();
		}
		return resultSet;
	}

	private static ResultSetMetaData makeResultSetMetaData(final ResultSet resultSet) {
		ResultSetMetaData resultSetMetaData = null;
		try {
			resultSetMetaData = resultSet.getMetaData();
		} catch (SQLException e) {
			System.out.println("执行resultSetMetaData=resultSet.getMetaData()时出错");
			e.printStackTrace();
		}
		if (null == resultSetMetaData) {
			throw new NullPointerException();
		}
		return resultSetMetaData;
	}

	private static int getColumnNumInTable(final ResultSetMetaData resultSetMetaData) {
		int columnNumInTable = -9;
		try {
			columnNumInTable = resultSetMetaData.getColumnCount();
		} catch (SQLException e) {
			System.out.println("在resultSetMetaData.getColumnCount()中获取字段数量时出错");
			e.printStackTrace();
		}
		return columnNumInTable;
	}

	private static String getColumnName(final ResultSetMetaData resultSetMetaData, final int num) {
		String columnName = "";
		try {
			columnName = resultSetMetaData.getColumnName(num);
		} catch (SQLException e) {
			System.out.println("resultSetMetaData.getColumnName(i)出错");
			e.printStackTrace();
		}
		return columnName;
	}

	private static String getColumnClassName(final ResultSetMetaData resultSetMetaData, final int num) {
		String sqlTypeSwapToJavaClassName = "";
		try {
			sqlTypeSwapToJavaClassName = resultSetMetaData.getColumnClassName(num);
		} catch (SQLException e) {
			System.out.println("resultSetMetaData.getColumnClassName(num)出错");
			e.printStackTrace();
		}
		return sqlTypeSwapToJavaClassName;
	}

	private static String getColumnTypeName(final ResultSetMetaData resultSetMetaData, final int num) {
		String sqlDataTypeName = "";
		try {
			sqlDataTypeName = resultSetMetaData.getColumnTypeName(num);
		} catch (SQLException e) {
			System.out.println("resultSetMetaData.getColumnTypeName(num)出错");
			e.printStackTrace();
		}
		return sqlDataTypeName;
	}

	private static int getColumnDisplaySize(final ResultSetMetaData resultSetMetaData, final int num) {
		int columnLegth = -7;
		try {
			columnLegth = resultSetMetaData.getColumnDisplaySize(num);
		} catch (SQLException e) {
			System.out.println("resultSetMetaData.getColumnDisplaySize(num)出错");
			e.printStackTrace();
		}
		return columnLegth;
	}

	private static int getColumnType(final ResultSetMetaData resultSetMetaData, final int num) {
		int jdbcTypeAlsoJavaSqlType = -3276;
		try {
			jdbcTypeAlsoJavaSqlType = resultSetMetaData.getColumnType(num);
		} catch (SQLException e) {
			System.out.println("resultSetMetaData.getColumnType(num)出错");
			e.printStackTrace();
		}
		return jdbcTypeAlsoJavaSqlType;
	}

	private static void checkNullAndEmpty(final Connection connection, final String tableName) {
		if (null == connection) {
			throw new NullPointerException();
		}
		if (null == tableName) {
			throw new NullPointerException();
		}
		if (tableName.length() == 0) {
			throw new IllegalStateException();
		}
		final String inner = tableName.trim();
		if ("".equals(inner) || " ".equals(inner)) {
			throw new IllegalStateException();
		}
	}

	private static void closeResultSet(final ResultSet resultSet) {
		if (null != resultSet) {
			try {
				resultSet.close();
			} catch (SQLException e) {
				System.out.println("关闭resultSet.close()时出错，忽略掉，然后置null");
				e.printStackTrace();
			}
		}
	}

	private static void closePreparedStatement(final PreparedStatement preparedStatement) {
		if (null != preparedStatement) {
			try {
				preparedStatement.close();
			} catch (SQLException e) {
				System.out.println("关闭preparedStatement.close()时出错，忽略掉，然后置null");
				e.printStackTrace();
			}
		}
	}
}

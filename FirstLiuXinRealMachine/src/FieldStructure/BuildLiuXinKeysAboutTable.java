package FieldStructure;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BuildLiuXinKeysAboutTable {
	
	private BuildLiuXinKeysAboutTable() {

	}

	public static KeysAboutTable buildKeysAboutTable(final Connection connection, final String tableName) {
		checkNullAndEmpty(connection, tableName);
		KeysAboutTable sourceTableStructure = null;
		List<String> priKeyNames = null;
		List<String> expKeyNames = null;
		DatabaseMetaData databaseMetaData = null;
		ResultSet resultSetForPriKeys = null;
		ResultSet resultSetForExpKeys = null;
		boolean isHasPriKey = false;
		boolean isHasExpKey = false;
		try {
			databaseMetaData = makeDatabaseMetaData(connection);
			resultSetForPriKeys = makeResultSetForPriOrExpKey(databaseMetaData, tableName, true);
			resultSetForExpKeys = makeResultSetForPriOrExpKey(databaseMetaData, tableName, false);
			isHasPriKey = isHasDataInResultSet(resultSetForPriKeys);
			priKeyNames = makeKeys0(isHasPriKey, resultSetForPriKeys, true);
			isHasExpKey = isHasDataInResultSet(resultSetForExpKeys);
			expKeyNames = makeKeys0(isHasExpKey, resultSetForExpKeys, false);
			sourceTableStructure = KeysAboutTable.createSourceTableStructure(tableName,isHasPriKey, priKeyNames,
					isHasExpKey, expKeyNames);
		} finally {
			closeResult(resultSetForPriKeys);
			closeResult(resultSetForExpKeys);
			if(null!=resultSetForExpKeys){
				resultSetForExpKeys=null;
			}
			if(null!=resultSetForPriKeys){
				resultSetForPriKeys=null;
			}
			if (null != databaseMetaData) {
				databaseMetaData = null;
			}
		}
		return sourceTableStructure;
	}

	private static void checkNullAndEmpty(final Connection connection, final String tableName) {
		if (null == connection) {
			throw new NullPointerException();
		}
		if (null == tableName) {
			throw new NullPointerException();
		}
		if (tableName.length() == 0 || "".equals(tableName) || " ".equals(tableName)) {
			throw new IllegalStateException();
		}
	}

	private static DatabaseMetaData makeDatabaseMetaData(final Connection connection) {
		DatabaseMetaData databaseMetaData = null;
		try {
			databaseMetaData = connection.getMetaData();
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("从Connection获取DatabaseMetaData失败");
		}
		return databaseMetaData;
	}

	private static ResultSet makeResultSetForPriOrExpKey(final DatabaseMetaData databaseMetaData,
			final String tableName, final boolean isPrimaryKey) {
		ResultSet resultSetForPriOrExp = null;
		try {
			if (isPrimaryKey) {
				resultSetForPriOrExp = databaseMetaData.getPrimaryKeys(null, null, tableName);
			} else {
				resultSetForPriOrExp = databaseMetaData.getExportedKeys(null, null, tableName);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("从DatabaseMetaData获取ResultSetForPrimaryOrExportKey失败");
		}
		return resultSetForPriOrExp;
	}

	private static boolean isHasDataInResultSet(final ResultSet resultSet) {
		if (checkAfterLast(resultSet)) {
			return false;
		} else {
			return isHasData(resultSet);
		}
	}

	private static boolean isHasData(final ResultSet resultSet) {
		boolean isHasDataInResultSet = true;
		try {
			isHasDataInResultSet = resultSet.next();
		} catch (SQLException e) {
			// 这里的判断有些问题，要明确是没有数据还是出错
			System.out.println("判断结果集中是否有数据时出错,返回false");
			if (isHasDataInResultSet) {
				isHasDataInResultSet = false;
			}
			e.printStackTrace();
		}
		return isHasDataInResultSet;
	}

	private static boolean checkAfterLast(final ResultSet resultSet) {
		boolean isAfterLast = true;
		try {
			isAfterLast = resultSet.isAfterLast();
		} catch (SQLException e) {
			System.out.println("检测resultSet.isAfterLast()时出错");
			e.printStackTrace();
		}
		return isAfterLast;
	}

	@SuppressWarnings("unchecked")
	private static List<String> makeKeys0(final boolean isHasPriKey, final ResultSet resultSetForPriOrExp,
			final boolean isPrimaryKey) {
		List<String> keysName = null;
		if (isHasPriKey) {
			if (isPrimaryKey) {
				keysName = makeKeys(resultSetForPriOrExp, true);
			} else {
				keysName = makeKeys(resultSetForPriOrExp, false);
			}
		} else {
			keysName = Collections.EMPTY_LIST;
		}
		return keysName;
	}

	private static List<String> makeKeys(final ResultSet resultSetForPriOrExpKey, final boolean isPrimaryKey) {
		List<String> keys = new ArrayList<String>();
		String keyName = null;
		do {
			keyName = getPriOrExpKeyNameFromResultSet(resultSetForPriOrExpKey, isPrimaryKey);
			if (null != keyName) {
				keys.add(keyName);
			}
		} while (isHasDataInResultSet(resultSetForPriOrExpKey));
		return keys;
	}

	private static String getPriOrExpKeyNameFromResultSet(final ResultSet resultSetForPriOrExpKey,
			final boolean isPrimaryKey) {
		String primaryOrExportKeyName = null;
		try {
			if (isPrimaryKey) {
				primaryOrExportKeyName = resultSetForPriOrExpKey.getString("COLUMN_NAME");
			} else {
				primaryOrExportKeyName = resultSetForPriOrExpKey.getString("FKCOLUMN_NAME");
			}
		} catch (SQLException e) {
			System.out.println("从resultSetForPrimaryOrExportKey中获取主键或者外键名称失败");
			e.printStackTrace();
		}
		return primaryOrExportKeyName;
	}

	private static void closeResult(final ResultSet resultSet) {
		if (null != resultSet) {
			try {
				resultSet.close();
			} catch (SQLException e) {
				e.printStackTrace();
				System.out.println("buildtablestruc时关闭result出错");
			}
		}
	}
}

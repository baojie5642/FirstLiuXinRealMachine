package FieldStructure;

public class ColumnInLine {

	// 至此，
	// 通过以上两段代码，我们把

	// 1.字段名
	// 2.字段的SQL数据类型
	// 3.SQL数据类型的名称
	// /4. 该字段数据的Java类型

	// 的属性值整合到了Field对象中
	// 。这为下一步根据条件判断实现通用的PreparedStatement赋值方法奠定了基础。

	// 字段名称,System.out.println( rsmd.getColumnName(1));
	private final String columnName;

	// 字段所对应的sql数据类型名称,ResultSetMetaData rsmd,rsmd.getColumnTypeName(1)
	private final String sqlDataTypeName;

	// 表里面的字段个数，int columnCount = rsmd.getColumnCount();
	// private final int columnNumInTable;

	// jdbcType也就是sqlType类型,System.out.println( rsmd.getColumnType(1));
	private final int jdbcTypeAlsoJavaSqlType;

	// sql类型对应到java里面的类型，输出的是java里面类型的名称，System.out.println(rsmd.getColumnClassName(7));
	private final String sqlTypeSwapToJavaClassName;

	// 是否是主键或者外键
	private final boolean existAKey;

	// 该字段是否可以为空
	// private final boolean isCanNull;

	// 判断是否为这个字段是或否为主键
	private final int isPriKey;

	// 判断是否为外键
	private final int isExpKey;

	// 字段在数据库中的定义的长度
	private final int columnLegth;
//认为构造的rowNum
	private final int rowNum;
	
	private ColumnInLine(final String columnName, final String sqlDataTypeName, final int jdbcTypeAlsoJavaSqlType,
			final String sqlTypeSwapToJavaClassName, final boolean existAKey, final int isPriKey, final int isExpKey,
			final int columnLegth,final int rowNum) {
		super();
		this.columnName = columnName;
		this.sqlDataTypeName = sqlDataTypeName;
		this.jdbcTypeAlsoJavaSqlType = jdbcTypeAlsoJavaSqlType;
		this.sqlTypeSwapToJavaClassName = sqlTypeSwapToJavaClassName;
		this.existAKey = existAKey;
		this.isPriKey = isPriKey;
		this.isExpKey = isExpKey;
		this.columnLegth = columnLegth;
		this.rowNum=rowNum;
	}

	public static ColumnInLine createColumnForLiuXin(final String columnName, final String sqlDataTypeName,
			final int jdbcTypeAlsoJavaSqlType, final String sqlTypeSwapToJavaClassName, final boolean existAKey,
			final int isPriKey, final int isExpKey, final int columnLegth,final int rowNum) {
		ColumnInLine columnForLiuXin = new ColumnInLine(columnName, sqlDataTypeName, jdbcTypeAlsoJavaSqlType,
				sqlTypeSwapToJavaClassName, existAKey, isPriKey, isExpKey, columnLegth,rowNum);
		return columnForLiuXin;
	}

	public static ColumnInLine createColumnForLiuXin(final String columnName, final String sqlDataTypeName,
			final int jdbcTypeAlsoJavaSqlType, final String sqlTypeSwapToJavaClassName, final boolean existAKey,
			final int columnLegth,final int rowNum) {
		if (existAKey != false) {
			throw new IllegalStateException();
		}
		ColumnInLine columnForLiuXin = new ColumnInLine(columnName, sqlDataTypeName, jdbcTypeAlsoJavaSqlType,
				sqlTypeSwapToJavaClassName, existAKey, 0, 0, columnLegth,rowNum);
		return columnForLiuXin;
	}

	public String getColumnName() {
		return columnName;
	}

	public String getSqlDataTypeName() {
		return sqlDataTypeName;
	}

	public int getJdbcTypeAlsoJavaSqlType() {
		return jdbcTypeAlsoJavaSqlType;
	}

	public String getSqlTypeSwapToJavaClassName() {
		return sqlTypeSwapToJavaClassName;
	}

	public boolean isExistAKey() {
		return existAKey;
	}

	public int getIsPriKey() {
		return isPriKey;
	}

	public int getIsExpKey() {
		return isExpKey;
	}

	public int getColumnLegth() {
		return columnLegth;
	}

	public int getRowNum() {
		return rowNum;
	}

}

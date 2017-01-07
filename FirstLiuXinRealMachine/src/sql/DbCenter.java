/*
 * 2009-9-19 南陵
 * DbCenter、Table、Field三个类分别用于数据库连接和描述、数据库表描述及表的字段描述
 * 用于对HashMap关系数据映射技术的进一步完善
 * 本Db类侧重描述以下问题 a.数据库相关信息,如:事务,b.表的结构，c.字段的属性
 * 创建一些实用方法
 * Db的初始化，将依赖FileManager的路径设置，配置文件管理与应用策略:
 * 0、若path==null，则使用defaultPath = System.getProperty("user.dir")，其中user.dir是应用程序工作路径
 * 1、查找文件 //db.xml
 * 2、若不存在，则创建/config目录，并自动加载一个db.xml样板文件
 * 3、用户应当修改db.xml中的五条数据库配置信息，前四条与第五条，二者至少必填其一，使其符合实际情况
 * 4、重新启动应用系统，本系统加载这个db.xml文件
 * 5、本系统采用单例模式创建一个Db实例，供应用系统使用，如果db==null,则
 * 6、本系统根据这个db.xml文件创建数据库连接
 * 7、实例化Db对象db
 * 8、初始配置regex.xml文件
 * 9、regex.xml文件的生成，初始值为regex=""
 * 此类使用步骤
 *    如果使用用户自定义目录，则分两步操作
 *
 *        FileManager fm = dm;
 *        fm.setPath("用户指定存放配置文件夹路径");//**** 如:d:\\temp
 *        fm.createDbXML();
 *
 *        DbCenter.instance().init();//初次运行时,极可能会出现异常,请用户修改 "用户指定存放配置文件夹路径"/META-INF/db.xml文件的配置信息,正确配置该文件后,运行Db.instance().init();就会得到正确结果
 * 
 *    如果使用默认目录，则一步操作
 *    DbCenter.instance().init();
 *    此后均可获取数据库相关信息
 * @version DFree (pvo v2.0)
 * @since 2009-6-20
 * @author 胡开明
 * @see <a href='http://www.wnhot.com/DFree/' target="_blank">HashMap关系映射技术</a><br/>
 */
package sql;

import cn.jadepool.util.DateTool;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 修订日期：2012-10-19<br/> 新增项目：<br/> 1、private java.sql.Connection con;<br/>
 * 2、private DbCenter(Connection con);<br/>
 * 3、将单例模式修改为有上限多列模式（两个实例），以便于可能出现的两个数据库之间不同的数据交换<br/> 作用：<br/>
 * 提供两个Db实例，每个实例均提供数据库连接和数据库相关信息<br/> 测试情况：
 *
 * 目前在使用以下驱动的情况下通过 1、sqljdbc4.jar(Microsoft SQL Server JDBC Driver 3.0)
 * 2、mysql-connector-java-5.1.7-bin.jar(MySQL-AB JDBC Driver) 3、Apache Derby
 * Network Client JDBC Driver
 * Db负责管理数据库的结构信息，是pvo中的账房先生，用户（类）获取数据库的连接后必须先交给Db，Db提取数据库信息（记账）后将连接交换给用户,
 * 这是为用户得到正确的数据库结构及相关信息。用户（如ProcessVO、DbFree、...）负责将连接传递给Db，并使用从Db中传回的连接，在Db中不能关闭连接，由用户关闭。谁传递谁关闭。<br/>
 *
 * @author 胡开明
 * @since jadepool 1.0
 */
public class DbCenter implements Db {

    private String driverName = null;//数据库驱动程序名
    private String schema = null;//框架名
    private String catalog = null;//数据库名
    private String lastQuerySql = null;//上次使用的查询语句
    private int tsLevel = -1;//事务支持级别,可以在db.xml中预定义
    private Map<String, Table> tableMap = new LinkedHashMap();//表映射表
    private String[] tableNames = null;//转换成小写后的表名集合
    private String[] tableNames_tmp = null;//表名集合
    private String[] lastQuery = new String[20];//查询语句队列
    private DateTool dt = new DateTool();
    private JadeTool tool = new JadeTool();
    private Connection con = null;//数据库连接
    private DatabaseMetaData dm = null;
    private static DbCenter defaultDb = null;//通过db.xml配置的默认连接创建的Db实例
    private static DbCenter userDb = null;//通过指定的数据库连接创建的Db实例
    private static DbCenter db_01 = null;//通过指定的数据库连接，并指定db_01创建的Db实例
    private static DbCenter db_02 = null;//通过指定的数据库连接，并指定db_02创建的Db实例
    private static DbCenter jpaDb = null;//通过JPA配置文件配置的数据库连接创建的Db实例，暂缓
    private static DbCenter mybatisDb = null;//通过iBatis配置文件配置的数据库连接创建的Db实例，暂缓
    private static DbCenter hibernateDb = null;//通过hibernate配置文件配置的数据库连接创建的Db实例，暂缓
    public static final int USING_CONFIG_OF_JPA = 0;//使用JPA配置文件，获取连接
    public static final int USING_CONFIG_OF_HIBERNATE = 1;//使用HIBERNATE配置文件，获取连接
    public static final int USING_CONFIG_OF_MYBATIS = 2;//使用MYBATIS配置文件，获取连接
    public static final int USING_DB_01 = 101;//使用db_01，获取连接
    public static final int USING_DB_02 = 102;//使用db_02，获取连接
    private static int instance_times = 0;
    private int initNum = 0;

    /**
     * 通过指定的数据库连接构造实例
     */
    private DbCenter(Connection con) {
        init(con);
    }

    synchronized public static DbCenter instance(Connection con, int connectionType) {
        switch (connectionType) {
            case DbConnectionType.USING_CONFIG_OF_DEFAULT: {
                if (defaultDb == null) {
                    defaultDb = new DbCenter(con);
                }
                return defaultDb;
            }
            case DbConnectionType.USING_CONFIG_OF_JPA: {
                if (jpaDb == null) {
                    jpaDb = new DbCenter(con);
                }
                return jpaDb;
            }
            case DbConnectionType.USING_CONFIG_OF_HIBERNATE: {
                if (hibernateDb == null) {
                    hibernateDb = new DbCenter(con);
                }
                return hibernateDb;
            }
            case DbConnectionType.USING_CONFIG_OF_MYBATIS: {
                if (mybatisDb == null) {
                    mybatisDb = new DbCenter(con);
                }
                return mybatisDb;
            }
            case DbConnectionType.USING_DB_01: {
                if (db_01 == null) {
                    db_01 = new DbCenter(con);
                }
                return db_01;
            }
            case DbConnectionType.USING_DB_02: {
                if (db_02 == null) {
                    db_02 = new DbCenter(con);
                }
                return db_02;
            }
            case DbConnectionType.USING_CONFIG_OF_NONE: {
                if (userDb == null) {
                    userDb = new DbCenter(con);
                }
                return userDb;
            }
            default: {
                break;
            }
        }
        return null;
    }

    @Override
    public String getDriverName() {
        return driverName;
    }

    @Override
    public String getSchema() {
        return schema;
    }

    @Override
    public String getCatalog() {
        return catalog;
    }

    @Override
    public String[] getTableNames() {
        return tableNames;
    }

    @Override
    public Map<String, Table> getTableMap() {
        return tableMap;
    }

    @Override
    public Table getTable(String tableName) {
        Table t = tableMap.get(tableName);
        return t;
    }

    @Override
    public String[] getKeys(String tableName) {
        Table table = tableMap.get(tableName);
        return table.getKeys();
    }

    @Override
    public String[] getFields(String tableName) {
        Table table = tableMap.get(tableName);
        return table.getFields();
    }

    /**
     * 主键类型
     */
    @Override
    public String[] getKeysType(String tableName) {
        Table table = tableMap.get(tableName);
        String[] keys = table.getKeys();
        String[] type = new String[keys.length];
        for (int i = 0; i < keys.length; i++) {
            Field f = table.getFieldMap().get(keys[i]);
            type[i] = f.getTypeName();
        }
        return type;
    }

    @Override
    public Map<String, Field> getFieldMap(String tableName, String fieldName) {
        Table table = tableMap.get(tableName);
        return table.getFieldMap();
    }

    @Override
    public Field getField(String tableName, String fieldName) {
        Table table = tableMap.get(tableName);
        Field f = table.getFieldMap().get(fieldName);
        return f;
    }

    @Override
    public String getFieldTypeName(String tableName, String fieldName) {
        Field f = getField(tableName, fieldName);
        return f.getTypeName();
    }

    @Override
    public int getFieldSqlType(String tableName, String fieldName) {
        Field f = getField(tableName, fieldName);
        return f.getSqlType();
    }

    @Override
    public String getFieldTypeClassName(String tableName, String fieldName) {
        Field f = getField(tableName, fieldName);
        return f.getTypeClassName();
    }

    @Override
    public String getFieldPosition(String tableName, String fieldName) {
        Field f = getField(tableName, fieldName);
        return f.getPosition();
    }

    @Override
    public String getFieldSize(String tableName, String fieldName) {
        Field f = getField(tableName, fieldName);
        return f.getSize();
    }

    public String getFieldBufferLength(String tableName, String fieldName) {
        Field f = getField(tableName, fieldName);
        return f.getBufferLength();
    }

    @Override
    public String getFieldDecimal(String tableName, String fieldName) {
        Field f = getField(tableName, fieldName);
        return f.getDecimal();
    }

    @Override
    public String getFieldRegex(String tableName, String fieldName) {
        Field f = getField(tableName, fieldName);
        return f.getRegex();
    }

    @Override
    public String getFieldDefaultValue(String tableName, String fieldName) {
        Field f = getField(tableName, fieldName);
        return f.getDefaultValue();
    }

    @Override
    public String getFieldRemark(String tableName, String fieldName) {
        Field f = getField(tableName, fieldName);
        return f.getRemark();
    }

    @Override
    public boolean isFieldNullable(String tableName, String fieldName) {
        Field f = getField(tableName, fieldName);
        return f.isNullable();
    }

    @Override
    public boolean isFieldPrimarykey(String tableName, String fieldName) {
        Field f = getField(tableName, fieldName);
        return f.isPrimarykey();
    }

    /**
     * @deprecated 未获初始化值，暂不公开
     */
    private boolean isFieldForeignkey(String tableName, String fieldName) {
        Field f = getField(tableName, fieldName);
        return f.isForeignkey();
    }

    synchronized private void init(Connection _con) {
        this.con = _con;
        try {
            //schema=con.getSchema();
            catalog = con.getCatalog();//数据库名
            dm = con.getMetaData();
            driverName = dm.getDriverName();
            Set<String> tableNameSet = new java.util.LinkedHashSet();//表名集合
            String[] types = {"TABLE"};

            ResultSet rs = dm.getTables(null, null, null, types);//获取数据库中所有表的名称
            while (rs.next()) {
                String tableName = rs.getString("TABLE_NAME");
                tableNameSet.add(tableName);
            }
            Object[] o = tableNameSet.toArray();
            tableNames_tmp = new String[o.length];
            tableNames = new String[o.length];
            for (int i = 0; i < o.length; i++) {
                tableNames_tmp[i] = (String) o[i].toString();
                tableNames[i] = (String) o[i].toString().toLowerCase();
            }

            if (tableNames_tmp != null) {
                for (int i = 0; i < tableNames_tmp.length; i++) {
                    initTableMap(tableNames_tmp[i]);//大写
                }
            }

            instance_times++;

        } catch (SQLException ex) {
            Logger.getLogger(DbCenter.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            //con.close();共享con，不能关闭，由调用 Db实例.getCon()的用户关闭
        }

    }

    /**
     * 应当在此,还要初始化字段Field及各表的键值信息 注意事项： 使用ResultSet rs = dm.getColumns(catalog,
     * null, tableName, null);/使用derby数据库时，大小写是敏感的， 因此
     *
     * @param tableName的值，只能通过dm原型获取的值，这样造成了大小写问题，在运算过程中，不能转换大小写，只能在最后统一转换成小写，
     */
    synchronized private void initTableMap(String tableName) throws SQLException {
        Table table = getTable(tableName.toLowerCase());
        if (table == null) {
            table = new Table();
        }
        table.setName(tableName.toLowerCase());
        Set fieldSet = new java.util.LinkedHashSet();
        Set keySet = new java.util.LinkedHashSet();
        if (con != null) {
            ResultSet rs = dm.getColumns(catalog, null, tableName, null);//获取表中所有字段 //参考ResultSet rs = dm.getProcedureColumns(catalog, catalog, driverName, tableName);//?
            Map<String, Field> field_map = new LinkedHashMap();
            while (rs.next()) {
                String name = rs.getString("COLUMN_NAME");//参数值可参考dm.getColumns(catalog, null, tableName, null)的帮助文档
                fieldSet.add(lowerCase(name));
                Field f = new Field();
                f.setName(lowerCase(name));

                //DATA_TYPE int => SQL type from java.sql.Types

                /*
                 Each column description has the following columns:
                 TABLE_CAT String => table catalog (may be null)
                 TABLE_SCHEM String => table schema (may be null)
                 TABLE_NAME String => table name
                 COLUMN_NAME String => column name
                 DATA_TYPE int => SQL type from java.sql.Types
                 TYPE_NAME String => Data source dependent type name, for a UDT the type name is fully qualified
                 COLUMN_SIZE int => column size.
                 BUFFER_LENGTH is not used.
                 DECIMAL_DIGITS int => the number of fractional digits. Null is returned for data types where DECIMAL_DIGITS is not applicable.
                 NUM_PREC_RADIX int => Radix (typically either 10 or 2)
                 NULLABLE int => is NULL allowed.
                 columnNoNulls - might not allow NULL values
                 columnNullable - definitely allows NULL values
                 columnNullableUnknown - nullability unknown
                 REMARKS String => comment describing column (may be null)
                 COLUMN_DEF String => default value for the column, which should be interpreted as a string when the value is enclosed in single quotes (may be null)
                 SQL_DATA_TYPE int => unused
                 SQL_DATETIME_SUB int => unused
                 CHAR_OCTET_LENGTH int => for char types the maximum number of bytes in the column
                 ORDINAL_POSITION int => index of column in table (starting at 1)
                 IS_NULLABLE String => ISO rules are used to determine the nullability for a column.
                 YES --- if the column can include NULLs
                 NO --- if the column cannot include NULLs
                 empty string --- if the nullability for the column is unknown
                 SCOPE_CATALOG String => catalog of table that is the scope of a reference attribute (null if DATA_TYPE isn't REF)
                 SCOPE_SCHEMA String => schema of table that is the scope of a reference attribute (null if the DATA_TYPE isn't REF)
                 SCOPE_TABLE String => table name that this the scope of a reference attribute (null if the DATA_TYPE isn't REF)
                 SOURCE_DATA_TYPE short => source type of a distinct type or user-generated Ref type, SQL type from java.sql.Types (null if DATA_TYPE isn't DISTINCT or user-generated REF)
                 IS_AUTOINCREMENT String => Indicates whether this column is auto incremented
                 YES --- if the column is auto incremented
                 NO --- if the column is not auto incremented
                 empty string --- if it cannot be determined whether the column is auto incremented
                 IS_GENERATEDCOLUMN String => Indicates whether this is a generated column
                 YES --- if this a generated column
                 NO --- if this not a generated column
                 empty string --- if it cannot be determined whether this is a generated column

                 */


                String dataType = rs.getString("DATA_TYPE");
                f.setSqlType(new Integer(dataType).intValue());//如：java.sql.Types.INTEGER

                String type = rs.getString("TYPE_NAME");//如:BIGINT
                f.setTypeName(lowerCase(type));
                String position = rs.getString("ORDINAL_POSITION");//在表中的位置
                f.setPosition(position);


                String size = rs.getString("COLUMN_SIZE");//用户定义的字段长度
                f.setSize(size);

                String bufferLength = rs.getString("BUFFER_LENGTH");//字段缓冲区大小
                f.setBufferLength(bufferLength);


                String decimal = rs.getString("DECIMAL_DIGITS");//精度
                f.setDecimal(decimal);
                String defaultValue = rs.getString("COLUMN_DEF");
                f.setDefaultValue(defaultValue);
                String remark = rs.getString("REMARKS");
                f.setRemark(remark);
                String nullable = rs.getString("NULLABLE");//取值0||1,1允许空值,0不允许空值
                if ("0".equals(nullable)) {
                    f.setNullable(false);
                }
                if ("1".equals(nullable)) {
                    f.setNullable(true);
                }
                field_map.put(name.toLowerCase(), f);
            }

            table.setFieldMap(field_map);//字段名:Field对象的映射表;

            //获取字段名数组
            Object[] o = fieldSet.toArray();
            String[] fields = new String[o.length];
            for (int i = 0; i < o.length; i++) {
                fields[i] = ((String) o[i]).toLowerCase();

            }
            table.setFields(fields);

            //主键部分，开始
            ResultSet rsk = dm.getPrimaryKeys(catalog, null, tableName); //均通过新版SQL Server和MySQL的jdbc驱动的测试,返回所有主键  //ResultSet rsk = dm.getPrimaryKeys(catalog, "%", tableName);//早期版本的MySQL jdbc驱动程序中通过测试,返回所有主键  //
            while (rsk.next()) {
                String name = rsk.getString("COLUMN_NAME");//主键名
                keySet.add(lowerCase(name.toLowerCase()));//
            }
            Object[] k = keySet.toArray();
            String[] keys = new String[k.length];
            for (int i = 0; i < k.length; i++) {
                keys[i] = (String) k[i];
                field_map.get(keys[i]).setPrimarykey(true);//通过mssql、mysql、derby
            }
            table.setKeys(keys);
            //主键部分，结束

            ///给Field属性typeClassName赋值
            String squeryFieldTypeClassName = "select * from " + tableName.toLowerCase() + " where " + table.getFields()[0] + " is null";
            if (table.getKeys().length > 0) {
                squeryFieldTypeClassName = "select * from " + tableName.toLowerCase() + " where " + table.getKeys()[0] + " is null";
            }
            Statement stmt0 = con.createStatement();
            ResultSet rscname = stmt0.executeQuery(squeryFieldTypeClassName);
            ResultSetMetaData rsmd = rscname.getMetaData();
            for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                String fieldNmae = rsmd.getColumnName(i);
                field_map.get(fieldNmae.toLowerCase()).setTypeClassName(rsmd.getColumnClassName(i));//通过mssql、mysql、derby
            }
            stmt0.close();
        }
        tableMap.put(tableName.toLowerCase(), table);//初始化Table
    }

    @Override
    public boolean isExistTable(String tableName) {
        boolean b = tool.isInFields(tableNames, tableName);
        return b;
    }

    @Override
    public boolean isExistField(String tableName, String fieldName) {
        boolean b = tool.isInFields(getFields(tableName), fieldName);
        return b;
    }

    @Override
    public boolean isExistKeyField(String tableName, String fieldName) {
        boolean b = tool.isInFields(getKeys(tableName), fieldName);
        return b;
    }

    /**
     * 提取指定查询语句
     *
     * @param last 最近第last条
     */
    public String getLastQuerySql(int last) {
        if (last > (lastQuery.length - 1)) {
            return null;
        }
        return lastQuery[lastQuery.length - 1 - last];
    }

    /**
     * 将最近一次查询语句保存到已查询队列中
     *
     * @param lastQuerySql 最近一次查询语句
     */
    public void setLastQuerySql(String lastQuerySql) {
        for (int i = 0; i < this.lastQuery.length - 1; i++) {
            lastQuery[i] = lastQuery[i + 1];
        }
        lastQuery[lastQuery.length - 1] = lastQuerySql;
    }

    @Override
    public Connection getCon() {
        return con;
    }

    private String lowerCase(String s) {
        if (s != null) {
            s = s.toLowerCase();
        }
        return s;
    }
}

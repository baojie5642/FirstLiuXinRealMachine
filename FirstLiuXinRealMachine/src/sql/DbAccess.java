/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sql;

import cn.jadepool.util.DateTool;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author hkm
 */
public class DbAccess implements Db {

    private String driverName = null;//数据库驱动程序名
    private String schema = null;//框架名
    private String catalog = null;//数据库名
    private Map<String, Table> tableMap = new LinkedHashMap();//表映射表
    private String[] tableNames = null;//转换成小写后的表名集合
    private String[] tableNames_tmp = null;//表名集合
    private String[] lastQuery = new String[20];//查询语句队列
    private DateTool dt = new DateTool();
    private JadeTool tool = new JadeTool();
    private Connection con = null;//数据库连接
    private DatabaseMetaData dm = null;
    private static DbAccess defaultDb = null;//通过db.xml配置的默认连接创建的Db实例
    private static DbAccess userDb = null;//通过指定的数据库连接创建的Db实例
    private static int instance_times = 0;

    /**
     * 通过指定的数据库连接构造实例
     */
    private DbAccess(Connection con) {
        init(con);
    }

    synchronized public static DbAccess instance(Connection con, int connectionType) {
        switch (connectionType) {
            case DbConnectionType.USING_CONFIG_OF_DEFAULT: {
                if (defaultDb == null) {
                    defaultDb = new DbAccess(con);
                }
                return defaultDb;
            }

            case DbConnectionType.USING_CONFIG_OF_NONE: {
                if (userDb == null) {
                    userDb = new DbAccess(con);
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
            ex.printStackTrace();
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


                String dataType = rs.getString("DATA_TYPE");
                f.setSqlType(new Integer(dataType).intValue());//如：java.sql.Types.INTEGER

                String type = rs.getString("TYPE_NAME");//如:BIGINT
                f.setTypeName(lowerCase(type));
                String position = rs.getString("ORDINAL_POSITION");//在表中的位置
                f.setPosition(position);
                String size = rs.getString("COLUMN_SIZE");//字段长度//获取COLUMN_SIZE
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

            String squeryFieldTypeClassName = "select * from " + tableName.toLowerCase() + " where " + table.getFields()[0] + " is null";

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

    /*
     * 重新创建Db实例，用于数据库结构发生改变后
     */
    public void reBuild(int connectionType) {
        switch (connectionType) {
            case DbConnectionType.USING_CONFIG_OF_DEFAULT: {
                defaultDb = new DbAccess(con);
            }
            case DbConnectionType.USING_CONFIG_OF_NONE: {
                userDb = new DbAccess(con);
            }
            default: {
                break;
            }
        }
    }
}

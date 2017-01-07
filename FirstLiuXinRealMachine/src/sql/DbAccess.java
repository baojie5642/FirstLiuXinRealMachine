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

    private String driverName = null;//���ݿ�����������
    private String schema = null;//�����
    private String catalog = null;//���ݿ���
    private Map<String, Table> tableMap = new LinkedHashMap();//��ӳ���
    private String[] tableNames = null;//ת����Сд��ı�������
    private String[] tableNames_tmp = null;//��������
    private String[] lastQuery = new String[20];//��ѯ������
    private DateTool dt = new DateTool();
    private JadeTool tool = new JadeTool();
    private Connection con = null;//���ݿ�����
    private DatabaseMetaData dm = null;
    private static DbAccess defaultDb = null;//ͨ��db.xml���õ�Ĭ�����Ӵ�����Dbʵ��
    private static DbAccess userDb = null;//ͨ��ָ�������ݿ����Ӵ�����Dbʵ��
    private static int instance_times = 0;

    /**
     * ͨ��ָ�������ݿ����ӹ���ʵ��
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
     * ��������
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
     * @deprecated δ���ʼ��ֵ���ݲ�����
     */
    private boolean isFieldForeignkey(String tableName, String fieldName) {
        Field f = getField(tableName, fieldName);
        return f.isForeignkey();
    }

    synchronized private void init(Connection _con) {
        this.con = _con;
        try {
            //schema=con.getSchema();
            catalog = con.getCatalog();//���ݿ���
            dm = con.getMetaData();
            driverName = dm.getDriverName();
            Set<String> tableNameSet = new java.util.LinkedHashSet();//��������
            String[] types = {"TABLE"};

            ResultSet rs = dm.getTables(null, null, null, types);//��ȡ���ݿ������б������
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
                    initTableMap(tableNames_tmp[i]);//��д
                }
            }

            instance_times++;

        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            //con.close();����con�����ܹرգ��ɵ��� Dbʵ��.getCon()���û��ر�
        }

    }

    /**
     * Ӧ���ڴ�,��Ҫ��ʼ���ֶ�Field������ļ�ֵ��Ϣ ע����� ʹ��ResultSet rs = dm.getColumns(catalog,
     * null, tableName, null);/ʹ��derby���ݿ�ʱ����Сд�����еģ� ���
     *
     * @param tableName��ֵ��ֻ��ͨ��dmԭ�ͻ�ȡ��ֵ����������˴�Сд���⣬����������У�����ת����Сд��ֻ�������ͳһת����Сд��
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
            ResultSet rs = dm.getColumns(catalog, null, tableName, null);//��ȡ���������ֶ� //�ο�ResultSet rs = dm.getProcedureColumns(catalog, catalog, driverName, tableName);//?
            Map<String, Field> field_map = new LinkedHashMap();
            while (rs.next()) {
                String name = rs.getString("COLUMN_NAME");//����ֵ�ɲο�dm.getColumns(catalog, null, tableName, null)�İ����ĵ�
                fieldSet.add(lowerCase(name));
                Field f = new Field();
                f.setName(lowerCase(name));

                //DATA_TYPE int => SQL type from java.sql.Types


                String dataType = rs.getString("DATA_TYPE");
                f.setSqlType(new Integer(dataType).intValue());//�磺java.sql.Types.INTEGER

                String type = rs.getString("TYPE_NAME");//��:BIGINT
                f.setTypeName(lowerCase(type));
                String position = rs.getString("ORDINAL_POSITION");//�ڱ��е�λ��
                f.setPosition(position);
                String size = rs.getString("COLUMN_SIZE");//�ֶγ���//��ȡCOLUMN_SIZE
                f.setSize(size);

                String bufferLength = rs.getString("BUFFER_LENGTH");//�ֶλ�������С
                f.setBufferLength(bufferLength);

                String decimal = rs.getString("DECIMAL_DIGITS");//����
                f.setDecimal(decimal);
                String defaultValue = rs.getString("COLUMN_DEF");
                f.setDefaultValue(defaultValue);
                String remark = rs.getString("REMARKS");
                f.setRemark(remark);
                String nullable = rs.getString("NULLABLE");//ȡֵ0||1,1�����ֵ,0�������ֵ
                if ("0".equals(nullable)) {
                    f.setNullable(false);
                }
                if ("1".equals(nullable)) {
                    f.setNullable(true);
                }
                field_map.put(name.toLowerCase(), f);
            }

            table.setFieldMap(field_map);//�ֶ���:Field�����ӳ���;

            //��ȡ�ֶ�������
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
                field_map.get(fieldNmae.toLowerCase()).setTypeClassName(rsmd.getColumnClassName(i));//ͨ��mssql��mysql��derby
            }
            stmt0.close();
        }
        tableMap.put(tableName.toLowerCase(), table);//��ʼ��Table
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
     * ��ȡָ����ѯ���
     *
     * @param last �����last��
     */
    public String getLastQuerySql(int last) {
        if (last > (lastQuery.length - 1)) {
            return null;
        }
        return lastQuery[lastQuery.length - 1 - last];
    }

    /**
     * �����һ�β�ѯ��䱣�浽�Ѳ�ѯ������
     *
     * @param lastQuerySql ���һ�β�ѯ���
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
     * ���´���Dbʵ�����������ݿ�ṹ�����ı��
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

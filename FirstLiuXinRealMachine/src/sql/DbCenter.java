/*
 * 2009-9-19 ����
 * DbCenter��Table��Field������ֱ��������ݿ����Ӻ����������ݿ������������ֶ�����
 * ���ڶ�HashMap��ϵ����ӳ�似���Ľ�һ������
 * ��Db����������������� a.���ݿ������Ϣ,��:����,b.��Ľṹ��c.�ֶε�����
 * ����һЩʵ�÷���
 * Db�ĳ�ʼ����������FileManager��·�����ã������ļ�������Ӧ�ò���:
 * 0����path==null����ʹ��defaultPath = System.getProperty("user.dir")������user.dir��Ӧ�ó�����·��
 * 1�������ļ� //db.xml
 * 2���������ڣ��򴴽�/configĿ¼�����Զ�����һ��db.xml�����ļ�
 * 3���û�Ӧ���޸�db.xml�е��������ݿ�������Ϣ��ǰ��������������������ٱ�����һ��ʹ�����ʵ�����
 * 4����������Ӧ��ϵͳ����ϵͳ�������db.xml�ļ�
 * 5����ϵͳ���õ���ģʽ����һ��Dbʵ������Ӧ��ϵͳʹ�ã����db==null,��
 * 6����ϵͳ�������db.xml�ļ��������ݿ�����
 * 7��ʵ����Db����db
 * 8����ʼ����regex.xml�ļ�
 * 9��regex.xml�ļ������ɣ���ʼֵΪregex=""
 * ����ʹ�ò���
 *    ���ʹ���û��Զ���Ŀ¼�������������
 *
 *        FileManager fm = dm;
 *        fm.setPath("�û�ָ����������ļ���·��");//**** ��:d:\\temp
 *        fm.createDbXML();
 *
 *        DbCenter.instance().init();//��������ʱ,�����ܻ�����쳣,���û��޸� "�û�ָ����������ļ���·��"/META-INF/db.xml�ļ���������Ϣ,��ȷ���ø��ļ���,����Db.instance().init();�ͻ�õ���ȷ���
 * 
 *    ���ʹ��Ĭ��Ŀ¼����һ������
 *    DbCenter.instance().init();
 *    �˺���ɻ�ȡ���ݿ������Ϣ
 * @version DFree (pvo v2.0)
 * @since 2009-6-20
 * @author ������
 * @see <a href='http://www.wnhot.com/DFree/' target="_blank">HashMap��ϵӳ�似��</a><br/>
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
 * �޶����ڣ�2012-10-19<br/> ������Ŀ��<br/> 1��private java.sql.Connection con;<br/>
 * 2��private DbCenter(Connection con);<br/>
 * 3��������ģʽ�޸�Ϊ�����޶���ģʽ������ʵ�������Ա��ڿ��ܳ��ֵ��������ݿ�֮�䲻ͬ�����ݽ���<br/> ���ã�<br/>
 * �ṩ����Dbʵ����ÿ��ʵ�����ṩ���ݿ����Ӻ����ݿ������Ϣ<br/> ���������
 *
 * Ŀǰ��ʹ�����������������ͨ�� 1��sqljdbc4.jar(Microsoft SQL Server JDBC Driver 3.0)
 * 2��mysql-connector-java-5.1.7-bin.jar(MySQL-AB JDBC Driver) 3��Apache Derby
 * Network Client JDBC Driver
 * Db����������ݿ�Ľṹ��Ϣ����pvo�е��˷��������û����ࣩ��ȡ���ݿ�����Ӻ�����Ƚ���Db��Db��ȡ���ݿ���Ϣ�����ˣ������ӽ������û�,
 * ����Ϊ�û��õ���ȷ�����ݿ�ṹ�������Ϣ���û�����ProcessVO��DbFree��...���������Ӵ��ݸ�Db����ʹ�ô�Db�д��ص����ӣ���Db�в��ܹر����ӣ����û��رա�˭����˭�رա�<br/>
 *
 * @author ������
 * @since jadepool 1.0
 */
public class DbCenter implements Db {

    private String driverName = null;//���ݿ�����������
    private String schema = null;//�����
    private String catalog = null;//���ݿ���
    private String lastQuerySql = null;//�ϴ�ʹ�õĲ�ѯ���
    private int tsLevel = -1;//����֧�ּ���,������db.xml��Ԥ����
    private Map<String, Table> tableMap = new LinkedHashMap();//��ӳ���
    private String[] tableNames = null;//ת����Сд��ı�������
    private String[] tableNames_tmp = null;//��������
    private String[] lastQuery = new String[20];//��ѯ������
    private DateTool dt = new DateTool();
    private JadeTool tool = new JadeTool();
    private Connection con = null;//���ݿ�����
    private DatabaseMetaData dm = null;
    private static DbCenter defaultDb = null;//ͨ��db.xml���õ�Ĭ�����Ӵ�����Dbʵ��
    private static DbCenter userDb = null;//ͨ��ָ�������ݿ����Ӵ�����Dbʵ��
    private static DbCenter db_01 = null;//ͨ��ָ�������ݿ����ӣ���ָ��db_01������Dbʵ��
    private static DbCenter db_02 = null;//ͨ��ָ�������ݿ����ӣ���ָ��db_02������Dbʵ��
    private static DbCenter jpaDb = null;//ͨ��JPA�����ļ����õ����ݿ����Ӵ�����Dbʵ�����ݻ�
    private static DbCenter mybatisDb = null;//ͨ��iBatis�����ļ����õ����ݿ����Ӵ�����Dbʵ�����ݻ�
    private static DbCenter hibernateDb = null;//ͨ��hibernate�����ļ����õ����ݿ����Ӵ�����Dbʵ�����ݻ�
    public static final int USING_CONFIG_OF_JPA = 0;//ʹ��JPA�����ļ�����ȡ����
    public static final int USING_CONFIG_OF_HIBERNATE = 1;//ʹ��HIBERNATE�����ļ�����ȡ����
    public static final int USING_CONFIG_OF_MYBATIS = 2;//ʹ��MYBATIS�����ļ�����ȡ����
    public static final int USING_DB_01 = 101;//ʹ��db_01����ȡ����
    public static final int USING_DB_02 = 102;//ʹ��db_02����ȡ����
    private static int instance_times = 0;
    private int initNum = 0;

    /**
     * ͨ��ָ�������ݿ����ӹ���ʵ��
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
            Logger.getLogger(DbCenter.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
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
                f.setSqlType(new Integer(dataType).intValue());//�磺java.sql.Types.INTEGER

                String type = rs.getString("TYPE_NAME");//��:BIGINT
                f.setTypeName(lowerCase(type));
                String position = rs.getString("ORDINAL_POSITION");//�ڱ��е�λ��
                f.setPosition(position);


                String size = rs.getString("COLUMN_SIZE");//�û�������ֶγ���
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

            //�������֣���ʼ
            ResultSet rsk = dm.getPrimaryKeys(catalog, null, tableName); //��ͨ���°�SQL Server��MySQL��jdbc�����Ĳ���,������������  //ResultSet rsk = dm.getPrimaryKeys(catalog, "%", tableName);//���ڰ汾��MySQL jdbc����������ͨ������,������������  //
            while (rsk.next()) {
                String name = rsk.getString("COLUMN_NAME");//������
                keySet.add(lowerCase(name.toLowerCase()));//
            }
            Object[] k = keySet.toArray();
            String[] keys = new String[k.length];
            for (int i = 0; i < k.length; i++) {
                keys[i] = (String) k[i];
                field_map.get(keys[i]).setPrimarykey(true);//ͨ��mssql��mysql��derby
            }
            table.setKeys(keys);
            //�������֣�����

            ///��Field����typeClassName��ֵ
            String squeryFieldTypeClassName = "select * from " + tableName.toLowerCase() + " where " + table.getFields()[0] + " is null";
            if (table.getKeys().length > 0) {
                squeryFieldTypeClassName = "select * from " + tableName.toLowerCase() + " where " + table.getKeys()[0] + " is null";
            }
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
}

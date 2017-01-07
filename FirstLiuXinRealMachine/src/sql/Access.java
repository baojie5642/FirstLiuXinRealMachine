package sql;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

/*
 * ʵ���������ݿ�DML��������֧�����񣬲�֧�ָ��ิ�ӵ�JDBC����
 * ���͵��������ݿ��У�dbase��΢��Access��Ƕ��ʽJavaDB
 * Լ����
 * ���ڷ���Object[]��List��Map���ͣ���û�м�¼�����ؽ����������null�����ǰ���0����Ԫ�صĶ���
 * ʹ��LinkedHashMap��ȡ���ݿ��¼���Ա�֤�����¼�����ݿ��¼��˳��һ�£�
 * ʹ��List<Map<String,Object>>���ͣ����н飬ʵ�����ݿ���Ӧ�ó���֮�����ݻ�����
 * ˽�з����ṩ�㹻��Ĳ������Ա��ڽ�����ӵ����⣻
 * ���з����������ṩ���ٵĲ������Ա����û�ʹ�á�
 * ����JDBC����ͨ�����ӣ���ȡ���������ݿ��������Ϣ���ڸ��´���ʱ�����ѱ�ĵ�һ���ֶε�������������Ҫʱ���û�����ָ�������ֶ���
 */
public class Access implements Jdml {

    private java.sql.Connection con;
    private java.sql.Statement stmt;
    private java.sql.ResultSet rs;
    private java.sql.ResultSetMetaData rsmd;
    private JadeTool tool = new JadeTool();
    private DbAccess db = null;//ʵ����ʱ��Dbʹ�õ����ݿ�����������ProcessVOһ��
    private int indexStepLength;

    /*
     * Ĭ�Ϲ��캯����ͨ��DbĬ��ʵ��ʹ��/META-INF/db.xml���õ����ݿ�����
     */
    public Access() {
        db = DbAccess.instance(DbConnection.getDefaultCon(), DbConnectionType.USING_CONFIG_OF_DEFAULT);
        con = DbConnection.getDefaultCon();

    }

    /**
     * ���캯��.
     *
     * @param con ͨ��DbĬ��ʵ��ʹ��/META-INF/db.xml���õ����ݿ����ӡ�
     */
    public Access(Connection con) {

        db = DbAccess.instance(con, DbConnectionType.USING_CONFIG_OF_NONE);
        this.con = con;

    }

    /**
     * @param tableName �Ǳ���.
     * @return ��ȡ��¼��.
     * @throws SQLException
     */
    @Override
    public long queryCount(String tableName) throws SQLException {
        String sql = "select count(*) as express from " + tableName;
        return _queryCountOfTable(sql);
    }

    /**
     * @param tableName �Ǳ���
     * @param where String����where��where�Ӿ䲿��
     * @return ��ȡ��¼��.
     * @throws SQLException
     */
    @Override
    public long queryCount(String tableName, String where) throws SQLException {
        String sql = "select count(*) as express from " + tableName + " " + where;
        return _queryCountOfTable(sql);
    }

    /**
     * ��ȡ���ݿ�ṹ��Ϣ
     */
    @Override
    public String queryDbInfo() throws SQLException {
        String s = "Catalog : \t\t\t" + db.getCatalog();
        s = s + "\nConnection driver name :\t" + db.getDriverName();
        String[] t = db.getTableNames();
        for (int i = 0; i < t.length; i++) {
            s = s + "\n\n\t TABLE[" + i + "]: " + t[i] + "----------------------------------";
            String[] fs = db.getFields(t[i]);
            for (int j = 0; j < fs.length; j++) {
                Field f = db.getField(t[i], fs[j]);
                s = s + "\n\n\t\t FieldName :\t" + f.getName()
                        //+ ";\n\t\t IsPrimarykey :\t" + f.isPrimarykey()
                        + ";\n\t\t TypeName :\t" + f.getTypeName()
                        + ";\n\t\t DataType :\t" + f.getSqlType()
                        + ";\n\t\t BufferLength :\t" + f.getBufferLength()
                        + ";\n\t\t Position :\t" + f.getPosition()
                        + ";\n\t\t ThisSize :\t" + f.getSize()
                        + ";\n\t\t Decimal :\t" + f.getDecimal()
                        + ";\n\t\t DefaultValue :\t" + f.getDefaultValue()
                        + ";\n\t\t Remark :\t" + f.getRemark()
                        + ";\n\t\t NullAble :\t" + f.isNullable()
                        //+ ";\n\t\t Regex :\t" + f.getRegex()//��δʵ��
                        + ";\n\t\t className :\t" + f.getTypeClassName();
            }
        }
        return s;
    }

    /**
     * ��ѯָ����Ľṹ��Ϣ
     *
     * @param tableName
     * @return ���ر��������ֶ���Ϣ
     */
    @Override
    public String queryTableInfo(String tableName) throws SQLException {
        String s = "TABLE:\t " + tableName + "\n------------------------------------------------------";
        String[] fs = db.getFields(tableName);
        for (int j = 0; j < fs.length; j++) {
            Field f = db.getField(tableName, fs[j]);
            s = s + "\n\n\t\t FieldName :\t" + f.getName()
                    //+ ";\n\t\t IsPrimarykey :\t" + f.isPrimarykey()
                    + ";\n\t\t TypeName :\t" + f.getTypeName()
                    + ";\n\t\t DataType :\t" + f.getSqlType()
                    + ";\n\t\t BufferLength :\t" + f.getBufferLength()
                    + ";\n\t\t Position :\t" + f.getPosition()
                    + ";\n\t\t ThisSize :\t" + f.getSize()
                    + ";\n\t\t Decimal :\t" + f.getDecimal()
                    + ";\n\t\t DefaultValue :\t" + f.getDefaultValue()
                    + ";\n\t\t Remark :\t" + f.getRemark()
                    + ";\n\t\t NullAble :\t" + f.isNullable()
                    //+ ";\n\t\t Regex :\t" + f.getRegex()//��δʵ��
                    + ";\n\t\t className :\t" + f.getTypeClassName();
        }

        return s;
    }

    /**
     * �������쳣ʱ,ȡ����ѯ����,���ͷ�Statement��Դ.
     *
     * @param sql �ǲ�ѯ���
     * @return ��ȡ��¼��.
     * @throws SQLException
     */
    private long _queryCountOfTable(String sql) throws SQLException {
        long count = -1l;
        try {
            stmt = con.createStatement();
            rs = stmt.executeQuery(sql);
            rs.next();
            Object o = rs.getObject("express");
            count = Long.parseLong(o.toString());
            rs.close();
        } catch (SQLException ex) {
            throw ex;
        } finally {
            stmt.close();
        }
        return count;
    }

    /**
     * �����¼��ִ��executeUpdate����
     *
     * @param insertSql
     * @return ����JDBC��׼�������ķ���ֵ
     * @throws SQLException
     */
    @Override
    public int insert(String insertSql) throws SQLException {
        int num = this.update(insertSql);
        return num;
    }

    /**
     * �����¼��ִ��executeUpdate����
     *
     * @param insertSql
     * @return ����JDBC��׼�������ķ���ֵ
     * @throws SQLException
     */
    @Override
    public int insert(String[] insertSql) throws SQLException {
        int num = this.update(insertSql);
        return num;
    }

    /**
     * ���ķ���,����в���һ����¼�� �㳤���ַ���������Ϊnull��
     *
     * @param tableName �Ǳ���
     * @param mapRecord ��׼�����뵽���е�һ����¼������,��������ֶ�����ͬ,˳���޹�;�������������ֶ����������������
     * @return ����JDBC��׼�������ķ���ֵ
     * @throws SQLException
     */
    @Override
    public int insert(String tableName, Map<String, Object> mapRecord) throws SQLException {
        int num = 0;
        List a = new ArrayList();
        a.add(mapRecord);
        num = this.insert(tableName, a);
        return num;
    }

    /**
     * ���ķ���,����в���һ����¼�� �㳤���ַ���������Ϊnull��
     *
     * @param tableName �Ǳ���
     * @param mapRecord ��׼�����뵽���е�һ����¼������,��������ֶ�����ͬ,˳���޹�;�������������ֶ����������������
     * @param autoInsertKey ֵΪtrueʱ���Զ���������
     * @return ����JDBC��׼�������ķ���ֵ
     * @throws SQLException
     */
    @Override
    public int insert(String tableName, Map<String, Object> mapRecord, boolean autoInsertKey) throws SQLException {
        int num = 0;
        List a = new ArrayList();
        a.add(mapRecord);
        num = this.insert(tableName, a, autoInsertKey);
        return num;
    }

    /**
     * ����executeInsert���������¼
     */
    @Override
    public int insert(String tableName, List<Map> listRecord) throws SQLException {
        int num = this._insert(tableName, listRecord, false);
        return num;
    }

    /**
     * ����executeInsert���������¼
     *
     * @param tableName ����
     * @param listRecord ��¼��
     * @param autoInsertKey ֵΪtrueʱ���Զ���������
     */
    @Override
    public int insert(String tableName, List<Map> listRecord, boolean autoInsertKey) throws SQLException {
        int num = this._insert(tableName, listRecord, true);
        return num;
    }

    /**
     * ִ��Statement��execute���� ����ִ�д��������±�Ľṹ��...
     *
     * @param sqlUpdate ��һ����׼��SQL�������.
     * @return ���ر�׼�������ķ���ֵ
     * @throws SQLException
     */
    @Override
    public boolean execute(String sqlUpdate) throws SQLException {
        boolean f = false;
        try {
            stmt = con.createStatement();
            f = stmt.execute(sqlUpdate);
        } catch (SQLException ex) {
            throw ex;
        } finally {
            stmt.close();
        }
        return f;
    }

    /**
     * ִ��Statement��executeUpdate�������磺���¼�¼
     *
     * @param updateSql ��һ���׼��SQL������䣬�����ǲ��롢ɾ��������.
     * @return ���ر�׼�������ķ���ֵ
     * @throws SQLException
     */
    @Override
    public boolean execute(String[] updateSql) throws SQLException {
        boolean f = false;
        try {
            stmt = con.createStatement();
            for (int i = 0; i < updateSql.length; i++) {
                f = stmt.execute(updateSql[i]);
            }
        } catch (SQLException ex) {
            throw ex;
        } finally {
            stmt.close();
        }
        return f;
    }
    
    
    
    /**
     * ���á�ִ��һ��sql�ļ�
     *
     * @param sqlFileName һ��sql�ļ�.
     * @return �Ƿ�ִ�гɹ�
     * @throws SQLException
     */
    @Override
    public boolean executeSqlFile(String sqlFileName) throws SQLException {
        boolean f = false;
        try {
            String sql = new String(tool.getBytesFromFile(new File(sqlFileName)));
            stmt = con.createStatement();
            f = stmt.execute(sql);
        } catch (IOException ex) {
            stmt.cancel();
            throw new SQLException("IOException: " + ex.getMessage(), ex.getCause());
        } catch (SQLException ex) {
            stmt.cancel();
            throw ex;
        } finally {
            stmt.close();
        }
        return f;
    }
    

    /**
     * ִ��Statement��executeUpdate�������磺���롢ɾ�������¼�¼
     *
     * @param updateSql ��һ����׼��SQL������䣬�����ǲ��롢ɾ��������.
     * @return ���ر�׼�������ķ���ֵ
     * @throws SQLException
     */
    @Override
    public int update(String updateSql) throws SQLException {
        int count = -1;
        try {
            stmt = con.createStatement();
            count = stmt.executeUpdate(updateSql);
        } catch (SQLException ex) {
            throw ex;
        } finally {
            stmt.close();
        }
        return count;
    }

    /**
     * ִ��Statement��executeUpdate�������磺���¼�¼
     *
     * @param updateSql ��һ���׼��SQL������䣬�����ǲ��롢ɾ��������.
     * @return ���ر�׼�������ķ���ֵ
     * @throws SQLException
     */
    @Override
    public int update(String[] updateSql) throws SQLException {
        int count = 0;
        try {
            stmt = con.createStatement();
            for (int i = 0; i < updateSql.length; i++) {
                count = count + stmt.executeUpdate(updateSql[i]);
            }
            count = stmt.getUpdateCount();
        } catch (SQLException ex) {
            throw ex;
        } finally {
            stmt.close();
        }
        return count;
    }

    /**
     * ����һ����¼. �㳤���ַ���������Ϊnull��
     *
     * @param tableName �Ǳ���
     * @param mapRecord ��׼�����뵽���е�һ����¼������,��������ֶ�����ͬ,˳���޹�;�������������ֶ����������������;
     * @return ���ر�׼update�����ķ���ֵ
     * @throws SQLException
     */
    @Override
    public int update(String tableName, Map<String, Object> mapRecord) throws SQLException {
        List a = new ArrayList();
        a.add(mapRecord);
        return this._update(tableName, a, null);
    }

    /**
     * ����һ����¼. �㳤���ַ���������Ϊnull��
     *
     * @param tableName �Ǳ���
     * @param mapRecord ��׼�����뵽���е�һ����¼������,��������ֶ�����ͬ,˳���޹�;�������������ֶ����������������;
     * @param where ��һ����׼��where�Ӿ�.
     * @return ���ر�׼update�����ķ���ֵ
     * @throws SQLException
     */
    @Override
    public int update(String tableName, Map<String, Object> mapRecord, String where) throws SQLException {
        List a = new ArrayList();
        a.add(mapRecord);
        return this._update(tableName, a, where);
    }

    /**
     * ͨ��PreparedStatement�Զ����¶�����¼
     *
     * @param tableName ����
     * @param listRecord �������������ֶεļ�¼��
     */
    @Override
    public int update(String tableName, List<Map> listRecord) throws SQLException {
        int num = 0;
        num = _update(tableName, listRecord, null);
        return num;
    }

    /**
     *
     *
     * @param tableName ����
     * @param listRecord �������������ֶεļ�¼��
     * @param where ��������
     */
    @Override
    public int update(String tableName, List<Map> listRecord, String where) throws SQLException {
        int num = 0;
        num = _update(tableName, listRecord, where);
        return num;
    }

    /**
     * ����һ����¼.
     * ����ü�¼���ڣ������֮������ü�¼�����ڣ�����롣�����һ��������ֵΪnull||""�����Զ������µ�����ֵ������������ʺ϶Ժ��������ı���в������������Ӱ��Զ������ı���и���
     *
     * @param tableName �Ǳ���
     * @param mapRecord ��׼�����뵽���е�һ����¼������,��������ֶ�����ͬ,˳���޹�;�������������ֶ����������������;
     * @return ���ص�һ������ֵ����ע��ԭ����int��update����ֵ������Ϊ��һ������ֵ����������Ӧ�á�
     * @throws SQLException
     */
    @Override
    public Object saveOne(String tableName, Map<String, Object> mapRecord) throws SQLException {
        Object num = null;
        String[] keys = new String[]{db.getFields(tableName)[0]};
        num = this.saveOne(tableName, mapRecord, keys);
        return num;
    }

    /**
     * ����һ����¼.
     * ����ü�¼���ڣ������֮������ü�¼�����ڣ�����롣�����һ��������ֵΪnull||""�����Զ������µ�����ֵ������������ʺ϶Ժ��������ı���в������������Ӱ��Զ������ı���и���
     *
     * @param tableName �Ǳ���
     * @param mapRecord ��׼�����뵽���е�һ����¼������,��������ֶ�����ͬ,˳���޹�;�������������ֶ����������������;
     * @param keyFieldName ����������
     * @return ���ص�һ������ֵ����ע��ԭ����int��update����ֵ������Ϊ��һ������ֵ����������Ӧ�á�
     * @throws SQLException
     */
    public Object saveOne(String tableName, Map<String, Object> mapRecord, String[] keyFieldName) throws SQLException {
        Object kv = mapRecord.get(keyFieldName[0]);
        int n = 0;
        String _w = "";
        if (keyFieldName.length == 0) {
            n = this.insert(tableName, mapRecord);//�������ı����ֱ�Ӳ���
            if (n > 0) {
                return kv;
            }
        } else {


            if (kv != null || !"".equals(kv)) {
                Object[] recordFields = mapRecord.keySet().toArray();
                Map _key_m = new LinkedHashMap();
                for (int i = 0; i < recordFields.length; i++) {
                    if (tool.isInFields(keyFieldName, recordFields[i].toString())) {
                        _key_m.put(recordFields[i].toString(), mapRecord.get(recordFields[i].toString()));//��ȡ��¼�е������ֶ�
                    }
                }
                if (!_key_m.isEmpty()) {
                    Object[] _k = _key_m.keySet().toArray();
                    if (_k.length != keyFieldName.length) {
                        return null;//������������¼��������ȫ�����ܱ���
                    } else {
                        Field f = db.getField(tableName, _k[0].toString());
                        if (f.getTypeClassName().equals("java.lang.String")) {
                            _w = " where " + _k[0] + " like '" + mapRecord.get(_k[0].toString()) + "'";
                        } else {
                            _w = " where " + _k[0] + " = " + mapRecord.get(_k[0].toString());
                        }
                        if (_k.length > 1) {
                            for (int i = 1; i < _k.length; i++) {
                                f = db.getField(tableName, _k[i].toString());
                                if (f.getTypeClassName().equals("java.lang.String")) {
                                    _w = _w + " and " + _k[i] + " like '" + mapRecord.get(_k[i].toString()) + "'";
                                } else {
                                    _w = _w + " and " + _k[i] + " = " + mapRecord.get(_k[i].toString());
                                }
                            }
                        }
                        Map rm = this.queryOne("select " + _k[0] + " from " + tableName + _w);
                        if (rm.isEmpty()) {
                            n = this.insert(tableName, mapRecord);//ԭԭ�����ز���
                        } else {
                            n = this.update(tableName, mapRecord, _w);//ԭԭ�����ظ���
                        }
                    }
                }
            } else {// if (kv == null || "".equals(kv)) 
                Field f = db.getField(tableName, keyFieldName[0]);
                String className = f.getTypeClassName();
                if (className.equals("java.lang.Long")) {
                    kv = insertKey(tableName);
                    mapRecord.put(keyFieldName[0], kv);
                    n = update(tableName, mapRecord);//��������������¼�в���������¼||������¼ֵ��null||����ֵ��""������룬���Զ���������
                }
                if (className.equals("java.lang.Integer") || className.equals("java.lang.Short") || className.equals("java.lang.Float") || className.equals("java.lang.Double") || className.equals("java.lang.String")) {
                    kv = insertAutoKey(tableName);
                    mapRecord.put(keyFieldName[0], kv);
                    n = update(tableName, mapRecord);//��������������¼�в���������¼||������¼ֵ��null||����ֵ��""������룬���Զ���������
                }
            }
        }
        return kv;
    }

    /**
     * ����һ����¼.
     * ����ü�¼���ڣ������֮������ü�¼�����ڣ�����롣�����һ��������ֵΪnull||""�����Զ������µ�����ֵ������������ʺ϶Ժ��������ı���в������������Ӱ��Զ������ı���и���
     *
     * @param tableName �Ǳ���
     * @param mapRecord ��׼�����뵽���е�һ����¼������,��������ֶ�����ͬ,˳���޹�;�������������ֶ����������������;
     * @return ���ر�׼update�������ص�״ֵ̬
     * @throws SQLException
     */
    public int save(String tableName, Map<String, Object> mapRecord) throws SQLException {
        int num = 0;
        String[] keys = new String[]{db.getFields(tableName)[0]};
        num = this.save(tableName, mapRecord, keys);
        return num;
    }

    /**
     * ����һ����¼.
     * ����ü�¼���ڣ������֮������ü�¼�����ڣ�����롣�����һ��������ֵΪnull||""�����Զ������µ�����ֵ������������ʺ϶Ժ��������ı���в������������Ӱ��Զ������ı���и���
     *
     * @param tableName �Ǳ���
     * @param mapRecord ��׼�����뵽���е�һ����¼������,��������ֶ�����ͬ,˳���޹�;�������������ֶ����������������;
     * @param keyFieldName ����������
     * @return ���ر�׼update�������ص�״ֵ̬
     * @throws SQLException
     */
    public int save(String tableName, Map<String, Object> mapRecord, String[] keyFieldName) throws SQLException {
        int num = 0;
        String _w = "";
        if (keyFieldName.length == 0) {
            return this.insert(tableName, mapRecord);//�������ı����ֱ�Ӳ���
        } else {
            Object kv = mapRecord.get(keyFieldName[0].toString());
            if (kv == null || "".equals(kv)) {
                num = insert(tableName, mapRecord, true);//��������������¼�в���������¼||������¼ֵ��null||����ֵ��""������룬���Զ���������
            } else {
                Object[] recordFields = mapRecord.keySet().toArray();
                Map _key_m = new LinkedHashMap();
                for (int i = 0; i < recordFields.length; i++) {
                    if (tool.isInFields(keyFieldName, recordFields[i].toString())) {
                        _key_m.put(recordFields[i].toString(), mapRecord.get(recordFields[i].toString()));//��ȡ��¼�е������ֶ�
                    }
                }
                if (!_key_m.isEmpty()) {
                    Object[] _k = _key_m.keySet().toArray();
                    if (_k.length != keyFieldName.length) {
                        return num;
                    } else {
                        Field f = db.getField(tableName, _k[0].toString());
                        if (f.getTypeClassName().equals("java.lang.String")) {
                            _w = " where " + _k[0] + " like '" + mapRecord.get(_k[0].toString()) + "'";
                        } else {
                            _w = " where " + _k[0] + " = " + mapRecord.get(_k[0].toString());
                        }
                        if (_k.length > 1) {
                            for (int i = 1; i < _k.length; i++) {
                                f = db.getField(tableName, _k[i].toString());
                                if (f.getTypeClassName().equals("java.lang.String")) {
                                    _w = _w + " and " + _k[i] + " like '" + mapRecord.get(_k[i].toString()) + "'";
                                } else {
                                    _w = _w + " and " + _k[i] + " = " + mapRecord.get(_k[i].toString());
                                }
                            }
                        }
                        Map rm = this.queryOne("select " + _k[0] + " from " + tableName + _w);
                        if (rm.isEmpty()) {
                            num = this.insert(tableName, mapRecord);//ԭԭ�����ز���
                        } else {
                            num = this.update(tableName, mapRecord, _w);//ԭԭ�����ظ���
                        }
                    }
                }
            }
        }
        return num;
    }

    /**
     * ִ��Statement��executeUpdate����
     *
     * @param deleteSql ��һ����׼��SQL������䣬�����ǲ��롢ɾ��������.
     * @return ���ر�׼�������ķ���ֵ
     * @throws SQLException
     */
    @Override
    public int delete(String deleteSql) throws SQLException {
        return this.update(deleteSql);
    }

    /**
     * ִ��Statement��executeUpdate����
     *
     * @param deleteSql ��һ����׼��SQL������䣬�����ǲ��롢ɾ��������.
     * @return ���ر�׼�������ķ���ֵ
     * @throws SQLException
     */
    @Override
    public int delete(String[] deleteSql) throws SQLException {
        return this.update(deleteSql);
    }

    /**
     * �Զ���1��ȡ����long��ȡʱ��ֵ���ô����࣬��������ͨ��DateTool�Ĺ��캯��DateTool(ʱ��ֵ)������ȡ����ʱ����Ϣ
     */
    private Object _nextAutoValue(String fieldClassName, Object fieldValue) throws SQLException {
        Object nextValue = null;
        if (fieldValue == null) {

            if (fieldClassName.equals("java.lang.Long")) {
                nextValue = new Long(0);
            }
            if (fieldClassName.equals("java.lang.Integer")) {
                nextValue = new Integer(0);
            }
            if (fieldClassName.equals("java.lang.Short")) {
                short v = 0;
                nextValue = new Short(v);
            }
            if (fieldClassName.equals("java.lang.Float")) {
                nextValue = new Float(0);
            }
            if (fieldClassName.equals("java.lang.Double")) {
                nextValue = new Double(0);
            }
        } else {

            if (fieldClassName.equals("java.lang.Long")) {
                if (((Long) fieldValue).longValue() == Long.MAX_VALUE) {
                    throw new SQLException("Long value must less than Long.MAX_VALUE " + Long.MAX_VALUE);
                }
                nextValue = new Long(((Long) fieldValue).longValue() + 1);
            }
            if (fieldClassName.equals("java.lang.Integer")) {
                if (((Integer) fieldValue).intValue() == Integer.MAX_VALUE) {
                    throw new SQLException("Integer value must less than Integer.MAX_VALUE " + Integer.MAX_VALUE);
                }
                nextValue = new Integer(((Integer) fieldValue).intValue() + 1);
            }
            if (fieldClassName.equals("java.lang.Short")) {
                if (((Short) fieldValue).shortValue() == Short.MAX_VALUE) {
                    throw new SQLException("Short value must less than Short.MAX_VALUE " + Short.MAX_VALUE);
                }
                short v = ((Short) fieldValue).shortValue();
                v++;
                nextValue = new Short(v);
            }
            if (fieldClassName.equals("java.lang.Float")) {
                if (((Float) fieldValue).floatValue() == Float.MAX_VALUE) {
                    throw new SQLException("Float value must less than Float.MAX_VALUE " + Float.MAX_VALUE);
                }
                Float v = new Float(((Float) fieldValue).floatValue() + 1);
                long l = (long) v.floatValue();
                nextValue = new Float(l);

            }
            if (fieldClassName.equals("java.lang.Double")) {
                if (((Double) fieldValue).doubleValue() == Double.MAX_VALUE) {
                    throw new SQLException("Double value must less than Double.MAX_VALUE " + Double.MAX_VALUE);
                }
                Double v = new Double(((Double) fieldValue).doubleValue() + 1);
                long l = (long) v.floatValue();
                nextValue = new Double(l);
            }
        }
        return nextValue;
    }

    /*
     * �����������ݿ����
     *
     * ��ɲ���Ĳ��裺 
     * 1�����˼�¼����Ч�ֶΣ�����Ч�ֶ�Object[] fields
     * 2������isUpdateKey����Ч�ֶμ������飺trueʱ���ٵ������ֶ���Object[] keys�ͷ������ֶ���Object[] fields
     * 3��������¼for (Map record : listRecord) {}
     * 4������keys��fields��isUpdateKey��whereStr�����������updateSQL
     * 5��ִ�и���stmt.executeUpdate(updateSQL);
     * ����˵����
     * 1������ʱ����whereStr==null||"".equals(where)�����Զ�����keys���where������䣬����ʹ��whereStr
     * 2����Ҫʱ�������ṩͳһ���������ķ��� 
     * 3����������ʱ�������û�����executeUpdate(String updateSql)��������: updateSQL="update label set label_id = 0, label = '���' where groupid LIKE '�������'"; executeUpdate(updateSQL);
     *
     *
     * @param tableName ��һ������
     * @param listRecord �Ǿ�����ͬ�ṹ��һ���¼
     * @param autoInsertKey ֵΪtrueʱ���Զ���������ֵ������ʹ�ü�¼���������ֵ �Ǵ�LinkedHashMap�����л�ȡ��ֵ
     * @throws SQLException
     */
    private int _insert(String tableName, List<Map> listRecord, String[] keyFields, boolean autoInsertKey) throws SQLException {
        Statement stmt0 = con.createStatement();
        int num = 0;
        Map<String, Object> _m = new LinkedHashMap(listRecord.get(0));//��ȡһ����¼

        Object maxFieldValue = null;

        String[] tableFields = db.getFields(tableName);
        Object[] recordFields = (_m.keySet()).toArray(); //��ȡ��¼����ֶ����ļ���

        for (int i = 0; i < recordFields.length; i++) {
            if (!tool.isInFields(tableFields, recordFields[i].toString())) {
                _m.remove(recordFields[i].toString());//�Ƴ���Ч�ֶ�
            }
        }

        Object[] fields = (_m.keySet()).toArray(); //���˺����Ч�ֶ�
        try {
            long firstStringKey = (new java.util.Date()).getTime();

            for (Map record : listRecord) {
                String keyName = fields[0].toString();//�趨��һ������
                if (keyFields != null && keyFields.length > 0) {
                    keyName = keyFields[0];
                }

                //����һ��������ֵ
                if (autoInsertKey) {
                    Field keyF = db.getField(tableName, keyName);
                    maxFieldValue = this._nextAutoValue(keyF.getTypeClassName(), maxFieldValue);
                    record.put(keyName, maxFieldValue);
                    if ("java.lang.String".equals(keyF.getTypeClassName()) && Integer.parseInt(keyF.getSize()) > 13) {
                        record.put(keyName, "" + (firstStringKey + 1));
                    } else {
                        return num;
                    }
                }

                Object[] values = new Object[fields.length];
                for (int i = 0; i < fields.length; i++) {
                    values[i] = record.get(fields[i]);//�Ӽ�¼��ȡֵ
                }
                String insertSQL = "insert into " + tableName + " (" + tool.arryToString(fields, ",") + " ) values (" + tool.arryToValues(values, ",") + ")";//�����������

                stmt0.executeUpdate(insertSQL);  //������䷶����stmt.execute("insert into label(label_id,parent_id,groupid,label) values('1','0','��ҵ����','��ֲ��ֳ')");
                num = num + stmt0.getUpdateCount();

            }

        } catch (NumberFormatException ex) {
            throw new SQLException("NumberFormatException :" + ex.getMessage());
        } finally {
            stmt0.close();
        }
        return num;
    }

    /*
     * �����������ݿ����
     * 
     * @param tableName ��һ������
     * @param listRecord �Ǿ�����ͬ�ṹ��һ���¼
     * @param autoInsertKey ֵΪtrueʱ���Զ���������ֵ������ʹ�ü�¼���������ֵ
     * @throws SQLException
     */
    private int _insert(String tableName, List<Map> listRecord, boolean autoInsertKey) throws SQLException {
        int num = 0;
        String[] keyFields = new String[0];
        num = this._insert(tableName, listRecord, keyFields, autoInsertKey);
        return num;
    }

    /*
     * �����������ݿ����
     * 
     * ��ɸ��µĲ��裺
     * 1�����˼�¼����Ч�ֶΣ�����Ч�ֶ�Object[] fields
     * 2������isUpdateKey����Ч�ֶμ������飺trueʱ���ٵ������ֶ���Object[] keys�ͷ������ֶ���Object[] fields
     * 3��������¼for (Map record : listRecord) {}
     * 4������keys��fields��isUpdateKey��whereStr�����������updateSQL
     * 5��ִ�и���stmt.executeUpdate(updateSQL);
     * ����˵����
     * 1������ʱ����whereStr==null||"".equals(where)�����Զ�����keys���where������䣬����ʹ��whereStr
     * 2����Ҫʱ�������ṩͳһ���������ķ���
     * 3����������ʱ�������û�����executeUpdate(String updateSql)��������:
     *        updateSQL="update label set label_id = 0, label = '���' where groupid LIKE '�������'";
     *        executeUpdate(updateSQL);
     *
     * @param tableName ��һ������
     * @param listRecord �Ǿ�����ͬ�ṹ��һ���¼
     * @param  keyFieldName ����
     * @param isUpdateKey �Ƿ��������
     * @param whereStr �����������
     * @return ���ظ���״̬
     * @throws SQLException
     */
    private int _update(String tableName, List<Map> listRecord, String[] keyFieldName, boolean isUpdateKey, String whereStr) throws SQLException {
        Statement stmt0 = con.createStatement();
        int num = 0;
        Map<String, Object> _m = new LinkedHashMap(listRecord.get(0));//��ȡһ����¼����Ϊ���ˡ���������
        String[] tableFields = db.getFields(tableName);
        Object[] recordFields = (_m.keySet()).toArray(); //��ȡ��¼����ֶ����ļ���
        for (int i = 0; i < recordFields.length; i++) {
            if (!tool.isInFields(tableFields, recordFields[i].toString())) {
                _m.remove(recordFields[i].toString());//�Ƴ���Ч�ֶΣ� �鿴��¼�е��ֶ��ڱ����Ƿ���ڣ���������ڣ����Ƴ���
            }
        }
        Object[] k0 = (_m.keySet()).toArray(); //���˺����Ч�ֶ�
        Map<String, Object> key_m = new LinkedHashMap();//��¼�������
        if (!isUpdateKey) {
            for (int i = 0; i < k0.length; i++) {
                if (tool.isInFields(keyFieldName, k0[i].toString())) {//��¼���Ƿ�������
                    key_m.put(k0[i].toString(), _m.remove(k0[i].toString()));//����¼�е������Ƶ�_key_m�У���֤������������
                }
            }
        }
        Object[] fields = (_m.keySet()).toArray(); //��¼�в�������������Ч�ֶΣ��ٴι��˵������ֶεĽ��
        Object[] keys = (key_m.keySet()).toArray(); //��¼�а���������

        if (fields.length > 0) {
            try {
                for (Map record : listRecord) {
                    Object[] values_set = new Object[fields.length];
                    for (int i = 0; i < fields.length; i++) {
                        values_set[i] = fields[i].toString() + "='" + record.get(fields[i].toString()) + "'";
                    }
                    String updateSQL = "";
                    if (whereStr != null && !"".equals(whereStr)) {
                        updateSQL = "update " + tableName + " set " + tool.arryToString(values_set, ",") + whereStr;
                    } else {
                        if (keys.length > 0) {
                            String _w = "";
                            if (keys.length > 0) {
                                Field f = db.getField(tableName, keys[0].toString());
                                Object _o = record.get(keys[0].toString());
                                String _s = "";
                                if (f.getTypeClassName().equals("java.lang.String")) {
                                    _s = keys[0].toString() + " like '" + _o.toString() + "'";
                                } else {
                                    _s = keys[0].toString() + " = " + _o.toString();
                                }
                                if (keys.length > 1) {
                                    for (int i = 1; i < keys.length; i++) {
                                        f = db.getField(tableName, keys[i].toString());
                                        _o = record.get(keys[i].toString());
                                        if (f.getTypeClassName().equals("java.lang.String")) {
                                            _s = _s + " and " + keys[i].toString() + " like '" + _o.toString() + "'";
                                        } else {
                                            _s = _s + " and " + keys[i].toString() + " = " + _o.toString();
                                        }
                                    }
                                }
                                _w = " where " + _s;
                                updateSQL = "update " + tableName + " set " + tool.arryToString(values_set, ",") + _w;
                            }
                        } else {
                            return num;//��������������
                        }
                    }
                    if (!"".equals(updateSQL)) {
                        stmt0.executeUpdate(updateSQL);
                        num = num + stmt0.getUpdateCount();

                    }
                }
            } catch (NumberFormatException ex) {
                throw new SQLException("NumberFormatException :" + ex.getMessage());
            } finally {
                stmt0.close();
            }
        }
        return num;
    }

    /**
     * �����������ݿ���� �ѵ�һ���ֶε�����������
     *
     * @param tableName ��һ������
     * @param listRecord �Ǿ�����ͬ�ṹ��һ���¼
     * �ǲ�������������ʱ�������ã�����ʱ����where==null||"".equals(where)�����Զ����ݼ�¼���������ֶεļ�ֵ�����where�������
     * �Ǵ�LinkedHashMap�����л�ȡ��ֵ
     * @throws SQLException
     */
    private int _update(String tableName, List<Map> listRecord, String whereStr) throws SQLException {
        int num = 0;
        String[] keyFieldName = new String[]{db.getFields(tableName)[0]};
        num = _update(tableName, listRecord, keyFieldName, false, whereStr);
        return num;
    }

    /**
     * @return ����Connection con����
     */
    @Override
    public Connection getCon() {
        return this.con;
    }

    /**
     * @return �����ж����ݿ������Ƿ�ر�,����true��ʾ�ѹرգ�����false��ʾδ�ر�.
     * @throws SQLException
     *
     */
    @Override
    public boolean isClosed() throws SQLException {
        return con.isClosed();
    }

    /**
     * ��ѯ����
     *
     * @param sqlquery �Ǳ�׼��ѯ���,���������⸴�ӵĶ���ѯ���,����������JDBC API֧�ֵı�׼��ѯ���
     * @return ����ѯ���ResultSet����ת����List&lt;Map&lt;String,Object&gt;&gt;���͵Ľ��
     */
    @Override
    public List query(String sqlquery) throws SQLException {
        List records = new ArrayList();
        Map valueMap = null;
        int fieldCount = 0;
        try {
            stmt = con.createStatement();
            //stmt.setQueryTimeout(timeout);//Access��֧�ִ����
            rs = stmt.executeQuery(sqlquery);
            rsmd = rs.getMetaData();
            fieldCount = rsmd.getColumnCount();
            while (rs.next()) {
                valueMap = new LinkedHashMap();
                for (int i = 1; i <= fieldCount; i++) {
                    String fieldClassName = rsmd.getColumnClassName(i);
                    String fieldName = rsmd.getColumnName(i);
                    this._recordMappingToMap(fieldClassName, fieldName, rs, valueMap);
                }
                records.add(valueMap);
            }
            rs.close();
        } finally {
            stmt.close();
        }
        db.setLastQuerySql(sqlquery);
        return records;
    }

    /**
     * ������ѯһ����¼
     *
     * @param tableName ����
     * @param fields ����ѯ���ֶ�
     * @param row_in_table �Ӹ��п�ʼ��ȡֵ[1,��¼�ܳ���]������λ�÷���0����List&lt;Map&gt;���
     * @return ����List&lt;Map&gt;���͵Ľ��
     */
    public Map indexByRow(String tableName, String[] fields, int row_in_table) throws SQLException {
        String key = db.getFields(tableName)[0];
        if (db.getKeys(tableName).length > 0) {
            key = db.getKeys(tableName)[0];
        }
        IndexNode nodes = _indexNodeOne(tableName, row_in_table);
        Field f = db.getField(tableName, key);
        String sql = "select " + tool.arryToString(fields, ",") + " from  " + tableName + " where " + key + "=" + nodes.getFirstKeyValue();
        if (f.getTypeClassName().equals("java.lang.String")) {
            sql = "select " + tool.arryToString(fields, ",") + " from  " + tableName + " where " + key + " like '" + nodes.getFirstKeyValue() + "'";
        }
        Map m = this.queryOne(sql);
        return m;
    }

    /**
     * ������ѯ������¼
     *
     * @param tableName ����
     * @param fields ����ѯ���ֶ�
     * @param row_in_table �Ӹ��п�ʼ��ȡֵ[1,��¼�ܳ���]������λ�÷���0����List&lt;Map&gt;���
     * @param length ��ѯ���ȣ�row_in_table���¼������length���򷵻�����ȫ����¼
     * @param asc ����˳��true˳��false����
     * @return ����List&lt;Map&gt;���͵Ľ��
     */
    public List indexByRow(String tableName, String[] fields, int row_in_table, int length, boolean asc) throws SQLException {
        String key = db.getFields(tableName)[0];
        if (db.getKeys(tableName).length > 0) {
            key = db.getKeys(tableName)[0];
        }
        List<Map> list = new ArrayList();
        IndexNode[] nodes = _indexNodeTwo(tableName, row_in_table, length);
        Field f = db.getField(tableName, key);
        String sort = "asc";
        if (!asc) {
            sort = "desc";
        }
        String sql = "select " + tool.arryToString(fields, ",") + " from  " + tableName + " where " + key + ">=" + nodes[0].getFirstKeyValue() + " and  " + key + "<=" + nodes[1].getFirstKeyValue() + " order by " + key + " " + sort;
        if (f.getTypeClassName().equals("java.lang.String")) {
            sql = "select " + tool.arryToString(fields, ",") + " from  " + tableName + " where " + key + ">='" + nodes[0].getFirstKeyValue() + "' and  " + key + "<='" + nodes[1].getFirstKeyValue() + "' order by " + key + " " + sort;
        }
        list = this.query(sql);
        return list;
    }

    /**
     * ������ѯ������¼<br/> �����Ѿ������������ڵ������ѯ
     * ���صļ�¼�ǵ�ǰ�ڵ��������ǰһ�ڵ������������Ĭ�ϵĲ����ڵļ�¼����ͨ����1000��<br/>
     * Ĭ�ϲ������Ե���pvo.setIndexNodesStepLength(indexStepLength)������������<br/>
     * �����ڴ����ݷ����ҳ��ѯ
     *
     * @param tableName ����
     * @param fields ����ѯ���ֶ�
     * @param position_of_indexNodes
     * ȡ��position_of_indexNodesλ�ѽ����Ľڵ㣬ȡֵ[0,nodes.length - 1]
     * @param asc ����˳��true˳��false����
     * @param rebuildIndexNodes �ڲ�ѯǰ�Ƿ����´�����������
     * @return ����List&lt;Map&gt;���͵Ľ��
     */
    public List indexByIndexNodes(String tableName, String[] fields, int position_of_indexNodes, boolean asc, boolean rebuildIndexNodes) throws SQLException {
        String key = db.getFields(tableName)[0];
        if (db.getKeys(tableName).length > 0) {
            key = db.getKeys(tableName)[0];
        }
        int step_old = db.getTable(tableName).getIndexStepLength();
        if (this.indexStepLength != step_old) {
            rebuildIndexNodes = true;
        }//���������仯ʱ�������ؽ�
        IndexNode[] nodes = _indexNodes(tableName, rebuildIndexNodes);
        List<Map> list = new ArrayList();
        if (position_of_indexNodes < 0 || position_of_indexNodes >= (nodes.length - 1)) {
            return list;//������Χ
        }
        Field f = db.getField(tableName, key);
        String sort = "asc";
        if (!asc) {
            sort = "desc";
        }
        String sql = "select " + tool.arryToString(fields, ",") + " from  " + tableName + " where " + key + "<=" + nodes[position_of_indexNodes].getFirstKeyValue() + " order by " + key + " " + sort;
        if (f.getTypeClassName().equals("java.lang.String")) {
            sql = "select " + tool.arryToString(fields, ",") + " from  " + tableName + " where " + key + "<='" + nodes[position_of_indexNodes].getFirstKeyValue() + "' order by " + key + " " + sort;
        }
        if (position_of_indexNodes > 0 && position_of_indexNodes <= (nodes.length - 1)) {
            sql = "select " + tool.arryToString(fields, ",") + " from  " + tableName + " where " + key + ">" + nodes[position_of_indexNodes - 1].getFirstKeyValue() + " and " + key + "<=" + nodes[position_of_indexNodes].getFirstKeyValue() + " order by " + key + " " + sort;
            if (f.getTypeClassName().equals("java.lang.String")) {
                sql = "select " + tool.arryToString(fields, ",") + " from  " + tableName + " where " + key + ">'" + nodes[position_of_indexNodes - 1].getFirstKeyValue() + "'and " + key + "<='" + nodes[position_of_indexNodes].getFirstKeyValue() + "' order by " + key + " " + sort;
            }
        }
        db.getTable(tableName).setPosition_of_indexNodes(position_of_indexNodes);//���汾�β�ѯ��λ����Ϣ
        list = this.query(sql);
        return list;
    }

    /**
     * ��ѯһ�������ڵ�
     *
     * @param tableName ����
     * @param row_in_table ������λ�ã�indexRow>=1,indexRow<=��ļ�¼��
     * @return ����IndexNode
     */
    private IndexNode _indexNodeOne(String tableName, int row_in_table) throws SQLException {
        String key = db.getFields(tableName)[0];
        if (db.getKeys(tableName).length > 0) {
            key = db.getKeys(tableName)[0];
        }
        String querySql = "select " + key + " from " + tableName + " order by " + key;
        stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);//���������еĿɹ��������
        rs = stmt.executeQuery(querySql);
        rs.last();
        if (row_in_table > rs.getRow()) {
            return null;
        }
        rs.absolute(row_in_table);
        IndexNode n = new IndexNode();
        n.setFirstKeyValue(rs.getObject(key));
        n.setRow(row_in_table);
        rs.close();
        stmt.close();
        return n;
    }

    /**
     * ��ѯ���������ڵ�
     *
     * @param tableName ����
     * @param row_in_table ������λ�ã�indexRow>=1,indexRow<=��ļ�¼��
     * @param length ���ȣ��ڵ�ĳ���
     * @return ����IndexNode����
     */
    private IndexNode[] _indexNodeTwo(String tableName, int row_in_table, int length) throws SQLException {
        String key = db.getFields(tableName)[0];
        if (db.getKeys(tableName).length > 0) {
            key = db.getKeys(tableName)[0];
        }
        String querySql = "select " + key + " from " + tableName + " order by " + key;
        stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);//���������еĿɹ��������
        rs = stmt.executeQuery(querySql);
        rs.last();
        int allLength = rs.getRow();
        if (row_in_table > allLength) {
            return null;
        }
        rs.absolute(row_in_table);
        IndexNode[] n = new IndexNode[]{new IndexNode(), new IndexNode()};
        n[0].setFirstKeyValue(rs.getObject(key));
        n[0].setRow(row_in_table);

        int row2 = row_in_table + length - 1;
        if (row2 > allLength) {
            row2 = allLength;
        }
        rs.absolute(row2);
        n[1].setFirstKeyValue(rs.getObject(key));
        n[1].setRow(row_in_table);

        rs.close();
        stmt.close();
        return n;
    }

    /**
     * ������������ڵ�����<br/>
     * ʹ��Ĭ�ϲ���1000��ÿ����1000����¼����һ���ڵ㣬���һ���ڵ㱣�����һ����¼��Ϣ��ÿ���ڵ��¼��ĵ�һ������ֵ�Լ����ü�˳���ѯ���������<br/>
     *
     * @param tableName ����
     * @param rebuildAnyway �Ƿ��������ؽ�
     */
    private IndexNode[] _indexNodes(String tableName, boolean rebuildIndexNodes) throws SQLException {
        Table table = db.getTable(tableName);
        String key = db.getFields(tableName)[0];
        if (db.getKeys(tableName).length > 0) {
            key = db.getKeys(tableName)[0];
        }
        int length = (int) this.queryCount(tableName);
        int nodeNums = (int) Math.ceil((double) length / (double) indexStepLength);
        IndexNode[] nodes = db.getTable(tableName).getIndexNodes();
        if (nodes == null) {
            nodes = new IndexNode[nodeNums];
        } else if (nodes.length == 0) {
            nodes = new IndexNode[nodeNums];
        }
        int countInNodes = 0;
        if (nodes[nodes.length - 1] != null) {//======= ====
            countInNodes = nodes[nodes.length - 1].getRow();//���һ���ڵ㱣���˼�¼�ܳ���(������)��Ϣ
        }
        boolean b = false;
        if (table.getIndexStepLength() != indexStepLength) {
            b = true;
            table.setIndexStepLength(indexStepLength);
        } else if (rebuildIndexNodes) {
            b = true;
        } else if (countInNodes == 0) {//���������½�������
            b = true;
        } else if (length > countInNodes) {
            b = true;
        }
        if (b) {
            String querySql = "select " + key + " from " + tableName + " order by " + key;
            stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);//���������еĿɹ��������
            rs = stmt.executeQuery(querySql);
            nodes = new IndexNode[nodeNums];

            int row = indexStepLength;
            for (int i = 0; i < nodes.length - 1; i++) {
                rs.absolute(row);
                nodes[i] = new IndexNode();
                nodes[i].setFirstKeyValue(rs.getObject(key));
                nodes[i].setRow(rs.getRow());//"��һ���ؼ��ֶε�ֵ��Ӧ���к�"
                row += indexStepLength;
            }
            rs.last();
            nodes[nodes.length - 1] = new IndexNode();
            nodes[nodes.length - 1].setFirstKeyValue(rs.getObject(key));
            nodes[nodes.length - 1].setRow(rs.getRow());//"��һ���ؼ��ֶε�ֵ��Ӧ���к�"
            db.getTable(tableName).setIndexNodes(nodes);
        }
        rs.close();
        stmt.close();
        return nodes;
    }

    /**
     * ���������ڵ�����ĳ���
     */
    public int getIndexNodesLength(String tableName) {
        return db.getTable(tableName).getIndexNodes().length;
    }

    /**
     * ���������ڵ�Ĳ���
     */
    public int getIndexNodesStepLength() {
        return indexStepLength;
    }

    /**
     * ���������ڵ�Ĳ���
     */
    public void setIndexNodesStepLength(int indexStepLength) {
        this.indexStepLength = indexStepLength;
    }

    /**
     * ��ͨ����
     *
     * @param sqlquery �Ǳ�׼��ѯ���,���������⸴�ӵĶ���ѯ���,����������JDBC API֧�ֵı�׼��ѯ���
     * @return ��ȡ��ѯ���ResultSet����
     */
    @Override
    public ResultSet queryResultSet(String sqlquery) throws SQLException {
        stmt = con.createStatement();
        rs = stmt.executeQuery(sqlquery);
        return rs;
    }

    /**
     * �߼���ѯ����
     *
     * @param querySql �Ǳ�׼��ѯ���,���������⸴�ӵĶ���ѯ���,����������JDBC API֧�ֵı�׼��ѯ���
     * @param statement
     * �û������Statement��������ʵ�ָ��߼��Ĳ�ѯ����ɸ��µġ��ɹ����ġ�ֻ���ġ����еġ������еģ�<br/>����ͨ�� Statement
     * statement=getCon().createStatement(ResultSet.����0��ResultSet.����1);�ķ�ʽʵ��
     * @return ��ȡ��ѯ���ResultSet����
     */
    @Override
    public ResultSet queryResultSet(String querySql, Statement statement) throws SQLException {
        rs = statement.executeQuery(querySql);
        return rs;
    }

    /**
     * Access��֧�ִ����
     *
     * @deprecated
     * @param sqlquery �Ǳ�׼��ѯ���,���������⸴�ӵĶ���ѯ���,����������JDBC API֧�ֵı�׼��ѯ���
     * @param fetchSize Ԥȡ��
     * @return ��ȡ��ѯ�����ת����List����
     */
    @Override
    public List queryFetchSize(String sqlquery, int fetchSize) throws SQLException {
        List records = new ArrayList();
        return records;
    }

    /**
     * �ڲ�ѯ����л�ȡ��һ����¼
     *
     * @param sqlQuery ��ѯ���
     * @return ��ȡһ����¼��
     * @throws SQLException
     */
    @Override
    public Map queryOne(String sqlQuery) throws SQLException {
        stmt = con.createStatement();
        Map m = new LinkedHashMap();
        rs = stmt.executeQuery(sqlQuery);
        rsmd = rs.getMetaData();
        int fieldCount = rsmd.getColumnCount();
        if (rs.next()) {
            for (int i = 1; i <= fieldCount; i++) {
                String fieldClassName = rsmd.getColumnClassName(i);
                String fieldName = rsmd.getColumnName(i);
                _recordMappingToMap(fieldClassName, fieldName, rs, m);
            }
        }
        rs.close();
        stmt.close();
        db.setLastQuerySql(sqlQuery);
        return m;
    }

    /**
     * @param tableName ����
     * @return ��ȡ������һ����¼
     * @throws SQLException
     */
    @Override
    public Map queryFirstRecord(String tableName) throws SQLException {
        Table t = db.getTable(tableName);
        String fieldName = t.getFields()[0];
        if (t.getKeys().length > 0) {
            fieldName = t.getKeys()[0];
        }
        Field f = db.getField(tableName, fieldName);
        String sql = "select min(" + fieldName + ") as id from " + tableName;
        Map mm = this.queryOne(sql);
        if (f.getTypeClassName().equals("java.lang.String")) {
            sql = "select * from " + tableName + " where " + fieldName + " like '" + mm.get("id") + "'";
        } else {
            sql = "select * from " + tableName + " where " + fieldName + " = " + mm.get("id");
        }
        Map m = this.queryOne(sql);
        return m;
    }

    /**
     * @param tableName ����
     * @return ��ȡ������һ����¼
     * @throws SQLException
     */
    @Override
    public Map queryLastRecord(String tableName) throws SQLException {
        Table t = db.getTable(tableName);
        String fieldName = t.getFields()[0];
        if (t.getKeys().length > 0) {
            fieldName = t.getKeys()[0];
        }
        Field f = db.getField(tableName, fieldName);
        String sql = "select max(" + fieldName + ") as id from " + tableName;
        Map mm = this.queryOne(sql);;
        if (f.getTypeClassName().equals("java.lang.String")) {
            sql = "select * from " + tableName + " where " + fieldName + " like '" + mm.get("id") + "'";
        } else {
            sql = "select * from " + tableName + " where " + fieldName + " = " + mm.get("id");
        }
        Map m = this.queryOne(sql);;
        return m;
    }

    /**
     * @param tableName �Ǳ���;
     * @param fieldName �ؼ��ֶ���;
     * @param fieldValue �ؼ��ֶ�ֵ.
     * @return ����ָ������,��ȡһ����¼,�˷�����Ҫ���ڱ�ǩ��
     * @throws SQLException
     */
    @Override
    public Map queryOne(String tableName, String fieldName, Object fieldValue) throws SQLException {
        String sql = "select * from " + tableName + " where " + fieldName + "=" + fieldValue.toString();
        Field f = db.getField(tableName, fieldName);
        if (f.getTypeClassName().equals("java.lang.String")) {
            sql = "select * from " + tableName + " where " + fieldName + " like '" + fieldValue.toString() + "'";
        }
        return queryOne(sql);
    }

    /**
     * �����ϴβ�ѯ����ѯ
     */
    public List queryByLast(int last) throws SQLException {
        List v = new ArrayList();
        try {
            stmt = con.createStatement();
            String sqlquery = db.getLastQuerySql(last);
            rs = stmt.executeQuery(sqlquery);
            rsmd = rs.getMetaData();
            int fieldCount = rsmd.getColumnCount();
            while (rs.next()) {
                Map valueMap = new LinkedHashMap();
                for (int i = 1; i <= fieldCount; i++) {
                    String fieldClassName = rsmd.getColumnClassName(i);
                    String fieldName = rsmd.getColumnName(i);
                    this._recordMappingToMap(fieldClassName, fieldName, rs, valueMap);
                }
                v.add(valueMap);
            }
            rs.close();
        } finally {
            stmt.close();
        }
        return v;
    }

    /**
     * ���ķ���,��ResultSet������еļ�¼ӳ�䵽Map������.
     *
     * @param fieldClassName ��JDBC API�е���������,
     * @param fieldName ���ֶ�����
     * @param rs ��һ��ResultSet��ѯ�����,
     * @param fieldValue Map����,���ڴ���һ����¼.
     * @throws SQLException
     */
    private void _recordMappingToMap(String fieldClassName, String fieldName, ResultSet rs, Map fieldValue) throws SQLException {
        fieldName = fieldName.toLowerCase();

        //���ȹ��򣺳������Ϳ�ǰ

        if (fieldClassName.equals("java.lang.String")) {
            String s = rs.getString(fieldName);
            if (rs.wasNull()) {
                fieldValue.put(fieldName, null);
            } else {
                fieldValue.put(fieldName, s);
            }
        } else if (fieldClassName.equals("java.lang.Integer")) {
            int s = rs.getInt(fieldName);
            if (rs.wasNull()) {
                fieldValue.put(fieldName, null);
            } else {
                fieldValue.put(fieldName, s);//����jdk��Ҫ��װ��jdk1.5����Ҫ��װ
            }
        } else if (fieldClassName.equals("java.lang.Long")) {
            long s = rs.getLong(fieldName);
            if (rs.wasNull()) {
                fieldValue.put(fieldName, null);
            } else {
                fieldValue.put(fieldName, s);
            }
        } else if (fieldClassName.equals("java.lang.Boolean")) {
            boolean s = rs.getBoolean(fieldName);
            if (rs.wasNull()) {
                fieldValue.put(fieldName, null);
            } else {
                fieldValue.put(fieldName, s);
            }
        } else if (fieldClassName.equals("java.lang.Short")) {
            short s = rs.getShort(fieldName);
            if (rs.wasNull()) {
                fieldValue.put(fieldName, null);
            } else {
                fieldValue.put(fieldName, s);
            }
        } else if (fieldClassName.equals("java.lang.Float")) {
            float s = rs.getFloat(fieldName);
            if (rs.wasNull()) {
                fieldValue.put(fieldName, null);
            } else {
                fieldValue.put(fieldName, s);
            }
        } else if (fieldClassName.equals("java.lang.Double")) {
            double s = rs.getDouble(fieldName);
            if (rs.wasNull()) {
                fieldValue.put(fieldName, null);
            } else {
                fieldValue.put(fieldName, s);
            }
        } else if (fieldClassName.equals("java.sql.Timestamp")) {
            java.sql.Timestamp s = rs.getTimestamp(fieldName);
            if (rs.wasNull()) {
                fieldValue.put(fieldName, null);
            } else {
                fieldValue.put(fieldName, s);
            }
        } else if (fieldClassName.equals("java.sql.Date") || fieldClassName.equals("java.util.Date")) {
            java.util.Date s = rs.getDate(fieldName);
            if (rs.wasNull()) {
                fieldValue.put(fieldName, null);
            } else {
                fieldValue.put(fieldName, s);
            }
        } else if (fieldClassName.equals("java.sql.Time")) {
            java.sql.Time s = rs.getTime(fieldName);
            if (rs.wasNull()) {
                fieldValue.put(fieldName, null);
            } else {
                fieldValue.put(fieldName, s);
            }
        } else if (fieldClassName.equals("java.lang.Byte")) {
            byte s = rs.getByte(fieldName);
            if (rs.wasNull()) {
                fieldValue.put(fieldName, null);
            } else {
                fieldValue.put(fieldName, new Byte(s));
            }
        } else if (fieldClassName.equals("[B") || fieldClassName.equals("byte[]")) {
            //byte[]������SQL Server��
            byte[] s = rs.getBytes(fieldName);
            if (rs.wasNull()) {
                fieldValue.put(fieldName, null);
            } else {
                fieldValue.put(fieldName, s);
            }
        } else if (fieldClassName.equals("java.math.BigDecimal")) {
            BigDecimal s = rs.getBigDecimal(fieldName);
            if (rs.wasNull()) {
                fieldValue.put(fieldName, null);
            } else {
                fieldValue.put(fieldName, s);
            }
        } else if (fieldClassName.equals("java.lang.Object")
                || fieldClassName.equals("oracle.sql.STRUCT")) {
            Object s = rs.getObject(fieldName);
            if (rs.wasNull()) {
                fieldValue.put(fieldName, null);
            } else {
                fieldValue.put(fieldName, s);
            }
        } else if (fieldClassName.equals("java.sql.Array")
                || fieldClassName.equals("oracle.sql.ARRAY")) {
            java.sql.Array s = rs.getArray(fieldName);
            if (rs.wasNull()) {
                fieldValue.put(fieldName, null);
            } else {
                fieldValue.put(fieldName, s);
            }
        } else if (fieldClassName.equals("java.sql.Clob")) {
            java.sql.Clob s = rs.getClob(fieldName);
            if (rs.wasNull()) {
                fieldValue.put(fieldName, null);
            } else {
                fieldValue.put(fieldName, s);
            }
        } else if (fieldClassName.equals("java.sql.Blob")) {
            java.sql.Blob s = rs.getBlob(fieldName);
            if (rs.wasNull()) {
                fieldValue.put(fieldName, null);
            } else {
                fieldValue.put(fieldName, s);
            }
        } else {//���������κ�δ֪���͵Ĵ���
            Object s = rs.getObject(fieldName);
            if (rs.wasNull()) {
                fieldValue.put(fieldName, null);
            } else {
                fieldValue.put(fieldName, s);
            }
        }

    }

    /**
     * �ر����ݿ�����
     *
     * @throws SQLException
     */
    @Override
    public void closeCon() throws SQLException {
        try {
            //if(stmt!=null&&!stmt.isClosed()){stmt.close();}//Access��֧�������жϣ����stmt�ڴ������ķ����ڹر�
        } finally {
            if (con != null && !con.isClosed()) {
                con.close();
            }
        }
    }

    /**
     * ���볤��������
     *
     * @param tableName �Ǳ���,
     * @return
     * ��ǰϵͳʱ��*10000+��λ��Ȼ������2012-11-01ĳʱĳ��ϵͳʱ�䣺1351749144390���޸ĺ�13517491443897256���������ϣ������������ȡʱ�䣬��������ֵ/10000���ɣ���ȷ��1/100���롣
     * @throws SQLException
     *
     */
    @Override
    public Long insertKey(String tableName) throws SQLException {
        Long ID = null;
        long keyValue = 0;
        Map m = new HashMap();
        int b = 0;
        String keyField = db.getTable(tableName).getFields()[0];
        if (db.getTable(tableName).getKeys().length > 0) {
            keyField = db.getTable(tableName).getKeys()[0];
        }
        synchronized (this) {
            keyValue = db.getTable(tableName).makeLongKeyValue();
            m.put(keyField, keyValue);
            b = insert(tableName, m);
        }
        if (b > 0) {
            ID = keyValue;
        }
        return ID;
    }

    /**
     * ͨ�ü�ֵ������������ֵ�������������µļ�ֵ
     * ֧�ֵ��ֶ������У�Long\Integer\Short\Float\Double\String(���ȴ��ڵ���13)
     * �������Զ���1���������Զ���1��ȡ�����ַ��Ͳ���Date��longֵ���磺1348315761671���ֶο��Ӧ�����ڵ���13
     *
     * @return ����һ��ֻ�йؼ���ֵ�ļ�¼,�����ظ�ֵ��
     * @param tableName �Ǳ���,
     * @throws SQLException
     */
    @Override
    public Object insertAutoKey(String tableName) throws SQLException {

        String keyField = db.getTable(tableName).getFields()[0];
        if (db.getTable(tableName).getKeys().length > 0) {
            keyField = db.getTable(tableName).getKeys()[0];
        }
        Field f = db.getField(tableName, keyField);
        String className = f.getTypeClassName();

        Object ID = null;
        Object keyValue = null;
        Map m = new LinkedHashMap();
        int b = 0;
        if (className.equals("java.lang.Long") || className.equals("java.lang.Integer") || className.equals("java.lang.Short") || className.equals("java.lang.Float") || className.equals("java.lang.Double") || className.equals("java.lang.String")) {
            synchronized (this) {
                if ((className.equals("java.lang.String") && Integer.parseInt(f.getSize()) > 13)) {
                    keyValue = "" + (new java.util.Date()).getTime();
                } else {
                    String query = "select max(" + keyField + ") as exp1 from " + tableName;//select max(id) as exp1 from test_table01_on_pvo
                    Map map = this.queryOne(query);//{exp1=20121105}
                    Object _ID = map.get("exp1");
                    keyValue = db.getTable(tableName).makeObjectKeyValue(f, _ID);//makeObjectKeyValue
                }
                m.put(keyField, keyValue);
                b = insert(tableName, m);
                if (b > 0) {
                    ID = keyValue;
                }
            }
        } else {
            throw new SQLException(" not support this type of " + className + " to make keyvalue");
        }

        return ID;
    }

    /**
     * ���ַ�������ʽ�������ͺ��ַ��͵����������ֵ������ֵ�ĸ�ʽ�������ֶ��������
     *
     * @return ����ָ���ļ�ֵ������ɹ��򷵻ظ�ֵ��������ɹ����򷵻�null��
     * @param tableName �Ǳ���,
     * @param keyValue ��ֵ
     * @throws SQLException
     *
     */
    @Override
    public String insertKey(String tableName, String keyValue) throws SQLException {
        String keyField = db.getTable(tableName).getFields()[0];
        if (db.getTable(tableName).getKeys().length > 0) {
            keyField = db.getTable(tableName).getKeys()[0];
        }
        String ID = null;
        Map m = new LinkedHashMap();
        int b = 0;
        synchronized (this) {
            Map mm = this.queryOne("select " + keyField + " from " + tableName + " where " + keyField + " like '" + keyValue + "'");
            if (mm.isEmpty()) {
                String v = db.getTable(tableName).makeStringKeyValue(keyField, keyValue);//���Ϸ��ԡ�Ψһ��
                m.put(keyField, keyValue);
                b = insert(tableName, m);
            } else {
                throw new SQLException("the key field value already being existence. ");
            }
        }
        if (b > 0) {
            ID = keyValue;
        }
        return ID;
    }

    @Override
    public Db getDb() {
        return db;
    }
    /*
     * �ս�������,���һ����ȫ����,�ر����ӡ�������Ա��Ӧ�����˷������ر�����,��������������󣬿���������ݿ����Ӳ��ܹرգ�����б�Ҫ�����ս�������ģʽ�����ڵ�ʵ��֤�����ս��������������õ����á�<br/>
     * 1������������ֹ��<br/>
     * 2��Ӧ�ó����з�����SQLException�쳣��<br/>
     */
    private final Object _finalizerGuardian = new Object() {
        @Override
        protected void finalize() throws Throwable {
            try {
                super.finalize();
            } finally {
                if (con != null) {
                    if (!con.isClosed()) {
                        con.close();
                        System.out.println("Connection closed in finalize,You should close it.");
                    }
                }
            }
        }
    };
}

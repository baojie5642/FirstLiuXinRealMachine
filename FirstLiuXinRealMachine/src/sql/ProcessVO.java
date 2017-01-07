/*
 * ProcessVO.java
 * 2012-11-11
 * Kaiming Hu
 */
package sql;

import cn.jadepool.util.DateTool;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <code>ProcessVO</code>ʵ�ַ����������ݿ�DML(CRUD)����<br/>
 *
 * 1��ProcessVO()ʹ��db.xml���õ����ݿ����ӣ�<br/> 2��ProcessVO(Connection
 * con)��ʹ��ָ�������ݿ����ӣ�<br/> 3���������closeCon()�ر����ӣ�<br/>
 * 4�������û���try{}catch(SQLExceptionex){}finally{}�ṹ�У������񣬵���ProcessVO�е�DML������<br/>
 * 5���ܾ����롢������ͬ�ļ�¼��<br/> 6��֧�ֶ�����²�����<br/>
 *
 *
 * ������Լ����<br/>
 * �������ݿ⣬ʹ��insert��insertXXX��update��delete��query��queryOne��save��index����<br/>
 * ���У�index��indexXXX��һ��������صĲ�ѯ<br/> �����ܱ������׼��sql����һ�¡�
 *
 *
 * @author ������
 * @since jadepool 1.0
 */
public class ProcessVO implements Jdml {

    private java.sql.Connection con;
    private java.sql.Statement stmt;
    private java.sql.ResultSet rs;
    private java.sql.ResultSetMetaData rsmd;
    private Savepoint savepoint;//�������ûع���
    private JadeTool tool = new JadeTool();
    private DateTool dt = new DateTool();
    private DbCenter db = null;//ʵ����ʱ��Dbʹ�õ����ݿ�����������ProcessVOһ��
    private boolean autoCommit;
    private boolean failCommit;//
    private boolean isSupportJTA;//�Ƿ�֧��JTA����
    private boolean showtip;//�Ƿ���ʾSystem.out.println�������ʾ
    //con.isValid(10);//�趨���ݿ�������Ҫ��ɹ�����ʱ�䣬ʱ������
    private WhereString where;
    private QueryString query;
    private int indexStepLength = 1000;

    /**
     * Ĭ�Ϲ��캯��.ʹ��/META-INF/db.xml���õ����ݿ�����
     */
    public ProcessVO() {
        this.con = DbConnection.getDefaultCon();
        db = DbCenter.instance(DbConnection.getDefaultCon(), DbConnectionType.USING_CONFIG_OF_DEFAULT);
        autoCommit = true;
        failCommit = true;
        where = new WhereString(db);
        query = new QueryString(db);
    }

    /**
     * ���캯��.
     *
     * @param con ʹ��ָ�������ݿ�����
     */
    public ProcessVO(Connection con) {
        this.con = con;
        db = DbCenter.instance(con, DbConnectionType.USING_CONFIG_OF_NONE);
        autoCommit = true;
        failCommit = true;
        where = new WhereString(db);
        query = new QueryString(db);
    }

    /**
     * ���캯��.������캯�����Ը���connectionTypeֵ������ͬ��Dbʵ�����������ڲ�ͬ���ݿ�֮�����ݵĽ���
     *
     * @param con ʹ��ָ�������ݿ�����
     * @param connectionType ���ӵ����� ���ӵ����ͣ�Ŀǰ������������<br/>
     * DbConnectionType.USING_CONFIG_OF_DEFAULT��ʵ�ʵ�ֵ1����Ҫdb.xml�����ļ�<br/>
     * DbConnectionType.USING_CONFIG_OF_NONE��ʵ�ʵ�ֵ0����Ҫָ����con<br/>
     * DbConnectionType.USING_DB_01��ʵ�ʵ�ֵ101����Ҫָ����con<br/>
     * DbConnectionType.USING_DB_02��ʵ�ʵ�ֵ102����Ҫָ����con<br/>
     */
    public ProcessVO(Connection con, int connectionType) {
        this.con = con;

        switch (connectionType) {
            case DbConnectionType.USING_CONFIG_OF_JPA: {
                //db = DbCenter.instance(con, DbConnectionType.USING_CONFIG_OF_JPA);
                this.con = con;
                db = DbCenter.instance(con, DbConnectionType.USING_CONFIG_OF_NONE);
                break;
            }
            case DbConnectionType.USING_CONFIG_OF_HIBERNATE: {
                //db = DbCenter.instance(con, DbConnectionType.USING_CONFIG_OF_HIBERNATE);
                this.con = con;
                db = DbCenter.instance(con, DbConnectionType.USING_CONFIG_OF_NONE);
                break;
            }
            case DbConnectionType.USING_CONFIG_OF_MYBATIS: {
                //db = DbCenter.instance(con, DbConnectionType.USING_CONFIG_OF_MYBATIS);
                this.con = con;
                db = DbCenter.instance(con, DbConnectionType.USING_CONFIG_OF_NONE);
                break;
            }
            case DbConnectionType.USING_CONFIG_OF_DEFAULT: {
                this.con = DbConnection.getDefaultCon();
                db = DbCenter.instance(con, DbConnectionType.USING_CONFIG_OF_DEFAULT);
                break;
            }
            case DbConnectionType.USING_CONFIG_OF_NONE: {
                this.con = con;
                db = DbCenter.instance(con, DbConnectionType.USING_CONFIG_OF_NONE);
                break;
            }
            case DbConnectionType.USING_DB_01: {
                this.con = con;
                db = DbCenter.instance(con, DbConnectionType.USING_DB_01);
                break;
            }
            case DbConnectionType.USING_DB_02: {
                this.con = con;
                db = DbCenter.instance(con, DbConnectionType.USING_DB_02);
                break;
            }
            default: {
                this.con = DbConnection.getDefaultCon();
                db = DbCenter.instance(con, DbConnectionType.USING_CONFIG_OF_DEFAULT);
                break;
            }
        }

        autoCommit = true;
        failCommit = true;
        where = new WhereString(db);
        query = new QueryString(db);
    }

    /*
     * ���캯��.
     *
     * @param connectionType ָ�������ļ��Ĺ��캯�� ȡֵ��<br/>
     * DbConnectionType.USING_CONFIG_OF_JPA<br/>
     * DbConnectionType.USING_CONFIG_OF_HIBERNATE<br/>
     * DbConnectionType.USING_CONFIG_OF_MYBATIS<br/>
     * DbConnectionType.USING_DB_01<br/> DbConnectionType.USING_DB_02<br/>
     * @deprecated
    
     public ProcessVO(int connectionType) {
     switch (connectionType) {
     case DbConnectionType.USING_CONFIG_OF_JPA: {
     where = new WhereString(db);
     query = new QueryString(db);
     break;
     }
     case DbConnectionType.USING_CONFIG_OF_HIBERNATE: {
     where = new WhereString(db);
     query = new QueryString(db);
     break;
     }
     case DbConnectionType.USING_CONFIG_OF_MYBATIS: {
     where = new WhereString(db);
     query = new QueryString(db);
     break;
     }
     default: {
     break;
     }
     }
     } 
     */
    /**
     * ����������뼶��Ĭ�ϵ�������뼶����Connection.TRANSACTION_READ_COMMITTED�����������Connection���ĵ�
     * <pre>
     *         Connection.TRANSACTION_NONE=              0;        ����ʹ��
     *         Connection.TRANSACTION_READ_UNCOMMITTED = 1;        dirty reads, non-repeatable reads and phantom reads can occur
     *         Connection.TRANSACTION_READ_COMMITTED   = 2;        dirty reads are prevented; non-repeatable reads and phantom reads can occur
     *         Connection.TRANSACTION_REPEATABLE_READ  = 4;        dirty reads and non-repeatable reads are prevented; phantom reads can occur.
     *         Connection.TRANSACTION_SERIALIZABLE     = 8;        dirty reads, non-repeatable reads and phantom reads are prevented
     * </pre>
     *
     * @param transactionIsolation ��ȡֵӦ����Connection�����ĳ���֮һ :
     */
    public void setTransactionIsolation(int transactionIsolation) throws SQLException {
        con.setTransactionIsolation(transactionIsolation);
    }

    /**
     * �������񱣴�㡣
     *
     * @throws SQLException
     */
    public Savepoint setSavepoint() throws SQLException {
        savepoint = con.setSavepoint();
        return savepoint;
    }

    /**
     * �������񱣴�㡣
     *
     * @param point ���������
     * @throws SQLException
     */
    public Savepoint setSavepoint(String point) throws SQLException {
        savepoint = con.setSavepoint(point);
        return savepoint;
    }

    /**
     * �ͷ����񱣴�㡣
     *
     * @throws SQLException
     */
    public void releaseSavepoint() throws SQLException {
        if (savepoint != null) {
            con.releaseSavepoint(savepoint);
        }
    }

    /**
     * �ύ����
     *
     * @throws SQLException
     */
    public void commit() throws SQLException {
        if (!con.isClosed()) {
            if (!autoCommit) {
                try {
                    con.commit();
                    autoCommit = true;
                    con.setAutoCommit(autoCommit);
                    failCommit = false;
                    con.close();
                } finally {
                    if (failCommit) {
                        rollback();
                    }
                }
            }
        }
    }

    /**
     * ����ع���
     *
     * @throws SQLException
     */
    public void rollback() throws SQLException {
        if (!con.isClosed()) {
            if (!this.autoCommit) {
                if (failCommit) {
                    if (savepoint == null) {
                        con.rollback();
                        con.close();
                        System.out.println("[" + dt.dateTime() + "] SQLServerException. Connection is already rollback and closed .");
                    } else {
                        con.rollback(savepoint);
                        con.close();
                        System.out.println("[" + dt.dateTime() + "] SQLServerException. Connection is already rollback at " + savepoint + " point and closed .");
                    }
                    autoCommit = true;
                }
            }
        }
    }

    /**
     * �����ж����ݿ������Ƿ�ʹ��������
     *
     * @throws SQLException
     */
    public boolean isAutoCommit() throws SQLException {
        return con.getAutoCommit();
    }

    /**
     * �趨������;
     *
     * @param autoCommit false��������true�ر�����
     * @throws SQLException
     */
    public void setAutoCommit(boolean autoCommit) throws SQLException {
        con.setAutoCommit(autoCommit);
        this.autoCommit = autoCommit;
    }

    /**
     * �Ƿ�ر������ݿ�����
     *
     * @return �����ж����ݿ������Ƿ�ر�,����true��ʾ�ѹرգ�����false��ʾδ�ر�.
     * @throws SQLException
     *
     */
    @Override
    public boolean isClosed() throws SQLException {
        return con.isClosed();
    }

    /**
     * �ر����ݿ�����
     *
     * @throws SQLException
     */
    @Override
    public void closeCon() throws SQLException {
        try {
            /*
             * if (rs != null && !rs.isClosed()) { rs.close(); }
             *
             * if (stmt != null && !stmt.isClosed()) { stmt.close(); }
             */
        } finally {
            if (con != null && !con.isClosed()) {
                con.close();
            }
        }
    }

    /**
     * �������ݿ�����
     *
     * @return ����Connection con����
     */
    @Override
    public Connection getCon() {
        return this.con;
    }

    /**
     * ����Dbʵ�����Ա����û���ѯ���ݿ⼰����ֶεĽṹ��Ϣ
     *
     * @return ��ǰʹ�õ�Dbʵ��
     */
    @Override
    public DbCenter getDb() {
        return db;
    }

    //CRUD��ʼ
    /**
     * �����¼
     *
     * @param insertSql �������
     * @return ����JDBC��׼�������ķ���ֵ
     * @throws SQLException
     */
    @Override
    public int insert(String insertSql) throws SQLException {
        int num = this.update(insertSql);
        return num;
    }

    /**
     * ����һ���¼
     *
     * @param insertSql �����������
     * @return ����JDBC��׼�������ķ���ֵ
     * @throws SQLException
     */
    @Override
    public int insert(String[] insertSql) throws SQLException {
        int num = this.update(insertSql);
        return num;
    }

    /**
     * ����һ����¼
     *
     * @param tableName �Ǳ���
     * @param mapRecord
     * ��׼�����뵽���е�һ����¼������,��������ֶ�����ͬ,˳���޹�;�������������ֶ���������������ƣ��Զ�������Ч�ֶ�����
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
     * ����һ����¼
     *
     * @param tableName �Ǳ���
     * @param mapRecord ��׼�����뵽���е�һ����¼������,��������ֶ�����ͬ,˳���޹� ,�Զ�������Ч�ֶ�����
     * @param autoInsertKey ֵΪtrueʱ���Զ���������
     * @return ����JDBC��׼�������ķ���ֵ
     * @throws SQLException
     */
    @Override
    public int insert(String tableName, Map<String, Object> mapRecord, boolean autoInsertKey) throws SQLException {
        int num = 0;
        List a = new ArrayList();
        a.add(mapRecord);
        num = this._preparedStatementInsert(tableName, a, autoInsertKey);
        return num;
    }

    /**
     * ���������¼
     *
     * @param tableName �Ǳ���
     * @param listRecord ��׼�����뵽���еĶ�����¼������,��������ֶ�����ͬ,˳���޹� ,�Զ�������Ч�ֶ�����
     * @return ����JDBC��׼�������ķ���ֵ
     * @throws SQLException
     */
    @Override
    public int insert(String tableName, List<Map> listRecord) throws SQLException {
        int num = this._preparedStatementInsert(tableName, listRecord, false);
        return num;
    }

    /**
     * ���������¼
     *
     * @param tableName �Ǳ���
     * @param listRecord ��׼�����뵽���еĶ�����¼������,��������ֶ�����ͬ,˳���޹� ,�Զ�������Ч�ֶ�����
     * @param autoInsertKey ֵΪtrueʱ���Զ���������
     * @return ����JDBC��׼�������ķ���ֵ
     * @throws SQLException
     */
    @Override
    public int insert(String tableName, List<Map> listRecord, boolean autoInsertKey) throws SQLException {
        int num = this._preparedStatementInsert(tableName, listRecord, true);
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
            stmt.cancel();//javaDB not supported  this feature
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
            stmt.cancel();//javaDB not supported  this feature
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
            stmt.cancel();//javaDB not supported  this feature
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
        int count = -1;
        try {
            stmt = con.createStatement();
            for (int i = 0; i < updateSql.length; i++) {
                stmt.executeUpdate(updateSql[i]);
            }
            count = stmt.getUpdateCount();
        } catch (SQLException ex) {
            stmt.cancel();//javaDB not supported  this feature
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
        return this._preparedStatementUpdate(tableName, a, false, null);
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
        return this._preparedStatementUpdate(tableName, a, false, where);
    }

    /**
     * ͨ��PreparedStatement�Զ����¶�����¼
     *
     * @param tableName ����
     * @param listRecord �������������ֶεļ�¼��
     * @return ���ر�׼update�����ķ���ֵ
     * @throws SQLException
     */
    @Override
    public int update(String tableName, List<Map> listRecord) throws SQLException {
        int num = 0;
        num = _preparedStatementUpdate(tableName, listRecord, false, null);
        return num;
    }

    /**
     * ����һ����¼.
     * ����ü�¼���ڣ������֮������ü�¼�����ڣ�����롣�����һ��������ֵΪnull||""�����Զ������µ�����ֵ������������ʺ϶Ժ��������ı���в������������Ӱ��Զ������ı���и���
     *
     * @param tableName �Ǳ���
     * @param listRecord ��׼�����뵽���еĶ�����¼������,��������ֶ�����ͬ,˳���޹�;�������������ֶ����������������;
     * @param where ��һ����׼��where�Ӿ�.
     * @return ���ر�׼update�������ص�״ֵ̬
     * @throws SQLException
     */
    @Override
    public int update(String tableName, List<Map> listRecord, String where) throws SQLException {
        int num = 0;
        num = _preparedStatementUpdate(tableName, listRecord, false, where);
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
        Object firstKeyValue = null;
        int n = 0;
        String _w = "";
        String[] keys = db.getKeys(tableName);
        String[] fields = db.getFields(tableName);
        if (keys.length == 0) {
            n = this.insert(tableName, mapRecord);//�������ı����ֱ�Ӳ���
            if (n > 0) {
                firstKeyValue = mapRecord.get(fields[0]);
            }//û���������򷵻ص�һ���ֶ�ֵ
        } else {
            firstKeyValue = mapRecord.get(keys[0].toString());
            if (firstKeyValue != null & !"".equals(firstKeyValue)) {
                Object[] recordFields = mapRecord.keySet().toArray();
                Map _key_m = new LinkedHashMap();
                for (int i = 0; i < recordFields.length; i++) {
                    if (tool.isInFields(keys, recordFields[i].toString())) {
                        _key_m.put(recordFields[i].toString(), mapRecord.get(recordFields[i].toString()));//��ȡ��¼�е������ֶ�
                    }
                }
                if (!_key_m.isEmpty()) {
                    Object[] _k = _key_m.keySet().toArray();
                    if (_k.length != keys.length) {
                        return null;//����ж������������¼��ȱ�ٶ���������򲻱���
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
            } else {// if (firstKeyValue == null || "".equals(firstKeyValue))
                Field f = db.getField(tableName, keys[0]);
                String className = f.getTypeClassName();
                if (className.equals("java.lang.Long")) {
                    firstKeyValue = insertKey(tableName);
                    mapRecord.put(keys[0], firstKeyValue);
                    n = update(tableName, mapRecord);//��������������¼�в���������¼||������¼ֵ��null||����ֵ��""������룬���Զ���������

                }
                if (className.equals("java.lang.Integer") || className.equals("java.lang.Short") || className.equals("java.lang.Float") || className.equals("java.lang.Double") || className.equals("java.lang.String")) {
                    firstKeyValue = insertAutoKey(tableName);
                    mapRecord.put(keys[0], firstKeyValue);
                    n = update(tableName, mapRecord);//��������������¼�в���������¼||������¼ֵ��null||����ֵ��""������룬���Զ���������
                }
            }
        }
        return firstKeyValue;
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
    @Override
    public int save(String tableName, Map<String, Object> mapRecord) throws SQLException {
        int num = 0;
        String _w = "";
        String[] keys = db.getKeys(tableName);
        if (keys.length == 0) {
            return this.insert(tableName, mapRecord);//�������ı����ֱ�Ӳ���
        } else {
            Object kv = mapRecord.get(keys[0].toString());
            if (kv == null || "".equals(kv)) {
                num = insert(tableName, mapRecord, true);//��������������¼�в���������¼||������¼ֵ��null||����ֵ��""������룬���Զ���������
            } else {
                Object[] recordFields = mapRecord.keySet().toArray();
                Map _key_m = new LinkedHashMap();
                for (int i = 0; i < recordFields.length; i++) {
                    if (tool.isInFields(keys, recordFields[i].toString())) {
                        _key_m.put(recordFields[i].toString(), mapRecord.get(recordFields[i].toString()));//��ȡ��¼�е������ֶ�
                    }
                }
                if (!_key_m.isEmpty()) {
                    Object[] _k = _key_m.keySet().toArray();
                    if (_k.length != keys.length) {
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
     * ɾ����¼�� ִ��Statement��executeUpdate����
     *
     * @param deleteSql ��һ����׼��SQLɾ�����
     * @return ���ر�׼�������ķ���ֵ
     * @throws SQLException
     */
    @Override
    public int delete(String deleteSql) throws SQLException {
        return this.update(deleteSql);
    }

    /**
     * ɾ����¼�� ִ��Statement��executeUpdate����
     *
     * @param deleteSql �Ƕ�����׼��SQLɾ�����
     * @return ���ر�׼�������ķ���ֵ
     * @throws SQLException
     */
    @Override
    public int delete(String[] deleteSql) throws SQLException {
        return this.update(deleteSql);
    }

    /**
     * ʹ��PreparedStatement���������¼ �������Ǹ���JDBC API�������ж�����������<br/>
     *
     * Ϊ�˷�������ݵ�¼��,�������ṩ�����ַ��������Integer|Long|Short|Float|Double|Bigdecimal|java.sql.Date�ȳ������͵�֧��,<br/>
     * ����ע��:���ַ������Dateʱ,Ŀǰjava.sql.Date��֧�ֽ�"yyyy-mm-dd"��ʽת����Date����;<br/>
     * ������������,�û����뽨����Ӧ���͵Ķ���; �������ṩ�˶��ϴ��ļ���֧��; �㳤���ַ���������Ϊnull��<br/>
     *
     *
     *
     * ��ɲ���Ĳ��裺 1�����˼�¼����Ч�ֶΣ�����Ч�ֶ�Object[] fields<br/>
     * 2������isUpdateKey����Ч�ֶμ������飺trueʱ���ٵ������ֶ���Object[]
     * keys�ͷ������ֶ���Object[]fields<br/> 3���Զ���ϳ�PreparedStatement����Ҫ�Ĳ������<br/>
     * 4��������¼for (Map record : listRecord) {}<br/>
     * 5��ִ��PreparedStatement��executeUpdate(updateSQL);<br/>
     *
     *
     * @param tableName ��һ������
     * @param listRecord �Ǿ�����ͬ�ṹ��һ���¼
     * @param autoInsertKey ֵΪtrueʱ���Զ���������
     * �ǲ�������������ʱ�������ã�����ʱ����where==null||"".equals(where)�����Զ����ݼ�¼���������ֶεļ�ֵ�����where�������
     * �Ǵ�LinkedHashMap�����л�ȡ��ֵ
     * @throws SQLException
     */
    private int _preparedStatementInsert(String tableName, List<Map> listRecord, boolean autoInsertKey) throws SQLException {
        int num = 0;
        if (listRecord == null || listRecord.isEmpty()) {
            return num;
        }
        String preparedStatementInsert = "";

        Map<String, Object> _m = new LinkedHashMap(listRecord.get(0));//��ȡһ����¼����Ϊ����preparedStatementSQL��������
        Object maxFieldValue = null;
        String[] tableFields = db.getFields(tableName);//���е��ֶ����ļ���
        String[] tableKeys = db.getKeys(tableName);//���е������ֶμ�
        if (autoInsertKey) {
            Map lastRecord = this.queryLastRecord(tableName);//׼���Զ����������
            if (tableKeys.length > 0) {
                maxFieldValue = lastRecord.get(tableKeys[0]);
                _m.put(tableKeys[0], "");//��ֹ��¼�в���tableKeys[0]�����ֶ�
            } else {
                maxFieldValue = lastRecord.get(tableFields[0]);
                _m.put(tableFields[0], "");//��ֹ��¼�в���tableKeys[0]�����ֶ�
            }
        }
        Object[] recordFields = (_m.keySet()).toArray(); //��ȡ��¼����ֶ����ļ���
        for (int i = 0; i < recordFields.length; i++) {
            if (!tool.isInFields(tableFields, recordFields[i].toString())) {
                _m.remove(recordFields[i].toString());//�Ƴ���Ч�ֶΣ� �鿴��¼�е��ֶ��ڱ����Ƿ���ڣ���������ڣ����Ƴ���
            }
        }

        Object[] fields = (_m.keySet()).toArray(); //���˺����Ч�ֶ�
        String[] values = new String[fields.length]; //����ͨ���'?'
        for (int i = 0; i < fields.length; i++) {
            values[i] = "?";
        }

        String sql_field = tool.arryToString(fields, ",");
        String sql_values = tool.arryToString(values, ",");
        preparedStatementInsert = "insert into " + tableName + " (" + sql_field + " ) values (" + sql_values + ")";//�ϳ�preparedStatement���
        PreparedStatement pstmt = null;

        try {
            pstmt = con.prepareStatement(preparedStatementInsert);//���¼����prepareStatement�������
            long firstStringKey = (new java.util.Date()).getTime();

            //Ϊ���������Ч�ʣ��涨�ж���������˳�򣺳��ñ�׼������ȷƥ�䡢��׼������ȷƥ�䡢�Ǳ�׼������ȷƥ�䡢�Ǳ�׼��������ƥ�䡢�Ǳ�׼��������Сдƥ��
            for (Map record : listRecord) {

                if (autoInsertKey) {
                    if (tableKeys.length > 0) {
                        Field keyF = db.getField(tableName, tableKeys[0]);
                        if ("java.lang.Long".equals(keyF.getTypeClassName())) {//ʱ��+4λ����
                            record.put(tableKeys[0], db.getTable(tableName).makeLongKeyValue());
                        } else if ("java.lang.String".equals(keyF.getTypeClassName())) {
                            if (Integer.parseInt(keyF.getSize()) > 13) {
                                record.put(tableKeys[0], "" + (firstStringKey + 1));//�Զ������ַ�������ֵ//if (className.equals("java.lang.String") && Integer.parseInt(f.getSize()) > 13) 
                            } else {
                                return num;
                            }
                        } else {
                            maxFieldValue = db.getTable(tableName).makeObjectKeyValue(keyF, maxFieldValue);
                            record.put(tableKeys[0], maxFieldValue);//�Զ�������ֵ������ֵ
                        }
                    } else {
                        Field keyF = db.getField(tableName, tableFields[0]);
                        if ("java.lang.Long".equals(keyF.getTypeClassName())) {//ʱ��+4λ����
                            record.put(tableFields[0], db.getTable(tableName).makeLongKeyValue());
                        } else if ("java.lang.String".equals(keyF.getTypeClassName())) {
                            if (Integer.parseInt(keyF.getSize()) > 13) {
                                record.put(tableFields[0], "" + (firstStringKey + 1));//�Զ������ַ�������ֵ//if (className.equals("java.lang.String") && Integer.parseInt(f.getSize()) > 13) 
                            } else {
                                return num;
                            }
                        } else {
                            maxFieldValue = db.getTable(tableName).makeObjectKeyValue(keyF, maxFieldValue);
                            record.put(tableFields[0], maxFieldValue);//�Զ�������ֵ������ֵ
                        }
                    }
                }

                for (int i = 0; i < fields.length; i++) {
                    Field f = db.getField(tableName, fields[i].toString());
                    String className = f.getTypeClassName();
                    int index = f.getSqlType();
                    Object v = record.get(fields[i].toString());

                    if (v == null) {
                        pstmt.setNull(i + 1, index);//continue;
                    } else if (v != null) {
                        String _c = ((Class) v.getClass()).getName(); //���ӶԱ����ݵ�֧��,�ڱ��л�ȡ�����ݾ�ΪString��,��Ӧ�������ת��.
                        if ((_c.equals("java.lang.String")) && ("".equals(((String) v).trim()))) {
                            pstmt.setNull(i + 1, index);//continue;
                        } else {
                            if (className.equals("java.lang.String")) {
                                if ((_c.equals("java.lang.String")) && (!"".equals(((String) v).trim()))) {
                                    pstmt.setString(i + 1, (String) v);
                                }
                                continue;
                            }

                            if (className.equals("java.lang.Integer")) {
                                if ((_c.equals("java.lang.String")) && (!"".equals(((String) v).trim()))) {
                                    pstmt.setInt(i + 1, Integer.parseInt((String) v));
                                } else {
                                    if (_c.equals("java.lang.Integer")) {
                                        pstmt.setInt(i + 1, ((Integer) v).intValue());
                                    } else {
                                        Integer n = new Integer(v.toString());
                                        pstmt.setInt(i + 1, n);
                                    }
                                }
                                continue;
                            }

                            if (className.equals("java.lang.Long")) {
                                if ((_c.equals("java.lang.String")) && (!"".equals(((String) v).trim()))) {
                                    pstmt.setLong(i + 1, Long.parseLong((String) v));
                                } else {
                                    if (_c.equals("java.lang.Long")) {
                                        pstmt.setLong(i + 1, ((Long) v).longValue());
                                    } else {
                                        Long l = new Long(v.toString());
                                        pstmt.setLong(i + 1, l);
                                    }
                                }
                                continue;
                            }

                            if (className.equals("java.lang.Short")) {
                                if ((_c.equals("java.lang.String")) && (!"".equals(((String) v).trim()))) {
                                    pstmt.setShort(i + 1, Short.parseShort((String) v));
                                } else {
                                    pstmt.setShort(i + 1, ((Short) v).shortValue());
                                }
                                continue;
                            }

                            if (className.equals("java.lang.Float")) {
                                if ((_c.equals("java.lang.String")) && (!"".equals(((String) v).trim()))) {
                                    pstmt.setFloat(i + 1, Float.parseFloat((String) v));
                                } else {
                                    pstmt.setFloat(i + 1, ((Float) v).floatValue());
                                }
                                continue;
                            }

                            if (className.equals("java.lang.Double")) {
                                if ((_c.equals("java.lang.String")) && (!"".equals(((String) v).trim()))) {
                                    pstmt.setDouble(i + 1, Double.parseDouble((String) v));
                                } else {
                                    pstmt.setDouble(i + 1, ((Double) v).doubleValue());
                                }
                                continue;
                            }

                            if (className.equals("java.lang.Boolean")) {
                                if ((_c.equals("java.lang.String")) && (!"".equals(((String) v).trim()))) {
                                    pstmt.setBoolean(i + 1, (Boolean.valueOf((String) v)).booleanValue());
                                } else {
                                    pstmt.setBoolean(i + 1, ((Boolean) v).booleanValue());
                                }
                                continue;
                            }

                            if (className.equals("java.sql.Timestamp")) {
                                if ((_c.equals("java.lang.String")) && (!"".equals(((String) v).trim()))) {
                                    String _s = ((String) v).trim();
                                    if (tool.matches(RegexType.chinaDate, _s)) {//�磺2012-01-24
                                        Time t = new Time(0l);
                                        _s = _s + " " + t.toString();
                                        pstmt.setTimestamp(i + 1, java.sql.Timestamp.valueOf(_s));
                                    } else {
                                        pstmt.setTimestamp(i + 1, java.sql.Timestamp.valueOf((String) v));
                                    }
                                } else if (_c.equals("java.sql.Date")) {
                                    java.sql.Date _v = (java.sql.Date) v;
                                    pstmt.setTimestamp(i + 1, new Timestamp(_v.getTime()));
                                } else if (_c.equals("java.util.Date")) {
                                    java.util.Date _v = (java.util.Date) v;
                                    pstmt.setTimestamp(i + 1, new Timestamp(_v.getTime()));
                                } else if (_c.equals("java.sql.Time")) {
                                    java.sql.Time _v = (java.sql.Time) v;
                                    pstmt.setTimestamp(i + 1, new Timestamp(_v.getTime()));
                                } else {
                                    pstmt.setTimestamp(i + 1, new Timestamp(((java.util.Date) v).getTime()));//��֧�ָ����Ӧ��
                                    //pstmt.setTimestamp(i + 1,  (java.sql.Timestamp) v);//ʹ��jsf����ת�����õĽ�����ܲ���������ʱ�����ת���쳣
                                }
                                continue;
                            }

                            if (className.equals("java.sql.Date")) {
                                if ((_c.equals("java.lang.String")) && (!"".equals(((String) v).trim()))) {
                                    pstmt.setDate(i + 1, java.sql.Date.valueOf((String) v));
                                } else if (_c.equals("java.util.Date")) {
                                    java.util.Date _v = (java.util.Date) v;
                                    pstmt.setDate(i + 1, new java.sql.Date(_v.getTime()));
                                } else {
                                    pstmt.setDate(i + 1, (java.sql.Date) v);
                                }
                                continue;
                            }

                            if (className.equals("java.util.Date")) {
                                if ((_c.equals("java.lang.String")) && (!"".equals(((String) v).trim()))) {
                                    pstmt.setDate(i + 1, java.sql.Date.valueOf((String) v));
                                } else if (_c.equals("java.util.Date")) {
                                    java.util.Date _v = (java.util.Date) v;
                                    pstmt.setDate(i + 1, new java.sql.Date(_v.getTime()));
                                } else {
                                    pstmt.setDate(i + 1, (java.sql.Date) v);
                                }
                                continue;
                            }

                            if (className.equals("java.sql.Time")) {
                                if ((_c.equals("java.lang.String")) && (!"".equals(((String) v).trim()))) {
                                    pstmt.setTime(i + 1, java.sql.Time.valueOf((String) v));
                                } else if (_c.equals("java.util.Date")) {
                                    java.util.Date _v = (java.util.Date) v;
                                    DateFormat df = new SimpleDateFormat("HH:mm:ss");
                                    String _dt = df.format(_v);
                                    pstmt.setTime(i + 1, java.sql.Time.valueOf(_dt));
                                } else {
                                    pstmt.setTime(i + 1, (java.sql.Time) v);
                                }
                                continue;
                            }

                            if (className.equals("[B") || className.equals("byte[]")) {
                                //SQL Server ��image\timestamp\binary������byte[],MySQL ��blobϵ����java.lang.Object,Sybase��image��[B
                                if ((_c.equals("java.lang.String")) && (!"".equals(((String) v).trim()))) {
                                    pstmt.setBytes(i + 1, ((String) v).getBytes());
                                } else {
                                    pstmt.setBytes(i + 1, (byte[]) v);
                                }
                                continue;
                            }
                            if (className.equals("java.sql.Blob")) {
                                //SQL Server ��image\timestamp\binary������byte[],MySQL ��blobϵ����java.lang.Object,Sybase��image��[B
                                if ((_c.equals("java.lang.String")) && (!"".equals(((String) v).trim()))) {
                                    pstmt.setBytes(i + 1, ((String) v).getBytes());
                                } else {
                                    pstmt.setBlob(i + 1, (java.sql.Blob) v);
                                }
                                continue;
                            }

                            if (className.equals("java.lang.Object")) {
                                //SQL Server ��image\timestamp\binary������byte[],MySQL ��blobϵ����java.lang.Object,Sybase��image��[B
                                if ((_c.equals("java.lang.String")) && (!"".equals(((String) v).trim()))) {
                                    pstmt.setBytes(i + 1, ((String) v).getBytes());
                                } else {
                                    pstmt.setObject(i + 1, v);
                                }
                                continue;
                            }

                            if (className.equals("java.lang.Byte")) {
                                //MySQL��tinyint��java.lang.Byte
                                if ((_c.equals("java.lang.String")) && (!"".equals(((String) v).trim()))) {
                                    pstmt.setByte(i + 1, java.lang.Byte.parseByte((String) v));
                                } else {
                                    pstmt.setByte(i + 1, java.lang.Byte.parseByte(v.toString()));
                                }
                                continue;
                            }

                            if (className.equals("java.math.BigDecimal")) {
                                if ((_c.equals("java.lang.String")) && (!"".equals(((String) v).trim()))) {
                                    pstmt.setBigDecimal(i + 1, new BigDecimal((String) v));
                                } else if ((_c.equals("java.lang.Double"))) {
                                    pstmt.setBigDecimal(i + 1, new BigDecimal((Double) v));
                                } else if ((_c.equals("java.lang.Float"))) {
                                    double _v = (Double) v;
                                    pstmt.setBigDecimal(i + 1, new BigDecimal(_v));
                                } else if ((_c.equals("java.lang.Integer"))) {
                                    int _v = (Integer) v;
                                    pstmt.setBigDecimal(i + 1, new BigDecimal(_v));
                                } else if ((_c.equals("java.lang.Long"))) {
                                    long _v = (Long) v;
                                    pstmt.setBigDecimal(i + 1, new BigDecimal(_v));
                                } else if ((_c.equals("java.math.BigInteger"))) {
                                    java.math.BigInteger _v = (java.math.BigInteger) v;
                                    pstmt.setBigDecimal(i + 1, new BigDecimal(_v));
                                } else if ((_c.equals("[C"))) {
                                    char[] _v = (char[]) v;
                                    pstmt.setBigDecimal(i + 1, new BigDecimal(_v));
                                } else {
                                    pstmt.setBigDecimal(i + 1, (BigDecimal) v);
                                }
                                continue;
                            }

                            //���²��ֽ����ݾ�������ݿ���Ҫ����,�д���֤
                            if (className.equals("java.sql.Clob")) {
                                if ((_c.equals("java.lang.String")) && (!"".equals(((String) v).trim()))) {
                                    pstmt.setString(i + 1, (String) v);//��clob���͵��ֶθ����ַ�����
                                } else {
                                    pstmt.setClob(i + 1, (java.sql.Clob) v);
                                }
                                continue;
                            }

                            //���²��ֽ����ݾ�������ݿ���Ҫ����,�д���֤
                            if (className.equals("java.sql.Array")) {
                                if ((_c.equals("java.lang.String")) && (!"".equals(((String) v).trim()))) {
                                    //
                                } else {
                                    pstmt.setArray(i + 1, (java.sql.Array) v);
                                }
                                continue;
                            }

                            //�������ͣ��ǳ��ã��ú�
                            if (className.equals("com.sybase.jdbc2.tds.SybTimestamp") || className.toLowerCase().indexOf("timestamp") > 0) {
                                if ((_c.equals("java.lang.String")) && (!"".equals(((String) v).trim()))) {
                                    pstmt.setTimestamp(i + 1, java.sql.Timestamp.valueOf((String) v));
                                } else {
                                    pstmt.setTimestamp(i + 1, (java.sql.Timestamp) v);
                                }
                                continue;
                            }

                            //����ƥ��
                            if (className.toLowerCase().indexOf("date") > 0) {
                                if ((_c.equals("java.lang.String")) && (!"".equals(((String) v).trim()))) {
                                    pstmt.setDate(i + 1, java.sql.Date.valueOf((String) v));
                                } else {
                                    pstmt.setDate(i + 1, new java.sql.Date(((java.util.Date) v).getTime()));
                                }
                                continue;
                            }

                            if (className.toLowerCase().indexOf("time") > 0) {
                                if ((_c.equals("java.lang.String")) && (!"".equals(((String) v).trim()))) {
                                    pstmt.setTime(i + 1, java.sql.Time.valueOf((String) v));
                                } else {
                                    pstmt.setTime(i + 1, (java.sql.Time) v);
                                }
                                continue;
                            }

                            if (_c.equals("java.io.FileInputStream")) {
                                //�����磺FileInputStream in = new FileInputStream("D:\\test.jpg");�Ľ��
                                pstmt.setBinaryStream(i + 1, (FileInputStream) v, ((FileInputStream) v).available());
                                continue;
                            }//java.io.FileInputStream

                            //�����������ͣ��ǳ��ã��ú�
                        }
                    }
                }
                num = num + pstmt.executeUpdate();
            }
        } catch (java.lang.ClassCastException ex) {
            throw new SQLException("java.lang.ClassCastException: " + ex.getMessage(), ex.getCause());
        } catch (NumberFormatException ex) {
            throw new SQLException("NumberFormatException: " + ex.getMessage(), ex.getCause());
        } catch (IOException ex) {
            throw new SQLException("IOException: " + ex.getMessage(), ex.getCause());
        } finally {
            pstmt.close();
        }
        return num;
    }

    /**
     * ʹ��PreparedStatement���¶�����¼ �������Ǹ���JDBC API�������ж�����������<br/>
     * Ϊ�˷�������ݵ�¼��,�������ṩ�����ַ��������Integer|Long|Short|Float|Double|Bigdecimal|java.sql.Date�ȳ������͵�֧��,<br/>
     * ����ע��:���ַ������Dateʱ,Ŀǰjava.sql.Date��֧�ֽ�"yyyy-mm-dd"��ʽת����Date����;<br/>
     * ������������,�û����뽨����Ӧ���͵Ķ���; �������ṩ�˶��ϴ��ļ���֧��; �㳤���ַ���������Ϊnull��<br/>
     * ��ϸ���������ͣ��ɲ��ġ�Java�����������p700 PreparedStatement��setXXX����<br/>
     *
     *
     * ��ɸ��µĲ��裺 1�����˼�¼����Ч�ֶΣ�����Ч�ֶ�Object[] fields<br/>
     * 2������isUpdateKey����Ч�ֶμ������飺!trueʱ���ٵ������ֶ���Object[] keys�ͷ������ֶ���Object[]
     * fields<br/> 3���Զ�����PreparedStatement����ĸ��²���<br/> 4��������¼for (Map record :
     * listRecord) {}<br/> 5��ִ��PreparedStatement��executeUpdate(updateSQL);<br/>
     *
     *
     * @param tableName ��һ������
     * @param listRecord �Ǿ�����ͬ�ṹ��һ���¼
     * @param isUpdateKey �Ƿ��������
     * @param whereStr
     * �ǲ�������������ʱ�������ã�����ʱ����where==null||"".equals(where)�����Զ����ݼ�¼���������ֶεļ�ֵ�����where�������
     * �Ǵ�LinkedHashMap�����л�ȡ��ֵ
     * @throws SQLException
     */
    private int _preparedStatementUpdate(String tableName, List<Map> listRecord, boolean isUpdateKey, String whereStr) throws SQLException {
        int num = 0;
        if (listRecord == null || listRecord.isEmpty()) {
            return num;
        }
        Map<String, Object> _m = new LinkedHashMap(listRecord.get(0));//��ȡһ����¼����Ϊ���ˡ���������
        String[] tableFields = db.getFields(tableName);//���е��ֶ�
        String[] tableKeys = db.getKeys(tableName);//���е�����
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
                if (tool.isInFields(tableKeys, k0[i].toString())) {//��¼���Ƿ�������
                    key_m.put(k0[i].toString(), _m.remove(k0[i].toString()));//����¼�е������Ƶ�key_m�У���֤������������
                }
            }
        }
        Object[] fields = (_m.keySet()).toArray(); //��¼�в�������������Ч�ֶΣ��ٴι��˵������ֶεĽ��
        Object[] keys = (key_m.keySet()).toArray(); //��¼�а���������

        if (isUpdateKey) {
            if (keys.length == 0 || keys.length != tableKeys.length) {
                return num;
            }
        }

        String[] kss = new String[fields.length]; //����"����=?"
        for (int i = 0; i < fields.length; i++) {
            kss[i] = fields[i].toString() + "=?";
        }

        String n_v = tool.arryToString(kss, ",");

        String preparedStatementUpdate = "update " + tableName + " set " + n_v + " ";
        //System.out.println(preparedStatementUpdate);

        PreparedStatement pstmt = null;

        try {

            if (whereStr != null) {
                pstmt = con.prepareStatement(preparedStatementUpdate + whereStr);//���whereStr!=null��������ִ��PrepareStatement����
            }

            for (Map record : listRecord) {

                if (whereStr == null) {
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
                    }

                    //System.out.println(preparedStatementUpdate + _w);//��������
                    pstmt = con.prepareStatement(preparedStatementUpdate + _w);//���whereStr==null��������ִ��PrepareStatement����//������ͬ��ֻ����������������
                }

                //Ϊ���������Ч�ʣ��涨�ж���������˳�򣺳��ñ�׼������ȷƥ�䡢��׼������ȷƥ�䡢�Ǳ�׼������ȷƥ�䡢�Ǳ�׼��������ƥ�䡢�Ǳ�׼��������Сдƥ��
                for (int i = 0; i < fields.length; i++) {
                    Field f = db.getField(tableName, fields[i].toString());
                    String className = f.getTypeClassName();
                    int index = f.getSqlType();
                    Object v = record.get(fields[i].toString());

                    if (v == null) {
                        pstmt.setNull(i + 1, index);//continue;
                    } else if (v != null) {
                        String _c = ((Class) v.getClass()).getName(); //���ӶԱ����ݵ�֧��,�ڱ��л�ȡ�����ݾ�ΪString��,��Ӧ�������ת��.
                        if ((_c.equals("java.lang.String")) && ("".equals(((String) v).trim()))) {
                            pstmt.setNull(i + 1, index);//continue;
                        } else {
                            if (className.equals("java.lang.String")) {
                                if ((_c.equals("java.lang.String")) && (!"".equals(((String) v).trim()))) {
                                    pstmt.setString(i + 1, (String) v);
                                }
                                continue;
                            }

                            if (className.equals("java.lang.Integer")) {
                                if ((_c.equals("java.lang.String")) && (!"".equals(((String) v).trim()))) {
                                    pstmt.setInt(i + 1, Integer.parseInt((String) v));
                                } else {
                                    if (_c.equals("java.lang.Integer")) {
                                        pstmt.setInt(i + 1, ((Integer) v).intValue());
                                    } else {
                                        Integer n = new Integer(v.toString());
                                        pstmt.setInt(i + 1, n);
                                    }
                                }
                                continue;
                            }

                            if (className.equals("java.lang.Long")) {
                                if ((_c.equals("java.lang.String")) && (!"".equals(((String) v).trim()))) {
                                    pstmt.setLong(i + 1, Long.parseLong((String) v));
                                } else {
                                    if (_c.equals("java.lang.Long")) {
                                        pstmt.setLong(i + 1, ((Long) v).longValue());
                                    } else {
                                        Long l = new Long(v.toString());
                                        pstmt.setLong(i + 1, l);
                                    }
                                }
                                continue;
                            }

                            if (className.equals("java.lang.Short")) {
                                if ((_c.equals("java.lang.String")) && (!"".equals(((String) v).trim()))) {
                                    pstmt.setShort(i + 1, Short.parseShort((String) v));
                                } else {
                                    pstmt.setShort(i + 1, ((Short) v).shortValue());
                                }
                                continue;
                            }

                            if (className.equals("java.lang.Float")) {
                                if ((_c.equals("java.lang.String")) && (!"".equals(((String) v).trim()))) {
                                    pstmt.setFloat(i + 1, Float.parseFloat((String) v));
                                } else {
                                    pstmt.setFloat(i + 1, ((Float) v).floatValue());
                                }
                                continue;
                            }

                            if (className.equals("java.lang.Double")) {
                                if ((_c.equals("java.lang.String")) && (!"".equals(((String) v).trim()))) {
                                    pstmt.setDouble(i + 1, Double.parseDouble((String) v));
                                } else {
                                    pstmt.setDouble(i + 1, ((Double) v).doubleValue());
                                }
                                continue;
                            }

                            if (className.equals("java.lang.Boolean")) {
                                if ((_c.equals("java.lang.String")) && (!"".equals(((String) v).trim()))) {
                                    pstmt.setBoolean(i + 1, (Boolean.valueOf((String) v)).booleanValue());
                                } else {
                                    pstmt.setBoolean(i + 1, ((Boolean) v).booleanValue());
                                }
                                continue;
                            }

                            if (className.equals("java.sql.Timestamp")) {
                                if ((_c.equals("java.lang.String")) && (!"".equals(((String) v).trim()))) {
                                    String _s = ((String) v).trim();
                                    if (tool.matches(RegexType.chinaDate, _s)) {//�磺2012-01-24
                                        Time t = new Time(0l);
                                        _s = _s + " " + t.toString();
                                        pstmt.setTimestamp(i + 1, java.sql.Timestamp.valueOf(_s));
                                    } else {
                                        pstmt.setTimestamp(i + 1, java.sql.Timestamp.valueOf((String) v));
                                    }
                                } else if (_c.equals("java.sql.Date")) {
                                    java.sql.Date _v = (java.sql.Date) v;
                                    pstmt.setTimestamp(i + 1, new Timestamp(_v.getTime()));
                                } else if (_c.equals("java.util.Date")) {
                                    java.util.Date _v = (java.util.Date) v;
                                    pstmt.setTimestamp(i + 1, new Timestamp(_v.getTime()));
                                } else if (_c.equals("java.sql.Time")) {
                                    java.sql.Time _v = (java.sql.Time) v;
                                    pstmt.setTimestamp(i + 1, new Timestamp(_v.getTime()));
                                } else {
                                    pstmt.setTimestamp(i + 1, new Timestamp(((java.util.Date) v).getTime()));//��֧�ָ����Ӧ��
                                    //pstmt.setTimestamp(i + 1,  (java.sql.Timestamp) v);//ʹ��jsf����ת�����õĽ�����ܲ���������ʱ�����ת���쳣
                                }
                                continue;
                            }

                            if (className.equals("java.sql.Date")) {
                                if ((_c.equals("java.lang.String")) && (!"".equals(((String) v).trim()))) {
                                    pstmt.setDate(i + 1, java.sql.Date.valueOf((String) v));
                                } else if (_c.equals("java.util.Date")) {
                                    java.util.Date _v = (java.util.Date) v;
                                    pstmt.setDate(i + 1, new java.sql.Date(_v.getTime()));
                                } else {
                                    pstmt.setDate(i + 1, (java.sql.Date) v);
                                }
                                continue;
                            }

                            if (className.equals("java.util.Date")) {
                                if ((_c.equals("java.lang.String")) && (!"".equals(((String) v).trim()))) {
                                    pstmt.setDate(i + 1, java.sql.Date.valueOf((String) v));
                                } else if (_c.equals("java.util.Date")) {
                                    java.util.Date _v = (java.util.Date) v;
                                    pstmt.setDate(i + 1, new java.sql.Date(_v.getTime()));
                                } else {
                                    pstmt.setDate(i + 1, (java.sql.Date) v);
                                }
                                continue;
                            }

                            if (className.equals("java.sql.Time")) {
                                if ((_c.equals("java.lang.String")) && (!"".equals(((String) v).trim()))) {
                                    pstmt.setTime(i + 1, java.sql.Time.valueOf((String) v));
                                } else if (_c.equals("java.util.Date")) {
                                    java.util.Date _v = (java.util.Date) v;
                                    DateFormat df = new SimpleDateFormat("HH:mm:ss");
                                    String _dt = df.format(_v);
                                    pstmt.setTime(i + 1, java.sql.Time.valueOf(_dt));
                                } else {
                                    pstmt.setTime(i + 1, (java.sql.Time) v);
                                }
                                continue;
                            }

                            if (className.equals("[B") || className.equals("byte[]")) {
                                //SQL Server ��image��timestamp��binary������byte[],MySQL ��blobϵ����java.lang.Object,Sybase��image��[B
                                if ((_c.equals("java.lang.String")) && (!"".equals(((String) v).trim()))) {
                                    pstmt.setBytes(i + 1, ((String) v).getBytes());
                                } else {
                                    pstmt.setBytes(i + 1, (byte[]) v);
                                }
                                continue;
                            }
                            if (className.equals("java.sql.Blob")) {
                                //SQL Server ��image��timestamp��binary������byte[],MySQL ��blobϵ����java.lang.Object,Sybase��image��[B
                                if ((_c.equals("java.lang.String")) && (!"".equals(((String) v).trim()))) {
                                    pstmt.setBytes(i + 1, ((String) v).getBytes());
                                } else {
                                    pstmt.setBlob(i + 1, (java.sql.Blob) v);
                                }
                                continue;
                            }
                            if (className.equals("java.lang.Object")) {
                                //SQL Server ��image��timestamp��binary������byte[],MySQL ��blobϵ����java.lang.Object,Sybase��image��[B
                                if ((_c.equals("java.lang.String")) && (!"".equals(((String) v).trim()))) {
                                    pstmt.setBytes(i + 1, ((String) v).getBytes());
                                } else {
                                    pstmt.setObject(i + 1, v);
                                }
                                continue;
                            }

                            if (className.equals("java.lang.Byte")) {
                                //MySQL��tinyint��java.lang.Byte
                                if ((_c.equals("java.lang.String")) && (!"".equals(((String) v).trim()))) {
                                    pstmt.setByte(i + 1, java.lang.Byte.parseByte((String) v));
                                } else {
                                    pstmt.setByte(i + 1, java.lang.Byte.parseByte(v.toString()));
                                }
                                continue;
                            }

                            if (className.equals("java.math.BigDecimal")) {
                                if ((_c.equals("java.lang.String")) && (!"".equals(((String) v).trim()))) {
                                    pstmt.setBigDecimal(i + 1, new BigDecimal((String) v));
                                } else if ((_c.equals("java.lang.Double"))) {
                                    pstmt.setBigDecimal(i + 1, new BigDecimal((Double) v));
                                } else if ((_c.equals("java.lang.Float"))) {
                                    double _v = (Double) v;
                                    pstmt.setBigDecimal(i + 1, new BigDecimal(_v));
                                } else if ((_c.equals("java.lang.Integer"))) {
                                    int _v = (Integer) v;
                                    pstmt.setBigDecimal(i + 1, new BigDecimal(_v));
                                } else if ((_c.equals("java.lang.Long"))) {
                                    long _v = (Long) v;
                                    pstmt.setBigDecimal(i + 1, new BigDecimal(_v));
                                } else if ((_c.equals("java.math.BigInteger"))) {
                                    java.math.BigInteger _v = (java.math.BigInteger) v;
                                    pstmt.setBigDecimal(i + 1, new BigDecimal(_v));
                                } else if ((_c.equals("[C"))) {
                                    char[] _v = (char[]) v;
                                    pstmt.setBigDecimal(i + 1, new BigDecimal(_v));
                                } else {
                                    pstmt.setBigDecimal(i + 1, (BigDecimal) v);
                                }
                                continue;
                            }

                            //���²��ֽ����ݾ�������ݿ���Ҫ����,�д���֤
                            if (className.equals("java.sql.Clob")) {
                                if ((_c.equals("java.lang.String")) && (!"".equals(((String) v).trim()))) {
                                    pstmt.setString(i + 1, (String) v);//��clob���͵��ֶθ����ַ�����
                                } else {
                                    pstmt.setClob(i + 1, (java.sql.Clob) v);
                                }
                                continue;
                            }

                            //���²��ֽ����ݾ�������ݿ���Ҫ����,�д���֤
                            if (className.equals("java.sql.Array")) {
                                if ((_c.equals("java.lang.String")) && (!"".equals(((String) v).trim()))) {
                                    //
                                } else {
                                    pstmt.setArray(i + 1, (java.sql.Array) v);
                                }
                                continue;
                            }

                            //�������ͣ��ǳ��ã��ú�
                            if (className.equals("com.sybase.jdbc2.tds.SybTimestamp") || className.toLowerCase().indexOf("timestamp") > 0) {
                                if ((_c.equals("java.lang.String")) && (!"".equals(((String) v).trim()))) {
                                    pstmt.setTimestamp(i + 1, java.sql.Timestamp.valueOf((String) v));
                                } else {
                                    pstmt.setTimestamp(i + 1, (java.sql.Timestamp) v);
                                }
                                continue;
                            }

                            //����ƥ��
                            if (className.toLowerCase().indexOf("date") > 0) {
                                if ((_c.equals("java.lang.String")) && (!"".equals(((String) v).trim()))) {
                                    pstmt.setDate(i + 1, java.sql.Date.valueOf((String) v));
                                } else {
                                    pstmt.setDate(i + 1, new java.sql.Date(((java.util.Date) v).getTime()));
                                }
                                continue;
                            }

                            if (className.toLowerCase().indexOf("time") > 0) {
                                if ((_c.equals("java.lang.String")) && (!"".equals(((String) v).trim()))) {
                                    pstmt.setTime(i + 1, java.sql.Time.valueOf((String) v));
                                } else {
                                    pstmt.setTime(i + 1, (java.sql.Time) v);
                                }
                                continue;
                            }

                            if (_c.equals("java.io.FileInputStream")) {
                                //�����磺FileInputStream in = new FileInputStream("D:\\test.jpg");�Ľ��
                                pstmt.setBinaryStream(i + 1, (FileInputStream) v, ((FileInputStream) v).available());
                                continue;
                            }//java.io.FileInputStream
                            //�����������ͣ��ǳ��ã��ú󣬸����setXXX��������������p700
                        }
                    }
                }
                num = num + pstmt.executeUpdate();
            }
        } catch (java.lang.ClassCastException ex) {
            throw new SQLException("java.lang.ClassCastException: " + ex.getMessage(), ex.getCause());
        } catch (NumberFormatException ex) {
            throw new SQLException("NumberFormatException: " + ex.getMessage(), ex.getCause());
        } catch (IOException ex) {
            throw new SQLException("IOException: " + ex.getMessage(), ex.getCause());
        } finally {
            pstmt.close();
        }

        return num;
    }

    /**
     * ��ѯ���м�¼
     *
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
     * ��ѯ���м�¼
     *
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
     * ��ѯ���м�¼
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
            stmt.cancel();
            throw ex;
        } finally {
            stmt.close();
        }
        return count;
    }

    /**
     * ��ѯ���ݿ�ṹ��Ϣ
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
                        + ";\n\t\t IsPrimarykey :\t" + f.isPrimarykey()
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
     * ��ѯ����
     *
     * @param sqlquery �Ǳ�׼��ѯ���,���������⸴�ӵĶ���ѯ���,����������JDBC API֧�ֵı�׼��ѯ���
     * @return ����ѯ���ResultSet����ת����List&lt;Map&lt;String,Object&gt;&gt;���͵Ľ��
     * @throws SQLException
     */
    @Override
    public List query(String sqlquery) throws SQLException {
        return this.query(sqlquery, 0);
    }

    /**
     * ��ѯ����
     *
     * @param sqlquery �Ǳ�׼��ѯ���,���������⸴�ӵĶ���ѯ���,����������JDBC API֧�ֵı�׼��ѯ���
     * @param timeout �趨��ѯʱ�ޣ���λ���룻timeout=0,���ѯ����ʱ������
     * @return ����ѯ���ResultSet����ת����List&lt;Map&lt;String,Object&gt;&gt;���͵Ľ��
     * @throws SQLException
     */
    public List query(String sqlquery, int timeout) throws SQLException {
        List records = new ArrayList();
        try {
            stmt = con.createStatement();
            if (timeout > 0) {
                stmt.setQueryTimeout(timeout);
            }
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
     * ͨ��һ���ɹ����������ȡָ����ʼλ�á�ָ�����ȵ��ӽ����,����isScrollSenstive��񣬽�������趨Ϊֻ��.
     *
     * @param sqlquery �Ǳ�׼��ѯ���,���������⸴�ӵĶ���ѯ���,����������JDBC API֧�ֵı�׼��ѯ���
     * @param position ��¼��ʼλ��,ע����м�¼�Ǵ�1��ʼ;Խ���򷵻�0����¼
     * @param length ��ָ����¼����,����������,��position���ȫ����¼
     * @param isScrollSenstive ָ��������Ƿ�����
     * @return
     * ��ȡ��ѯ�����ת����List����,ÿһ����¼ӳ���һ��HashMap����,���HashMap����ļ����Ǳ��е��ֶ����������ֶεı�������ֵΪ�ֶ�ֵ����ֵ���������ֶ�����Ӧ��JDBC
     * API��Java�ࡣ���޼�¼�򷵻��㳤��List����
     * @throws SQLException
     */
    public List query(String sqlquery, int position, int length, boolean isScrollSenstive) throws SQLException {
        List records = new ArrayList();
        try {
            java.sql.DatabaseMetaData dmd = con.getMetaData();
            if (dmd.supportsResultSetType(ResultSet.TYPE_SCROLL_INSENSITIVE)
                    || dmd.supportsResultSetType(ResultSet.TYPE_SCROLL_SENSITIVE)) {
                if (isScrollSenstive) {
                    stmt = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,
                            ResultSet.CONCUR_READ_ONLY);
                } else {
                    stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                            ResultSet.CONCUR_READ_ONLY);
                }
            }
            rs = stmt.executeQuery(sqlquery);
            rsmd = rs.getMetaData();
            int fieldCount = rsmd.getColumnCount();
            rs.last();
            int x = rs.getRow();
            if (position < 1 || position > x) {
                return records; //��ʼλ��Խ��,�򷵻�0����¼;
            }
            if (position + length > x) {
                length = x - (position - 1); //����ʼλ�ú�ļ�¼��С��length,��ȡ��ʼλ�ú��ȫ����¼;
            }
            Map valueMap = null;
            if (rs.absolute(position)) {
                for (int k = position; k < position + length; k++) {
                    valueMap = new HashMap();
                    for (int i = 1; i <= fieldCount; i++) {
                        String fieldClassName = rsmd.getColumnClassName(i);
                        String fieldName = rsmd.getColumnName(i);
                        this._recordMappingToMap(fieldClassName, fieldName, rs, valueMap);
                    }
                    records.add(valueMap);
                    if (!rs.next()) {
                        break;
                    }
                }
            }
        } finally {
            stmt.close();
        }
        return records;
    }

    /**
     * ��ѯ����
     *
     * @param sqlquery �Ǳ�׼��ѯ���,���������⸴�ӵĶ���ѯ���,����������JDBC API֧�ֵı�׼��ѯ���
     * @param fetchSize Ԥȡ��
     * @return ��ȡ��ѯ�����ת����List����
     * @throws SQLException
     */
    @Override
    public List queryFetchSize(String sqlquery, int fetchSize) throws SQLException {
        List records = new ArrayList();
        Map valueMap = null;
        int fieldCount = 0;
        stmt = con.createStatement();
        stmt.setFetchSize(fetchSize);//
        rs = stmt.executeQuery(sqlquery);
        rsmd = rs.getMetaData();
        fieldCount = rsmd.getColumnCount();
        int index = 0;
        while (rs.next()) {
            valueMap = new LinkedHashMap();
            for (int i = 1; i <= fieldCount; i++) {
                String fieldClassName = rsmd.getColumnClassName(i);
                String fieldName = rsmd.getColumnName(i);
                this._recordMappingToMap(fieldClassName, fieldName, rs, valueMap);
            }
            records.add(valueMap);
            index = index + 1;
        }
        rs.setFetchSize(fetchSize);
        rs.close();
        stmt.close();
        return records;
    }

    /**
     * ��ѯһ����¼
     *
     * @param sqlQuery ��ѯ���
     * @return ��ȡһ����¼��
     * @throws SQLException
     */
    @Override
    public Map queryOne(String sqlQuery) throws SQLException {
        Map m = new LinkedHashMap();
        try {
            stmt = con.createStatement();
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
        } finally {
            stmt.close();
        }
        db.setLastQuerySql(sqlQuery);
        return m;
    }

    /**
     * ��ѯһ����¼
     *
     * @param tableName �Ǳ���;
     * @param fieldName �ؼ��ֶ���;
     * @param fieldValue �ؼ��ֶ�ֵ.
     * @return ����ָ������,��ȡһ����¼��
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
     * ��ѯ���е�һ����¼
     *
     * @param tableName ����
     * @return ��ȡ��ĵ�һ����¼
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
     * ��ѯ�������һ����¼
     *
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
        Map m = this.queryOne(sql);
        return m;
    }

    /**
     * ������ѯһ����¼
     *
     * @param tableName ����
     * @param fields ����ѯ���ֶ�
     * @param row_in_table �Ӹ��п�ʼ��ȡֵ[1,��¼�ܳ���]������λ�÷���0����List&lt;Map&gt;���
     * @return ����List&lt;Map&gt;���͵Ľ��
     * @throws SQLException
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
     * @throws SQLException
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
     * @throws SQLException
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
     * @param row_in_table ������λ�ã�indexRow>=1,indexRow<=��ļ�¼�� @ return
     * ����IndexNode @throws SQLException
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
     * @param row_in_table ������λ�ã�indexRow>=1,indexRow<=��ļ�¼�� @ param length
     * ���ȣ��ڵ�ĳ��� @return ����IndexNode���� @ t hrows SQLException
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
     * @param rebuildIndexNodes �Ƿ��������ؽ�
     * @return ����IndexNode����
     * @throws SQLException
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
     * �����ڵ�����ĳ���
     *
     * @param tableName ����
     * @return ���������ڵ�����ĳ���
     */
    public int getIndexNodesLength(String tableName) {
        return db.getTable(tableName).getIndexNodes().length;
    }

    /**
     * @return ���������ڵ�Ĳ���
     */
    public int getIndexNodesStepLength() {
        return indexStepLength;
    }

    /**
     * ���������ڵ�Ĳ���
     *
     * @param indexStepLength �µ������ڵ�Ĳ���
     */
    public void setIndexNodesStepLength(int indexStepLength) {
        this.indexStepLength = indexStepLength;
    }

    /**
     * ��ѯ�����
     *
     * @param querySql �Ǳ�׼��ѯ���,���������⸴�ӵĶ���ѯ���,����������JDBC API֧�ֵı�׼��ѯ���
     * @return ��ȡ��ѯ���ResultSet����
     * @throws SQLException
     */
    @Override
    public ResultSet queryResultSet(String querySql) throws SQLException {
        stmt = con.createStatement();
        rs = stmt.executeQuery(querySql);
        return rs;
    }

    /**
     * ��ѯ�����
     *
     * @param querySql �Ǳ�׼��ѯ���,���������⸴�ӵĶ���ѯ���,����������JDBC API֧�ֵı�׼��ѯ���
     * @param statement
     * �û������Statement��������ʵ�ָ��߼��Ĳ�ѯ����ɸ��µġ��ɹ����ġ�ֻ���ġ����еġ������еģ�����ͨ��Statement
     * statement=getCon().createStatement(ResultSet.����0��ResultSet.����1);�ķ�ʽʵ��
     * @return ��ȡ��ѯ���ResultSet����
     * @throws SQLException
     */
    @Override
    public ResultSet queryResultSet(String querySql, Statement statement) throws SQLException {
        rs = statement.executeQuery(querySql);
        return rs;
    }

    /**
     * �����ϴβ�ѯ����ѯ
     *
     * @param last ��ѯ���ջ�еĵ�����
     * @throws SQLException
     */
    @Override
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
     * ��ResultSet������еļ�¼ӳ�䵽Map������.
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
     * ���볤��������ֵ��ֻ�Ե�һ���������һ���ֶ���Ч ʹ�õ���ģʽ��Ψһ����֤<br/>
     *
     * @param tableName �Ǳ���,
     * @return ���ؾ���Ψһ����ֵ֤��
     * ��ǰϵͳʱ��*10000+��λ��Ȼ������2012-11-01ĳʱĳ��ϵͳʱ�䣺1351749144390���޸ĺ�13517491443897256���������ϣ������������ȡʱ�䣬��������ֵ/10000���ɣ���1/100���롣
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
     * ����ָ����ֵ<br/> ���ַ�������ʽ�������ͺ��ַ��͵����������ֵ������ֵ�ĸ�ʽ�������ֶ��������<br/> ʹ�õ���ģʽ��Ψһ����֤<br/>
     *
     * @return ����ָ���ļ�ֵ������ɹ��򷵻ظ�ֵ��������ɹ����򷵻�null��
     * @param tableName �Ǳ���,
     * @param keyValue ��ֵ
     * @return ��keyValue��Ч���򷵻�keyValue�����򷵻�null��
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

    /**
     * ͨ�ü�ֵ��������ֻ�Ե�һ���������һ���ֶ���Ч<br/>
     * ֧�ֵ��ֶ������У�Long\Integer\Short\Float\Double\String(���ȴ��ڵ���13)<br/>
     * �������Զ���1���������Զ���1��ȡ�����ַ��Ͳ���Date��longֵ���磺1348315761671���ֶο��Ӧ�����ڵ���13<br/>
     * ʹ�õ���ģʽ��Ψһ����֤<br/>
     *
     * @param tableName �Ǳ���,
     * @return ���ؾ���Ψһ����֤���Զ�����ֵ
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
     * �ս�������,���һ����ȫ����,�ر����ӡ�������Ա��Ӧ�����˷������ر�����,��������������󣬿���������ݿ����Ӳ��ܹرգ�����б�Ҫ�����ս�������ģʽ�����ڵ�ʵ��֤�����ս��������������õ����á�<br/>
     * 1������������ֹ��<br/> 2��Ӧ�ó����з�����SQLException�쳣�󣬿��ܵ������ݿ�����û�йرա�<br/>
     */
    private final Object _finalizerGuardian = new Object() {
        @Override
        protected void finalize() throws SQLException, Throwable {
            if (con != null && !con.isClosed()) {
                try {
                    super.finalize();
                } finally {
                    if (!autoCommit) {
                        if (failCommit) {
                            try {
                                if (savepoint == null) {
                                    con.rollback();
                                } else {
                                    con.rollback(savepoint);
                                }
                                System.out.println("[" + dt.dateTime() + "] Connection rollback in finalize.");
                            } catch (SQLException ex) {
                                Logger.getLogger(ProcessVO.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                            } finally {
                                con.close();
                                autoCommit = true;
                                System.out.println("[" + dt.dateTime() + "] Connection is not autocommit, rollback and then closed in finalize.");
                            }
                        } else {
                            con.close();
                            System.out.println("[" + dt.dateTime() + "] Connection is autocommit, closed in finalize,You should close it.");
                        }
                    } else {
                        con.close();
                        System.out.println("[" + dt.dateTime() + "] Connection closed in finalize,You should close it.");
                    }
                }
            }
        }
    };
}

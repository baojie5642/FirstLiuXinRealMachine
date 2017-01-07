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
 * <code>ProcessVO</code>实现服务器型数据库DML(CRUD)操作<br/>
 *
 * 1、ProcessVO()使用db.xml配置的数据库连接；<br/> 2、ProcessVO(Connection
 * con)，使用指定的数据库连接；<br/> 3、必须调用closeCon()关闭连接；<br/>
 * 4、建议用户在try{}catch(SQLExceptionex){}finally{}结构中，打开事务，调用ProcessVO中的DML方法；<br/>
 * 5、拒绝插入、更新相同的记录；<br/> 6、支持多键更新操作；<br/>
 *
 *
 * 方法名约定：<br/>
 * 操作数据库，使用insert、insertXXX、update、delete、query、queryOne、save、index命名<br/>
 * 其中，index、indexXXX是一组索引相关的查询<br/> 尽可能保持与标准的sql命令一致。
 *
 *
 * @author 胡开明
 * @since jadepool 1.0
 */
public class ProcessVO implements Jdml {

    private java.sql.Connection con;
    private java.sql.Statement stmt;
    private java.sql.ResultSet rs;
    private java.sql.ResultSetMetaData rsmd;
    private Savepoint savepoint;//事务设置回滚点
    private JadeTool tool = new JadeTool();
    private DateTool dt = new DateTool();
    private DbCenter db = null;//实例化时，Db使用的数据库连接类型与ProcessVO一致
    private boolean autoCommit;
    private boolean failCommit;//
    private boolean isSupportJTA;//是否支持JTA事务
    private boolean showtip;//是否显示System.out.println输出的提示
    //con.isValid(10);//设定数据库连接需要完成工作的时间，时间是秒
    private WhereString where;
    private QueryString query;
    private int indexStepLength = 1000;

    /**
     * 默认构造函数.使用/META-INF/db.xml配置的数据库连接
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
     * 构造函数.
     *
     * @param con 使用指定的数据库连接
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
     * 构造函数.这个构造函数可以根据connectionType值创建不同的Db实例，可以用于不同数据库之间数据的交换
     *
     * @param con 使用指定的数据库连接
     * @param connectionType 连接的类型 连接的类型，目前以下四种类型<br/>
     * DbConnectionType.USING_CONFIG_OF_DEFAULT，实际的值1，需要db.xml配置文件<br/>
     * DbConnectionType.USING_CONFIG_OF_NONE，实际的值0，需要指定的con<br/>
     * DbConnectionType.USING_DB_01，实际的值101，需要指定的con<br/>
     * DbConnectionType.USING_DB_02，实际的值102，需要指定的con<br/>
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
     * 构造函数.
     *
     * @param connectionType 指定配置文件的构造函数 取值：<br/>
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
     * 设置事务隔离级别，默认的事务隔离级别是Connection.TRANSACTION_READ_COMMITTED，具体请参阅Connection的文档
     * <pre>
     *         Connection.TRANSACTION_NONE=              0;        不能使用
     *         Connection.TRANSACTION_READ_UNCOMMITTED = 1;        dirty reads, non-repeatable reads and phantom reads can occur
     *         Connection.TRANSACTION_READ_COMMITTED   = 2;        dirty reads are prevented; non-repeatable reads and phantom reads can occur
     *         Connection.TRANSACTION_REPEATABLE_READ  = 4;        dirty reads and non-repeatable reads are prevented; phantom reads can occur.
     *         Connection.TRANSACTION_SERIALIZABLE     = 8;        dirty reads, non-repeatable reads and phantom reads are prevented
     * </pre>
     *
     * @param transactionIsolation 的取值应当是Connection给定的常量之一 :
     */
    public void setTransactionIsolation(int transactionIsolation) throws SQLException {
        con.setTransactionIsolation(transactionIsolation);
    }

    /**
     * 设置事务保存点。
     *
     * @throws SQLException
     */
    public Savepoint setSavepoint() throws SQLException {
        savepoint = con.setSavepoint();
        return savepoint;
    }

    /**
     * 设置事务保存点。
     *
     * @param point 事务点名称
     * @throws SQLException
     */
    public Savepoint setSavepoint(String point) throws SQLException {
        savepoint = con.setSavepoint(point);
        return savepoint;
    }

    /**
     * 释放事务保存点。
     *
     * @throws SQLException
     */
    public void releaseSavepoint() throws SQLException {
        if (savepoint != null) {
            con.releaseSavepoint(savepoint);
        }
    }

    /**
     * 提交事务。
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
     * 事务回滚。
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
     * 用于判断数据库连接是否使用了事务
     *
     * @throws SQLException
     */
    public boolean isAutoCommit() throws SQLException {
        return con.getAutoCommit();
    }

    /**
     * 设定事务处理;
     *
     * @param autoCommit false开启事务，true关闭事务
     * @throws SQLException
     */
    public void setAutoCommit(boolean autoCommit) throws SQLException {
        con.setAutoCommit(autoCommit);
        this.autoCommit = autoCommit;
    }

    /**
     * 是否关闭了数据库连接
     *
     * @return 用于判断数据库连接是否关闭,返回true表示已关闭，返回false表示未关闭.
     * @throws SQLException
     *
     */
    @Override
    public boolean isClosed() throws SQLException {
        return con.isClosed();
    }

    /**
     * 关闭数据库联接
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
     * 返回数据库连接
     *
     * @return 返回Connection con对象
     */
    @Override
    public Connection getCon() {
        return this.con;
    }

    /**
     * 返回Db实例，以便于用户查询数据库及其表、字段的结构信息
     *
     * @return 当前使用的Db实例
     */
    @Override
    public DbCenter getDb() {
        return db;
    }

    //CRUD开始
    /**
     * 插入记录
     *
     * @param insertSql 插入语句
     * @return 返回JDBC标准插入语句的返回值
     * @throws SQLException
     */
    @Override
    public int insert(String insertSql) throws SQLException {
        int num = this.update(insertSql);
        return num;
    }

    /**
     * 插入一组记录
     *
     * @param insertSql 插入语句数组
     * @return 返回JDBC标准插入语句的返回值
     * @throws SQLException
     */
    @Override
    public int insert(String[] insertSql) throws SQLException {
        int num = this.update(insertSql);
        return num;
    }

    /**
     * 插入一条记录
     *
     * @param tableName 是表名
     * @param mapRecord
     * 是准备插入到表中的一条记录的数据,其键名与字段名相同,顺序无关;但键名不能是字段名以外的其它名称，自动过滤无效字段数据
     * @return 返回JDBC标准插入语句的返回值
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
     * 插入一条记录
     *
     * @param tableName 是表名
     * @param mapRecord 是准备插入到表中的一条记录的数据,其键名与字段名相同,顺序无关 ,自动过滤无效字段数据
     * @param autoInsertKey 值为true时，自动插入主键
     * @return 返回JDBC标准插入语句的返回值
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
     * 插入多条记录
     *
     * @param tableName 是表名
     * @param listRecord 是准备插入到表中的多条记录的数据,其键名与字段名相同,顺序无关 ,自动过滤无效字段数据
     * @return 返回JDBC标准插入语句的返回值
     * @throws SQLException
     */
    @Override
    public int insert(String tableName, List<Map> listRecord) throws SQLException {
        int num = this._preparedStatementInsert(tableName, listRecord, false);
        return num;
    }

    /**
     * 插入多条记录
     *
     * @param tableName 是表名
     * @param listRecord 是准备插入到表中的多条记录的数据,其键名与字段名相同,顺序无关 ,自动过滤无效字段数据
     * @param autoInsertKey 值为true时，自动插入主键
     * @return 返回JDBC标准插入语句的返回值
     * @throws SQLException
     */
    @Override
    public int insert(String tableName, List<Map> listRecord, boolean autoInsertKey) throws SQLException {
        int num = this._preparedStatementInsert(tableName, listRecord, true);
        return num;
    }

    /**
     * 执行Statement的execute方法 负责执行创建、更新表的结构、...
     *
     * @param sqlUpdate 是一条标准的SQL更新语句.
     * @return 返回标准更新语句的返回值
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
     * 执行Statement的executeUpdate方法，如：更新记录
     *
     * @param updateSql 是一组标准的SQL更新语句，可以是插入、删除、更新.
     * @return 返回标准更新语句的返回值
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
     * 调用、执行一个sql文件
     *
     * @param sqlFileName 一个sql文件.
     * @return 是否执行成功
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
     * 执行Statement的executeUpdate方法，如：插入、删除、更新记录
     *
     * @param updateSql 是一条标准的SQL更新语句，可以是插入、删除、更新.
     * @return 返回标准更新语句的返回值
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
     * 执行Statement的executeUpdate方法，如：更新记录
     *
     * @param updateSql 是一组标准的SQL更新语句，可以是插入、删除、更新.
     * @return 返回标准更新语句的返回值
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
     * 更新一条记录. 零长度字符串将保存为null。
     *
     * @param tableName 是表名
     * @param mapRecord 是准备插入到表中的一条记录的数据,其键名与字段名相同,顺序无关;但键名不能是字段名以外的其它名称;
     * @return 返回标准update方法的返回值
     * @throws SQLException
     */
    @Override
    public int update(String tableName, Map<String, Object> mapRecord) throws SQLException {
        List a = new ArrayList();
        a.add(mapRecord);
        return this._preparedStatementUpdate(tableName, a, false, null);
    }

    /**
     * 更新一条记录. 零长度字符串将保存为null。
     *
     * @param tableName 是表名
     * @param mapRecord 是准备插入到表中的一条记录的数据,其键名与字段名相同,顺序无关;但键名不能是字段名以外的其它名称;
     * @param where 是一个标准的where子句.
     * @return 返回标准update方法的返回值
     * @throws SQLException
     */
    @Override
    public int update(String tableName, Map<String, Object> mapRecord, String where) throws SQLException {
        List a = new ArrayList();
        a.add(mapRecord);
        return this._preparedStatementUpdate(tableName, a, false, where);
    }

    /**
     * 通过PreparedStatement自动更新多条记录
     *
     * @param tableName 表名
     * @param listRecord 包含完整主键字段的记录集
     * @return 返回标准update方法的返回值
     * @throws SQLException
     */
    @Override
    public int update(String tableName, List<Map> listRecord) throws SQLException {
        int num = 0;
        num = _preparedStatementUpdate(tableName, listRecord, false, null);
        return num;
    }

    /**
     * 保存一条记录.
     * 如果该记录存在，则更新之，如果该记录不存在，则插入。如果第一个主键的值为null||""，则自动插入新的主键值，这个方法不适合对含多主键的表进行插入操作，但不影响对多主键的表进行更新
     *
     * @param tableName 是表名
     * @param listRecord 是准备插入到表中的多条记录的数据,其键名与字段名相同,顺序无关;但键名不能是字段名以外的其它名称;
     * @param where 是一个标准的where子句.
     * @return 返回标准update方法返回的状态值
     * @throws SQLException
     */
    @Override
    public int update(String tableName, List<Map> listRecord, String where) throws SQLException {
        int num = 0;
        num = _preparedStatementUpdate(tableName, listRecord, false, where);
        return num;
    }

    /**
     * 保存一条记录.
     * 如果该记录存在，则更新之，如果该记录不存在，则插入。如果第一个主键的值为null||""，则自动插入新的主键值，这个方法不适合对含多主键的表进行插入操作，但不影响对多主键的表进行更新
     *
     * @param tableName 是表名
     * @param mapRecord 是准备插入到表中的一条记录的数据,其键名与字段名相同,顺序无关;但键名不能是字段名以外的其它名称;
     * @return 返回第一个主键值。【注：原返回int型update返回值，更改为第一个主键值，将更方便应用】
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
            n = this.insert(tableName, mapRecord);//无主键的表可以直接插入
            if (n > 0) {
                firstKeyValue = mapRecord.get(fields[0]);
            }//没有主键，则返回第一个字段值
        } else {
            firstKeyValue = mapRecord.get(keys[0].toString());
            if (firstKeyValue != null & !"".equals(firstKeyValue)) {
                Object[] recordFields = mapRecord.keySet().toArray();
                Map _key_m = new LinkedHashMap();
                for (int i = 0; i < recordFields.length; i++) {
                    if (tool.isInFields(keys, recordFields[i].toString())) {
                        _key_m.put(recordFields[i].toString(), mapRecord.get(recordFields[i].toString()));//提取记录中的主键字段
                    }
                }
                if (!_key_m.isEmpty()) {
                    Object[] _k = _key_m.keySet().toArray();
                    if (_k.length != keys.length) {
                        return null;//如果有多个主键，而记录中缺少多个主键，则不保存
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
                            n = this.insert(tableName, mapRecord);//原原本本地插入
                        } else {
                            n = this.update(tableName, mapRecord, _w);//原原本本地更新
                        }
                    }
                }
            } else {// if (firstKeyValue == null || "".equals(firstKeyValue))
                Field f = db.getField(tableName, keys[0]);
                String className = f.getTypeClassName();
                if (className.equals("java.lang.Long")) {
                    firstKeyValue = insertKey(tableName);
                    mapRecord.put(keys[0], firstKeyValue);
                    n = update(tableName, mapRecord);//表有主键，但记录中不含主键记录||主键记录值是null||主键值是""，则插入，并自动插入主键

                }
                if (className.equals("java.lang.Integer") || className.equals("java.lang.Short") || className.equals("java.lang.Float") || className.equals("java.lang.Double") || className.equals("java.lang.String")) {
                    firstKeyValue = insertAutoKey(tableName);
                    mapRecord.put(keys[0], firstKeyValue);
                    n = update(tableName, mapRecord);//表有主键，但记录中不含主键记录||主键记录值是null||主键值是""，则插入，并自动插入主键
                }
            }
        }
        return firstKeyValue;
    }

    /**
     * 保存一条记录.
     * 如果该记录存在，则更新之，如果该记录不存在，则插入。如果第一个主键的值为null||""，则自动插入新的主键值，这个方法不适合对含多主键的表进行插入操作，但不影响对多主键的表进行更新
     *
     * @param tableName 是表名
     * @param mapRecord 是准备插入到表中的一条记录的数据,其键名与字段名相同,顺序无关;但键名不能是字段名以外的其它名称;
     * @return 返回标准update方法返回的状态值
     * @throws SQLException
     */
    @Override
    public int save(String tableName, Map<String, Object> mapRecord) throws SQLException {
        int num = 0;
        String _w = "";
        String[] keys = db.getKeys(tableName);
        if (keys.length == 0) {
            return this.insert(tableName, mapRecord);//无主键的表可以直接插入
        } else {
            Object kv = mapRecord.get(keys[0].toString());
            if (kv == null || "".equals(kv)) {
                num = insert(tableName, mapRecord, true);//表有主键，但记录中不含主键记录||主键记录值是null||主键值是""，则插入，并自动插入主键
            } else {
                Object[] recordFields = mapRecord.keySet().toArray();
                Map _key_m = new LinkedHashMap();
                for (int i = 0; i < recordFields.length; i++) {
                    if (tool.isInFields(keys, recordFields[i].toString())) {
                        _key_m.put(recordFields[i].toString(), mapRecord.get(recordFields[i].toString()));//提取记录中的主键字段
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
                            num = this.insert(tableName, mapRecord);//原原本本地插入
                        } else {
                            num = this.update(tableName, mapRecord, _w);//原原本本地更新
                        }
                    }
                }
            }
        }
        return num;
    }

    /**
     * 删除记录， 执行Statement的executeUpdate方法
     *
     * @param deleteSql 是一条标准的SQL删除语句
     * @return 返回标准更新语句的返回值
     * @throws SQLException
     */
    @Override
    public int delete(String deleteSql) throws SQLException {
        return this.update(deleteSql);
    }

    /**
     * 删除记录， 执行Statement的executeUpdate方法
     *
     * @param deleteSql 是多条标准的SQL删除语句
     * @return 返回标准更新语句的返回值
     * @throws SQLException
     */
    @Override
    public int delete(String[] deleteSql) throws SQLException {
        return this.update(deleteSql);
    }

    /**
     * 使用PreparedStatement插入多条记录 本方法是根据JDBC API类名的判断来存贮数据<br/>
     *
     * 为了方便表单数据的录入,本方法提供了用字符串来表达Integer|Long|Short|Float|Double|Bigdecimal|java.sql.Date等常规类型的支持,<br/>
     * 但请注意:用字符串表达Date时,目前java.sql.Date仅支持将"yyyy-mm-dd"格式转换成Date对象;<br/>
     * 对于其它类型,用户必须建立相应类型的对象; 本方法提供了对上传文件的支持; 零长度字符串将保存为null。<br/>
     *
     *
     *
     * 完成插入的步骤： 1、过滤记录中无效字段，得有效字段Object[] fields<br/>
     * 2、根据isUpdateKey对有效字段继续分组：true时，再得主键字段组Object[]
     * keys和非主键字段组Object[]fields<br/> 3、自动组合成PreparedStatement所需要的插入语句<br/>
     * 4、迭代记录for (Map record : listRecord) {}<br/>
     * 5、执行PreparedStatement的executeUpdate(updateSQL);<br/>
     *
     *
     * @param tableName 是一个表名
     * @param listRecord 是具有相同结构的一组记录
     * @param autoInsertKey 值为true时，自动插入主键
     * 是操作条件，插入时不起作用，更新时，若where==null||"".equals(where)，则自动根据记录自身主键字段的键值对组合where条件语句
     * 是从LinkedHashMap参数中获取的值
     * @throws SQLException
     */
    private int _preparedStatementInsert(String tableName, List<Map> listRecord, boolean autoInsertKey) throws SQLException {
        int num = 0;
        if (listRecord == null || listRecord.isEmpty()) {
            return num;
        }
        String preparedStatementInsert = "";

        Map<String, Object> _m = new LinkedHashMap(listRecord.get(0));//获取一条记录，作为创建preparedStatementSQL语句的依据
        Object maxFieldValue = null;
        String[] tableFields = db.getFields(tableName);//表中的字段名的集合
        String[] tableKeys = db.getKeys(tableName);//表中的主键字段集
        if (autoInsertKey) {
            Map lastRecord = this.queryLastRecord(tableName);//准备自动插入的主键
            if (tableKeys.length > 0) {
                maxFieldValue = lastRecord.get(tableKeys[0]);
                _m.put(tableKeys[0], "");//防止记录中不含tableKeys[0]主键字段
            } else {
                maxFieldValue = lastRecord.get(tableFields[0]);
                _m.put(tableFields[0], "");//防止记录中不含tableKeys[0]主键字段
            }
        }
        Object[] recordFields = (_m.keySet()).toArray(); //获取记录里的字段名的集合
        for (int i = 0; i < recordFields.length; i++) {
            if (!tool.isInFields(tableFields, recordFields[i].toString())) {
                _m.remove(recordFields[i].toString());//移除无效字段， 查看记录中的字段在表中是否存在，如果不存在，则移除到
            }
        }

        Object[] fields = (_m.keySet()).toArray(); //过滤后的有效字段
        String[] values = new String[fields.length]; //保存通配符'?'
        for (int i = 0; i < fields.length; i++) {
            values[i] = "?";
        }

        String sql_field = tool.arryToString(fields, ",");
        String sql_values = tool.arryToString(values, ",");
        preparedStatementInsert = "insert into " + tableName + " (" + sql_field + " ) values (" + sql_values + ")";//合成preparedStatement语句
        PreparedStatement pstmt = null;

        try {
            pstmt = con.prepareStatement(preparedStatementInsert);//多记录共享prepareStatement插入语句
            long firstStringKey = (new java.util.Date()).getTime();

            //为了提高运算效率，规定判断条件优先顺序：常用标准条件精确匹配、标准条件精确匹配、非标准条件精确匹配、非标准条件概略匹配、非标准条件概略小写匹配
            for (Map record : listRecord) {

                if (autoInsertKey) {
                    if (tableKeys.length > 0) {
                        Field keyF = db.getField(tableName, tableKeys[0]);
                        if ("java.lang.Long".equals(keyF.getTypeClassName())) {//时间+4位整数
                            record.put(tableKeys[0], db.getTable(tableName).makeLongKeyValue());
                        } else if ("java.lang.String".equals(keyF.getTypeClassName())) {
                            if (Integer.parseInt(keyF.getSize()) > 13) {
                                record.put(tableKeys[0], "" + (firstStringKey + 1));//自动插入字符型主键值//if (className.equals("java.lang.String") && Integer.parseInt(f.getSize()) > 13) 
                            } else {
                                return num;
                            }
                        } else {
                            maxFieldValue = db.getTable(tableName).makeObjectKeyValue(keyF, maxFieldValue);
                            record.put(tableKeys[0], maxFieldValue);//自动插入数值型主键值
                        }
                    } else {
                        Field keyF = db.getField(tableName, tableFields[0]);
                        if ("java.lang.Long".equals(keyF.getTypeClassName())) {//时间+4位整数
                            record.put(tableFields[0], db.getTable(tableName).makeLongKeyValue());
                        } else if ("java.lang.String".equals(keyF.getTypeClassName())) {
                            if (Integer.parseInt(keyF.getSize()) > 13) {
                                record.put(tableFields[0], "" + (firstStringKey + 1));//自动插入字符型主键值//if (className.equals("java.lang.String") && Integer.parseInt(f.getSize()) > 13) 
                            } else {
                                return num;
                            }
                        } else {
                            maxFieldValue = db.getTable(tableName).makeObjectKeyValue(keyF, maxFieldValue);
                            record.put(tableFields[0], maxFieldValue);//自动插入数值型主键值
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
                        String _c = ((Class) v.getClass()).getName(); //增加对表单数据的支持,在表单中获取的数据均为String型,固应对其进行转换.
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
                                    if (tool.matches(RegexType.chinaDate, _s)) {//如：2012-01-24
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
                                    pstmt.setTimestamp(i + 1, new Timestamp(((java.util.Date) v).getTime()));//能支持更多的应用
                                    //pstmt.setTimestamp(i + 1,  (java.sql.Timestamp) v);//使用jsf日期转换后获得的结果可能不完整，这时会出现转换异常
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
                                //SQL Server 的image\timestamp\binary类型是byte[],MySQL 的blob系列是java.lang.Object,Sybase的image是[B
                                if ((_c.equals("java.lang.String")) && (!"".equals(((String) v).trim()))) {
                                    pstmt.setBytes(i + 1, ((String) v).getBytes());
                                } else {
                                    pstmt.setBytes(i + 1, (byte[]) v);
                                }
                                continue;
                            }
                            if (className.equals("java.sql.Blob")) {
                                //SQL Server 的image\timestamp\binary类型是byte[],MySQL 的blob系列是java.lang.Object,Sybase的image是[B
                                if ((_c.equals("java.lang.String")) && (!"".equals(((String) v).trim()))) {
                                    pstmt.setBytes(i + 1, ((String) v).getBytes());
                                } else {
                                    pstmt.setBlob(i + 1, (java.sql.Blob) v);
                                }
                                continue;
                            }

                            if (className.equals("java.lang.Object")) {
                                //SQL Server 的image\timestamp\binary类型是byte[],MySQL 的blob系列是java.lang.Object,Sybase的image是[B
                                if ((_c.equals("java.lang.String")) && (!"".equals(((String) v).trim()))) {
                                    pstmt.setBytes(i + 1, ((String) v).getBytes());
                                } else {
                                    pstmt.setObject(i + 1, v);
                                }
                                continue;
                            }

                            if (className.equals("java.lang.Byte")) {
                                //MySQL的tinyint是java.lang.Byte
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

                            //以下部分将根据具体的数据库需要而定,有待验证
                            if (className.equals("java.sql.Clob")) {
                                if ((_c.equals("java.lang.String")) && (!"".equals(((String) v).trim()))) {
                                    pstmt.setString(i + 1, (String) v);//给clob类型的字段赋予字符串型
                                } else {
                                    pstmt.setClob(i + 1, (java.sql.Clob) v);
                                }
                                continue;
                            }

                            //以下部分将根据具体的数据库需要而定,有待验证
                            if (className.equals("java.sql.Array")) {
                                if ((_c.equals("java.lang.String")) && (!"".equals(((String) v).trim()))) {
                                    //
                                } else {
                                    pstmt.setArray(i + 1, (java.sql.Array) v);
                                }
                                continue;
                            }

                            //特殊类型，非常用，置后
                            if (className.equals("com.sybase.jdbc2.tds.SybTimestamp") || className.toLowerCase().indexOf("timestamp") > 0) {
                                if ((_c.equals("java.lang.String")) && (!"".equals(((String) v).trim()))) {
                                    pstmt.setTimestamp(i + 1, java.sql.Timestamp.valueOf((String) v));
                                } else {
                                    pstmt.setTimestamp(i + 1, (java.sql.Timestamp) v);
                                }
                                continue;
                            }

                            //概略匹配
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
                                //调用如：FileInputStream in = new FileInputStream("D:\\test.jpg");的结果
                                pstmt.setBinaryStream(i + 1, (FileInputStream) v, ((FileInputStream) v).available());
                                continue;
                            }//java.io.FileInputStream

                            //其它特殊类型，非常用，置后
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
     * 使用PreparedStatement更新多条记录 本方法是根据JDBC API类名的判断来存贮数据<br/>
     * 为了方便表单数据的录入,本方法提供了用字符串来表达Integer|Long|Short|Float|Double|Bigdecimal|java.sql.Date等常规类型的支持,<br/>
     * 但请注意:用字符串表达Date时,目前java.sql.Date仅支持将"yyyy-mm-dd"格式转换成Date对象;<br/>
     * 对于其它类型,用户必须建立相应类型的对象; 本方法提供了对上传文件的支持; 零长度字符串将保存为null。<br/>
     * 详细的数据类型，可参阅《Java开发者年鉴》p700 PreparedStatement的setXXX方法<br/>
     *
     *
     * 完成更新的步骤： 1、过滤记录中无效字段，得有效字段Object[] fields<br/>
     * 2、根据isUpdateKey对有效字段继续分组：!true时，再得主键字段组Object[] keys和非主键字段组Object[]
     * fields<br/> 3、自动生成PreparedStatement所需的更新操作<br/> 4、迭代记录for (Map record :
     * listRecord) {}<br/> 5、执行PreparedStatement的executeUpdate(updateSQL);<br/>
     *
     *
     * @param tableName 是一个表名
     * @param listRecord 是具有相同结构的一组记录
     * @param isUpdateKey 是否更新主键
     * @param whereStr
     * 是操作条件，插入时不起作用，更新时，若where==null||"".equals(where)，则自动根据记录自身主键字段的键值对组合where条件语句
     * 是从LinkedHashMap参数中获取的值
     * @throws SQLException
     */
    private int _preparedStatementUpdate(String tableName, List<Map> listRecord, boolean isUpdateKey, String whereStr) throws SQLException {
        int num = 0;
        if (listRecord == null || listRecord.isEmpty()) {
            return num;
        }
        Map<String, Object> _m = new LinkedHashMap(listRecord.get(0));//获取一条记录，作为过滤、分组依据
        String[] tableFields = db.getFields(tableName);//表中的字段
        String[] tableKeys = db.getKeys(tableName);//表中的主键
        Object[] recordFields = (_m.keySet()).toArray(); //获取记录里的字段名的集合
        for (int i = 0; i < recordFields.length; i++) {
            if (!tool.isInFields(tableFields, recordFields[i].toString())) {
                _m.remove(recordFields[i].toString());//移除无效字段， 查看记录中的字段在表中是否存在，如果不存在，则移除到
            }
        }
        Object[] k0 = (_m.keySet()).toArray(); //过滤后的有效字段
        Map<String, Object> key_m = new LinkedHashMap();//记录里的主键
        if (!isUpdateKey) {
            for (int i = 0; i < k0.length; i++) {
                if (tool.isInFields(tableKeys, k0[i].toString())) {//记录中是否有主键
                    key_m.put(k0[i].toString(), _m.remove(k0[i].toString()));//将记录中的主键移到key_m中；保证不对主键更新
                }
            }
        }
        Object[] fields = (_m.keySet()).toArray(); //记录中不包含主键的有效字段；再次过滤掉主键字段的结果
        Object[] keys = (key_m.keySet()).toArray(); //记录中包含的主键

        if (isUpdateKey) {
            if (keys.length == 0 || keys.length != tableKeys.length) {
                return num;
            }
        }

        String[] kss = new String[fields.length]; //保存"键名=?"
        for (int i = 0; i < fields.length; i++) {
            kss[i] = fields[i].toString() + "=?";
        }

        String n_v = tool.arryToString(kss, ",");

        String preparedStatementUpdate = "update " + tableName + " set " + n_v + " ";
        //System.out.println(preparedStatementUpdate);

        PreparedStatement pstmt = null;

        try {

            if (whereStr != null) {
                pstmt = con.prepareStatement(preparedStatementUpdate + whereStr);//如果whereStr!=null，从这里执行PrepareStatement更新
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

                    //System.out.println(preparedStatementUpdate + _w);//批量更新
                    pstmt = con.prepareStatement(preparedStatementUpdate + _w);//如果whereStr==null，从这里执行PrepareStatement更新//条件不同，只能逐条变更插入语句
                }

                //为了提高运算效率，规定判断条件优先顺序：常用标准条件精确匹配、标准条件精确匹配、非标准条件精确匹配、非标准条件概略匹配、非标准条件概略小写匹配
                for (int i = 0; i < fields.length; i++) {
                    Field f = db.getField(tableName, fields[i].toString());
                    String className = f.getTypeClassName();
                    int index = f.getSqlType();
                    Object v = record.get(fields[i].toString());

                    if (v == null) {
                        pstmt.setNull(i + 1, index);//continue;
                    } else if (v != null) {
                        String _c = ((Class) v.getClass()).getName(); //增加对表单数据的支持,在表单中获取的数据均为String型,固应对其进行转换.
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
                                    if (tool.matches(RegexType.chinaDate, _s)) {//如：2012-01-24
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
                                    pstmt.setTimestamp(i + 1, new Timestamp(((java.util.Date) v).getTime()));//能支持更多的应用
                                    //pstmt.setTimestamp(i + 1,  (java.sql.Timestamp) v);//使用jsf日期转换后获得的结果可能不完整，这时会出现转换异常
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
                                //SQL Server 的image、timestamp、binary类型是byte[],MySQL 的blob系列是java.lang.Object,Sybase的image是[B
                                if ((_c.equals("java.lang.String")) && (!"".equals(((String) v).trim()))) {
                                    pstmt.setBytes(i + 1, ((String) v).getBytes());
                                } else {
                                    pstmt.setBytes(i + 1, (byte[]) v);
                                }
                                continue;
                            }
                            if (className.equals("java.sql.Blob")) {
                                //SQL Server 的image、timestamp、binary类型是byte[],MySQL 的blob系列是java.lang.Object,Sybase的image是[B
                                if ((_c.equals("java.lang.String")) && (!"".equals(((String) v).trim()))) {
                                    pstmt.setBytes(i + 1, ((String) v).getBytes());
                                } else {
                                    pstmt.setBlob(i + 1, (java.sql.Blob) v);
                                }
                                continue;
                            }
                            if (className.equals("java.lang.Object")) {
                                //SQL Server 的image、timestamp、binary类型是byte[],MySQL 的blob系列是java.lang.Object,Sybase的image是[B
                                if ((_c.equals("java.lang.String")) && (!"".equals(((String) v).trim()))) {
                                    pstmt.setBytes(i + 1, ((String) v).getBytes());
                                } else {
                                    pstmt.setObject(i + 1, v);
                                }
                                continue;
                            }

                            if (className.equals("java.lang.Byte")) {
                                //MySQL的tinyint是java.lang.Byte
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

                            //以下部分将根据具体的数据库需要而定,有待验证
                            if (className.equals("java.sql.Clob")) {
                                if ((_c.equals("java.lang.String")) && (!"".equals(((String) v).trim()))) {
                                    pstmt.setString(i + 1, (String) v);//给clob类型的字段赋予字符串型
                                } else {
                                    pstmt.setClob(i + 1, (java.sql.Clob) v);
                                }
                                continue;
                            }

                            //以下部分将根据具体的数据库需要而定,有待验证
                            if (className.equals("java.sql.Array")) {
                                if ((_c.equals("java.lang.String")) && (!"".equals(((String) v).trim()))) {
                                    //
                                } else {
                                    pstmt.setArray(i + 1, (java.sql.Array) v);
                                }
                                continue;
                            }

                            //特殊类型，非常用，置后
                            if (className.equals("com.sybase.jdbc2.tds.SybTimestamp") || className.toLowerCase().indexOf("timestamp") > 0) {
                                if ((_c.equals("java.lang.String")) && (!"".equals(((String) v).trim()))) {
                                    pstmt.setTimestamp(i + 1, java.sql.Timestamp.valueOf((String) v));
                                } else {
                                    pstmt.setTimestamp(i + 1, (java.sql.Timestamp) v);
                                }
                                continue;
                            }

                            //概略匹配
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
                                //调用如：FileInputStream in = new FileInputStream("D:\\test.jpg");的结果
                                pstmt.setBinaryStream(i + 1, (FileInputStream) v, ((FileInputStream) v).available());
                                continue;
                            }//java.io.FileInputStream
                            //其它特殊类型，非常用，置后，更多的setXXX方法详见《年鉴》p700
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
     * 查询表中记录
     *
     * @param tableName 是表名.
     * @return 获取记录数.
     * @throws SQLException
     */
    @Override
    public long queryCount(String tableName) throws SQLException {
        String sql = "select count(*) as express from " + tableName;
        return _queryCountOfTable(sql);
    }

    /**
     * 查询表中记录
     *
     * @param tableName 是表名
     * @param where String对象where是where子句部分
     * @return 获取记录数.
     * @throws SQLException
     */
    @Override
    public long queryCount(String tableName, String where) throws SQLException {
        String sql = "select count(*) as express from " + tableName + " " + where;
        return _queryCountOfTable(sql);
    }

    /**
     * 查询表中记录
     *
     * @param sql 是查询语句
     * @return 获取记录数.
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
     * 查询数据库结构信息
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
                        //+ ";\n\t\t Regex :\t" + f.getRegex()//暂未实现
                        + ";\n\t\t className :\t" + f.getTypeClassName();
            }
        }
        return s;
    }

    /**
     * 查询指定表的结构信息
     *
     * @param tableName
     * @return 返回表及其所有字段信息
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
                    //+ ";\n\t\t Regex :\t" + f.getRegex()//暂未实现
                    + ";\n\t\t className :\t" + f.getTypeClassName();
        }

        return s;
    }

    /**
     * 查询方法
     *
     * @param sqlquery 是标准查询语句,可以是任意复杂的多表查询语句,但必须是受JDBC API支持的标准查询语句
     * @return 将查询结果ResultSet对象转换成List&lt;Map&lt;String,Object&gt;&gt;类型的结果
     * @throws SQLException
     */
    @Override
    public List query(String sqlquery) throws SQLException {
        return this.query(sqlquery, 0);
    }

    /**
     * 查询方法
     *
     * @param sqlquery 是标准查询语句,可以是任意复杂的多表查询语句,但必须是受JDBC API支持的标准查询语句
     * @param timeout 设定查询时限，单位：秒；timeout=0,则查询不受时间限制
     * @return 将查询结果ResultSet对象转换成List&lt;Map&lt;String,Object&gt;&gt;类型的结果
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
     * 通过一个可滚动结果集获取指定起始位置、指定长度的子结果集,不论isScrollSenstive真否，结果集均设定为只读.
     *
     * @param sqlquery 是标准查询语句,可以是任意复杂的多表查询语句,但必须是受JDBC API支持的标准查询语句
     * @param position 记录起始位置,注意表中记录是从1开始;越界则返回0条记录
     * @param length 是指定记录长度,若不够长度,则含position后的全部记录
     * @param isScrollSenstive 指定结果集是否敏感
     * @return
     * 获取查询结果集转化成List对象,每一条记录映射成一个HashMap对象,这个HashMap对象的键名是表中的字段名，或是字段的别名，键值为字段值，键值的类型是字段所对应的JDBC
     * API的Java类。若无记录则返回零长度List对象。
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
                return records; //起始位置越界,则返回0条记录;
            }
            if (position + length > x) {
                length = x - (position - 1); //若起始位置后的记录数小于length,则取起始位置后的全部记录;
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
     * 查询方法
     *
     * @param sqlquery 是标准查询语句,可以是任意复杂的多表查询语句,但必须是受JDBC API支持的标准查询语句
     * @param fetchSize 预取数
     * @return 获取查询结果集转化成List对象
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
     * 查询一条记录
     *
     * @param sqlQuery 查询语句
     * @return 获取一条记录。
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
     * 查询一条记录
     *
     * @param tableName 是表名;
     * @param fieldName 关键字段名;
     * @param fieldValue 关键字段值.
     * @return 根据指定条件,获取一条记录。
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
     * 查询表中第一条记录
     *
     * @param tableName 表名
     * @return 获取表的第一条记录
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
     * 查询表中最后一条记录
     *
     * @param tableName 表名
     * @return 获取表的最后一条记录
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
     * 索引查询一条记录
     *
     * @param tableName 表名
     * @param fields 待查询的字段
     * @param row_in_table 从该行开始，取值[1,记录总长度]，出界位置返回0长度List&lt;Map&gt;结果
     * @return 返回List&lt;Map&gt;类型的结果
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
     * 索引查询多条记录
     *
     * @param tableName 表名
     * @param fields 待查询的字段
     * @param row_in_table 从该行开始，取值[1,记录总长度]，出界位置返回0长度List&lt;Map&gt;结果
     * @param length 查询长度，row_in_table后记录数少于length，则返回其后的全部记录
     * @param asc 排列顺序，true顺序，false逆序
     * @return 返回List&lt;Map&gt;类型的结果
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
     * 索引查询多条记录<br/> 根据已经建立的索引节点数组查询
     * 返回的记录是当前节点的行数减前一节点的行数，即：默认的步长内的记录数，通常是1000。<br/>
     * 默认步长可以调用pvo.setIndexNodesStepLength(indexStepLength)方法重新设置<br/>
     * 可用于大数据分组分页查询
     *
     * @param tableName 表名
     * @param fields 待查询的字段
     * @param position_of_indexNodes
     * 取第position_of_indexNodes位已建立的节点，取值[0,nodes.length - 1]
     * @param asc 排列顺序，true顺序，false逆序
     * @param rebuildIndexNodes 在查询前是否重新创建索引数组
     * @return 返回List&lt;Map&gt;类型的结果
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
        }//步长发生变化时，必须重建
        IndexNode[] nodes = _indexNodes(tableName, rebuildIndexNodes);
        List<Map> list = new ArrayList();
        if (position_of_indexNodes < 0 || position_of_indexNodes >= (nodes.length - 1)) {
            return list;//超出范围
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
        db.getTable(tableName).setPosition_of_indexNodes(position_of_indexNodes);//保存本次查询的位置信息
        list = this.query(sql);
        return list;
    }

    /**
     * 查询一个索引节点
     *
     * @param tableName 表名
     * @param row_in_table 行索引位置，indexRow>=1,indexRow<=表的记录数 @ return
     * 返回IndexNode @throws SQLException
     */
    private IndexNode _indexNodeOne(String tableName, int row_in_table) throws SQLException {
        String key = db.getFields(tableName)[0];
        if (db.getKeys(tableName).length > 0) {
            key = db.getKeys(tableName)[0];
        }
        String querySql = "select " + key + " from " + tableName + " order by " + key;
        stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);//创建不敏感的可滚动结果集
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
     * 查询两个索引节点
     *
     * @param tableName 表名
     * @param row_in_table 行索引位置，indexRow>=1,indexRow<=表的记录数 @ param length
     * 长度，节点的长度 @return 返回IndexNode数组 @ t hrows SQLException
     */
    private IndexNode[] _indexNodeTwo(String tableName, int row_in_table, int length) throws SQLException {
        String key = db.getFields(tableName)[0];
        if (db.getKeys(tableName).length > 0) {
            key = db.getKeys(tableName)[0];
        }
        String querySql = "select " + key + " from " + tableName + " order by " + key;
        stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);//创建不敏感的可滚动结果集
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
     * 创建表的索引节点数组<br/>
     * 使用默认步长1000，每增加1000条记录创建一个节点，最后一个节点保存最后一条记录信息，每个节点记录表的第一个主键值以及按该键顺序查询结果的行数<br/>
     *
     * @param tableName 表名
     * @param rebuildIndexNodes 是否无条件重建
     * @return 返回IndexNode数组
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
            countInNodes = nodes[nodes.length - 1].getRow();//最后一个节点保存了记录总长度(总行数)信息
        }
        boolean b = false;
        if (table.getIndexStepLength() != indexStepLength) {
            b = true;
            table.setIndexStepLength(indexStepLength);
        } else if (rebuildIndexNodes) {
            b = true;
        } else if (countInNodes == 0) {//无条件重新建立索引
            b = true;
        } else if (length > countInNodes) {
            b = true;
        }
        if (b) {
            String querySql = "select " + key + " from " + tableName + " order by " + key;
            stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);//创建不敏感的可滚动结果集
            rs = stmt.executeQuery(querySql);
            nodes = new IndexNode[nodeNums];

            int row = indexStepLength;
            for (int i = 0; i < nodes.length - 1; i++) {
                rs.absolute(row);
                nodes[i] = new IndexNode();
                nodes[i].setFirstKeyValue(rs.getObject(key));
                nodes[i].setRow(rs.getRow());//"第一个关键字段的值对应的行号"
                row += indexStepLength;
            }
            rs.last();
            nodes[nodes.length - 1] = new IndexNode();
            nodes[nodes.length - 1].setFirstKeyValue(rs.getObject(key));
            nodes[nodes.length - 1].setRow(rs.getRow());//"第一个关键字段的值对应的行号"
            db.getTable(tableName).setIndexNodes(nodes);
        }
        rs.close();
        stmt.close();
        return nodes;
    }

    /**
     * 索引节点数组的长度
     *
     * @param tableName 表名
     * @return 返回索引节点数组的长度
     */
    public int getIndexNodesLength(String tableName) {
        return db.getTable(tableName).getIndexNodes().length;
    }

    /**
     * @return 返回索引节点的步长
     */
    public int getIndexNodesStepLength() {
        return indexStepLength;
    }

    /**
     * 设置索引节点的步长
     *
     * @param indexStepLength 新的索引节点的步长
     */
    public void setIndexNodesStepLength(int indexStepLength) {
        this.indexStepLength = indexStepLength;
    }

    /**
     * 查询结果集
     *
     * @param querySql 是标准查询语句,可以是任意复杂的多表查询语句,但必须是受JDBC API支持的标准查询语句
     * @return 获取查询结果ResultSet对象
     * @throws SQLException
     */
    @Override
    public ResultSet queryResultSet(String querySql) throws SQLException {
        stmt = con.createStatement();
        rs = stmt.executeQuery(querySql);
        return rs;
    }

    /**
     * 查询结果集
     *
     * @param querySql 是标准查询语句,可以是任意复杂的多表查询语句,但必须是受JDBC API支持的标准查询语句
     * @param statement
     * 用户定义的Statement对象，用来实现更高级的查询，如可更新的、可滚动的、只读的、敏感的、非敏感的，可以通过Statement
     * statement=getCon().createStatement(ResultSet.参数0，ResultSet.参数1);的方式实现
     * @return 获取查询结果ResultSet对象
     * @throws SQLException
     */
    @Override
    public ResultSet queryResultSet(String querySql, Statement statement) throws SQLException {
        rs = statement.executeQuery(querySql);
        return rs;
    }

    /**
     * 根据上次查询语句查询
     *
     * @param last 查询语句栈中的倒序数
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
     * 将ResultSet结果集中的记录映射到Map对象中.
     *
     * @param fieldClassName 是JDBC API中的类型名称,
     * @param fieldName 是字段名，
     * @param rs 是一个ResultSet查询结果集,
     * @param fieldValue Map对象,用于存贮一条记录.
     * @throws SQLException
     */
    private void _recordMappingToMap(String fieldClassName, String fieldName, ResultSet rs, Map fieldValue) throws SQLException {
        fieldName = fieldName.toLowerCase();

        //优先规则：常用类型靠前
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
                fieldValue.put(fieldName, s);//早期jdk需要包装，jdk1.5后不需要包装
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
            //byte[]出现在SQL Server中
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
        } else {//对于其它任何未知类型的处理
            Object s = rs.getObject(fieldName);
            if (rs.wasNull()) {
                fieldValue.put(fieldName, null);
            } else {
                fieldValue.put(fieldName, s);
            }
        }

    }

    /**
     * 插入长整形主键值，只对第一个主键或第一个字段有效 使用单例模式和唯一性验证<br/>
     *
     * @param tableName 是表名,
     * @return 返回经过唯一性验证值，
     * 当前系统时间*10000+四位自然数，如2012-11-01某时某刻系统时间：1351749144390，修改后：13517491443897256；如果将来希望根据主键获取时间，则用主键值/10000即可，误差到1/100毫秒。
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
     * 插入指定键值<br/> 用字符串的形式给数字型和字符型的主键插入键值，但键值的格式必须与字段类型相符<br/> 使用单例模式和唯一性验证<br/>
     *
     * @return 插入指定的键值，如果成功则返回该值，如果不成功，则返回null。
     * @param tableName 是表名,
     * @param keyValue 键值
     * @return 若keyValue有效，则返回keyValue，否则返回null。
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
                String v = db.getTable(tableName).makeStringKeyValue(keyField, keyValue);//检查合法性、唯一性
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
     * 通用键值生成器，只对第一个主键或第一个字段有效<br/>
     * 支持的字段类型有：Long\Integer\Short\Float\Double\String(长度大于等于13)<br/>
     * 整数型自动加1；浮点型自动加1并取整，字符型插入Date的long值，如：1348315761671，字段宽度应当大于等于13<br/>
     * 使用单例模式和唯一性验证<br/>
     *
     * @param tableName 是表名,
     * @return 返回经过唯一性验证，自动增量值
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
     * 终结守卫者,最后一道安全屏障,关闭联接。开发人员不应依赖此方法来关闭联接,但以下情况发生后，可能造成数据库连接不能关闭，因此有必要保留终结守卫者模式。长期的实践证明，终结守卫者能起到良好的作用。<br/>
     * 1、网络连接终止；<br/> 2、应用程序中发生非SQLException异常后，可能导致数据库连接没有关闭。<br/>
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

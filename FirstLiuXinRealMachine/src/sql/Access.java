package sql;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

/*
 * 实现桌面数据库DML操作，不支持事务，不支持更多复杂的JDBC操作
 * 典型的桌面数据库有：dbase、微软Access、嵌入式JavaDB
 * 约定：
 * 对于返回Object[]、List、Map类型，若没有记录，返回结果均不能是null，而是包含0长度元素的对象；
 * 使用LinkedHashMap提取数据库记录，以保证结果记录与数据库记录的顺序一致；
 * 使用List<Map<String,Object>>类型，做中介，实现数据库与应用程序之间数据互换；
 * 私有方法提供足够多的参数，以便于解决复杂的问题；
 * 公有方法尽可能提供最少的参数，以便于用户使用。
 * 由于JDBC不能通过连接，获取桌面型数据库的主键信息，在更新处理时，将把表的第一个字段当做主键处理，必要时，用户必须指定主键字段名
 */
public class Access implements Jdml {

    private java.sql.Connection con;
    private java.sql.Statement stmt;
    private java.sql.ResultSet rs;
    private java.sql.ResultSetMetaData rsmd;
    private JadeTool tool = new JadeTool();
    private DbAccess db = null;//实例化时，Db使用的数据库连接类型与ProcessVO一致
    private int indexStepLength;

    /*
     * 默认构造函数。通过Db默认实例使用/META-INF/db.xml配置的数据库连接
     */
    public Access() {
        db = DbAccess.instance(DbConnection.getDefaultCon(), DbConnectionType.USING_CONFIG_OF_DEFAULT);
        con = DbConnection.getDefaultCon();

    }

    /**
     * 构造函数.
     *
     * @param con 通过Db默认实例使用/META-INF/db.xml配置的数据库连接。
     */
    public Access(Connection con) {

        db = DbAccess.instance(con, DbConnectionType.USING_CONFIG_OF_NONE);
        this.con = con;

    }

    /**
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
     * 获取数据库结构信息
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
     * 当发生异常时,取消查询操作,并释放Statement资源.
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
            throw ex;
        } finally {
            stmt.close();
        }
        return count;
    }

    /**
     * 插入记录，执行executeUpdate方法
     *
     * @param insertSql
     * @return 返回JDBC标准插入语句的返回值
     * @throws SQLException
     */
    @Override
    public int insert(String insertSql) throws SQLException {
        int num = this.update(insertSql);
        return num;
    }

    /**
     * 插入记录，执行executeUpdate方法
     *
     * @param insertSql
     * @return 返回JDBC标准插入语句的返回值
     * @throws SQLException
     */
    @Override
    public int insert(String[] insertSql) throws SQLException {
        int num = this.update(insertSql);
        return num;
    }

    /**
     * 核心方法,向表中插入一条记录。 零长度字符串将保存为null。
     *
     * @param tableName 是表名
     * @param mapRecord 是准备插入到表中的一条记录的数据,其键名与字段名相同,顺序无关;但键名不能是字段名以外的其它名称
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
     * 核心方法,向表中插入一条记录。 零长度字符串将保存为null。
     *
     * @param tableName 是表名
     * @param mapRecord 是准备插入到表中的一条记录的数据,其键名与字段名相同,顺序无关;但键名不能是字段名以外的其它名称
     * @param autoInsertKey 值为true时，自动插入主键
     * @return 返回JDBC标准插入语句的返回值
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
     * 调用executeInsert插入多条记录
     */
    @Override
    public int insert(String tableName, List<Map> listRecord) throws SQLException {
        int num = this._insert(tableName, listRecord, false);
        return num;
    }

    /**
     * 调用executeInsert插入多条记录
     *
     * @param tableName 表名
     * @param listRecord 记录集
     * @param autoInsertKey 值为true时，自动插入主键
     */
    @Override
    public int insert(String tableName, List<Map> listRecord, boolean autoInsertKey) throws SQLException {
        int num = this._insert(tableName, listRecord, true);
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
        return this._update(tableName, a, null);
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
        return this._update(tableName, a, where);
    }

    /**
     * 通过PreparedStatement自动更新多条记录
     *
     * @param tableName 表名
     * @param listRecord 包含完整主键字段的记录集
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
     * @param tableName 表名
     * @param listRecord 包含完整主键字段的记录集
     * @param where 更新条件
     */
    @Override
    public int update(String tableName, List<Map> listRecord, String where) throws SQLException {
        int num = 0;
        num = _update(tableName, listRecord, where);
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
        Object num = null;
        String[] keys = new String[]{db.getFields(tableName)[0]};
        num = this.saveOne(tableName, mapRecord, keys);
        return num;
    }

    /**
     * 保存一条记录.
     * 如果该记录存在，则更新之，如果该记录不存在，则插入。如果第一个主键的值为null||""，则自动插入新的主键值，这个方法不适合对含多主键的表进行插入操作，但不影响对多主键的表进行更新
     *
     * @param tableName 是表名
     * @param mapRecord 是准备插入到表中的一条记录的数据,其键名与字段名相同,顺序无关;但键名不能是字段名以外的其它名称;
     * @param keyFieldName 主键名数组
     * @return 返回第一个主键值。【注：原返回int型update返回值，更改为第一个主键值，将更方便应用】
     * @throws SQLException
     */
    public Object saveOne(String tableName, Map<String, Object> mapRecord, String[] keyFieldName) throws SQLException {
        Object kv = mapRecord.get(keyFieldName[0]);
        int n = 0;
        String _w = "";
        if (keyFieldName.length == 0) {
            n = this.insert(tableName, mapRecord);//无主键的表可以直接插入
            if (n > 0) {
                return kv;
            }
        } else {


            if (kv != null || !"".equals(kv)) {
                Object[] recordFields = mapRecord.keySet().toArray();
                Map _key_m = new LinkedHashMap();
                for (int i = 0; i < recordFields.length; i++) {
                    if (tool.isInFields(keyFieldName, recordFields[i].toString())) {
                        _key_m.put(recordFields[i].toString(), mapRecord.get(recordFields[i].toString()));//提取记录中的主键字段
                    }
                }
                if (!_key_m.isEmpty()) {
                    Object[] _k = _key_m.keySet().toArray();
                    if (_k.length != keyFieldName.length) {
                        return null;//多主键，但记录中主键不全，不能保存
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
            } else {// if (kv == null || "".equals(kv)) 
                Field f = db.getField(tableName, keyFieldName[0]);
                String className = f.getTypeClassName();
                if (className.equals("java.lang.Long")) {
                    kv = insertKey(tableName);
                    mapRecord.put(keyFieldName[0], kv);
                    n = update(tableName, mapRecord);//表有主键，但记录中不含主键记录||主键记录值是null||主键值是""，则插入，并自动插入主键
                }
                if (className.equals("java.lang.Integer") || className.equals("java.lang.Short") || className.equals("java.lang.Float") || className.equals("java.lang.Double") || className.equals("java.lang.String")) {
                    kv = insertAutoKey(tableName);
                    mapRecord.put(keyFieldName[0], kv);
                    n = update(tableName, mapRecord);//表有主键，但记录中不含主键记录||主键记录值是null||主键值是""，则插入，并自动插入主键
                }
            }
        }
        return kv;
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
    public int save(String tableName, Map<String, Object> mapRecord) throws SQLException {
        int num = 0;
        String[] keys = new String[]{db.getFields(tableName)[0]};
        num = this.save(tableName, mapRecord, keys);
        return num;
    }

    /**
     * 保存一条记录.
     * 如果该记录存在，则更新之，如果该记录不存在，则插入。如果第一个主键的值为null||""，则自动插入新的主键值，这个方法不适合对含多主键的表进行插入操作，但不影响对多主键的表进行更新
     *
     * @param tableName 是表名
     * @param mapRecord 是准备插入到表中的一条记录的数据,其键名与字段名相同,顺序无关;但键名不能是字段名以外的其它名称;
     * @param keyFieldName 主键名数组
     * @return 返回标准update方法返回的状态值
     * @throws SQLException
     */
    public int save(String tableName, Map<String, Object> mapRecord, String[] keyFieldName) throws SQLException {
        int num = 0;
        String _w = "";
        if (keyFieldName.length == 0) {
            return this.insert(tableName, mapRecord);//无主键的表可以直接插入
        } else {
            Object kv = mapRecord.get(keyFieldName[0].toString());
            if (kv == null || "".equals(kv)) {
                num = insert(tableName, mapRecord, true);//表有主键，但记录中不含主键记录||主键记录值是null||主键值是""，则插入，并自动插入主键
            } else {
                Object[] recordFields = mapRecord.keySet().toArray();
                Map _key_m = new LinkedHashMap();
                for (int i = 0; i < recordFields.length; i++) {
                    if (tool.isInFields(keyFieldName, recordFields[i].toString())) {
                        _key_m.put(recordFields[i].toString(), mapRecord.get(recordFields[i].toString()));//提取记录中的主键字段
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
     * 执行Statement的executeUpdate方法
     *
     * @param deleteSql 是一条标准的SQL更新语句，可以是插入、删除、更新.
     * @return 返回标准更新语句的返回值
     * @throws SQLException
     */
    @Override
    public int delete(String deleteSql) throws SQLException {
        return this.update(deleteSql);
    }

    /**
     * 执行Statement的executeUpdate方法
     *
     * @param deleteSql 是一条标准的SQL更新语句，可以是插入、删除、更新.
     * @return 返回标准更新语句的返回值
     * @throws SQLException
     */
    @Override
    public int delete(String[] deleteSql) throws SQLException {
        return this.update(deleteSql);
    }

    /**
     * 自动加1后取整，long型取时间值，好处更多，将来可以通过DateTool的构造函数DateTool(时间值)轻松提取各种时间信息
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
     * 对桌面型数据库插入
     *
     * 完成插入的步骤： 
     * 1、过滤记录中无效字段，得有效字段Object[] fields
     * 2、根据isUpdateKey对有效字段继续分组：true时，再得主键字段组Object[] keys和非主键字段组Object[] fields
     * 3、迭代记录for (Map record : listRecord) {}
     * 4、根据keys、fields、isUpdateKey及whereStr创建更新语句updateSQL
     * 5、执行更新stmt.executeUpdate(updateSQL);
     * 补充说明：
     * 1、更新时，若whereStr==null||"".equals(where)，则自动根据keys组合where条件语句，否则使用whereStr
     * 2、需要时，单独提供统一更新主键的方法 
     * 3、更新主键时，建议用户调用executeUpdate(String updateSql)方法；如: updateSQL="update label set label_id = 0, label = '软件' where groupid LIKE '软件工程'"; executeUpdate(updateSQL);
     *
     *
     * @param tableName 是一个表名
     * @param listRecord 是具有相同结构的一组记录
     * @param autoInsertKey 值为true时，自动插入主键值，而不使用记录给予的主键值 是从LinkedHashMap参数中获取的值
     * @throws SQLException
     */
    private int _insert(String tableName, List<Map> listRecord, String[] keyFields, boolean autoInsertKey) throws SQLException {
        Statement stmt0 = con.createStatement();
        int num = 0;
        Map<String, Object> _m = new LinkedHashMap(listRecord.get(0));//获取一条记录

        Object maxFieldValue = null;

        String[] tableFields = db.getFields(tableName);
        Object[] recordFields = (_m.keySet()).toArray(); //获取记录里的字段名的集合

        for (int i = 0; i < recordFields.length; i++) {
            if (!tool.isInFields(tableFields, recordFields[i].toString())) {
                _m.remove(recordFields[i].toString());//移除无效字段
            }
        }

        Object[] fields = (_m.keySet()).toArray(); //过滤后的有效字段
        try {
            long firstStringKey = (new java.util.Date()).getTime();

            for (Map record : listRecord) {
                String keyName = fields[0].toString();//设定第一个主键
                if (keyFields != null && keyFields.length > 0) {
                    keyName = keyFields[0];
                }

                //给第一个主键赋值
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
                    values[i] = record.get(fields[i]);//从记录中取值
                }
                String insertSQL = "insert into " + tableName + " (" + tool.arryToString(fields, ",") + " ) values (" + tool.arryToValues(values, ",") + ")";//构建插入语句

                stmt0.executeUpdate(insertSQL);  //插入语句范例：stmt.execute("insert into label(label_id,parent_id,groupid,label) values('1','0','行业分类','种植养殖')");
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
     * 对桌面型数据库插入
     * 
     * @param tableName 是一个表名
     * @param listRecord 是具有相同结构的一组记录
     * @param autoInsertKey 值为true时，自动插入主键值，而不使用记录给予的主键值
     * @throws SQLException
     */
    private int _insert(String tableName, List<Map> listRecord, boolean autoInsertKey) throws SQLException {
        int num = 0;
        String[] keyFields = new String[0];
        num = this._insert(tableName, listRecord, keyFields, autoInsertKey);
        return num;
    }

    /*
     * 对桌面型数据库更新
     * 
     * 完成更新的步骤：
     * 1、过滤记录中无效字段，得有效字段Object[] fields
     * 2、根据isUpdateKey对有效字段继续分组：true时，再得主键字段组Object[] keys和非主键字段组Object[] fields
     * 3、迭代记录for (Map record : listRecord) {}
     * 4、根据keys、fields、isUpdateKey及whereStr创建更新语句updateSQL
     * 5、执行更新stmt.executeUpdate(updateSQL);
     * 补充说明：
     * 1、更新时，若whereStr==null||"".equals(where)，则自动根据keys组合where条件语句，否则使用whereStr
     * 2、需要时，单独提供统一更新主键的方法
     * 3、更新主键时，建议用户调用executeUpdate(String updateSql)方法；如:
     *        updateSQL="update label set label_id = 0, label = '软件' where groupid LIKE '软件工程'";
     *        executeUpdate(updateSQL);
     *
     * @param tableName 是一个表名
     * @param listRecord 是具有相同结构的一组记录
     * @param  keyFieldName 主键
     * @param isUpdateKey 是否更新主键
     * @param whereStr 条件更新语句
     * @return 返回更新状态
     * @throws SQLException
     */
    private int _update(String tableName, List<Map> listRecord, String[] keyFieldName, boolean isUpdateKey, String whereStr) throws SQLException {
        Statement stmt0 = con.createStatement();
        int num = 0;
        Map<String, Object> _m = new LinkedHashMap(listRecord.get(0));//获取一条记录，作为过滤、分组依据
        String[] tableFields = db.getFields(tableName);
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
                if (tool.isInFields(keyFieldName, k0[i].toString())) {//记录中是否有主键
                    key_m.put(k0[i].toString(), _m.remove(k0[i].toString()));//将记录中的主键移到_key_m中；保证不对主键更新
                }
            }
        }
        Object[] fields = (_m.keySet()).toArray(); //记录中不包含主键的有效字段；再次过滤掉主键字段的结果
        Object[] keys = (key_m.keySet()).toArray(); //记录中包含的主键

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
                            return num;//无条件，不更新
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
     * 对桌面型数据库更新 把第一个字段当做主键处理
     *
     * @param tableName 是一个表名
     * @param listRecord 是具有相同结构的一组记录
     * 是操作条件，插入时不起作用，更新时，若where==null||"".equals(where)，则自动根据记录自身主键字段的键值对组合where条件语句
     * 是从LinkedHashMap参数中获取的值
     * @throws SQLException
     */
    private int _update(String tableName, List<Map> listRecord, String whereStr) throws SQLException {
        int num = 0;
        String[] keyFieldName = new String[]{db.getFields(tableName)[0]};
        num = _update(tableName, listRecord, keyFieldName, false, whereStr);
        return num;
    }

    /**
     * @return 返回Connection con对象
     */
    @Override
    public Connection getCon() {
        return this.con;
    }

    /**
     * @return 用于判断数据库连接是否关闭,返回true表示已关闭，返回false表示未关闭.
     * @throws SQLException
     *
     */
    @Override
    public boolean isClosed() throws SQLException {
        return con.isClosed();
    }

    /**
     * 查询方法
     *
     * @param sqlquery 是标准查询语句,可以是任意复杂的多表查询语句,但必须是受JDBC API支持的标准查询语句
     * @return 将查询结果ResultSet对象转换成List&lt;Map&lt;String,Object&gt;&gt;类型的结果
     */
    @Override
    public List query(String sqlquery) throws SQLException {
        List records = new ArrayList();
        Map valueMap = null;
        int fieldCount = 0;
        try {
            stmt = con.createStatement();
            //stmt.setQueryTimeout(timeout);//Access不支持此项功能
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
     * 索引查询一条记录
     *
     * @param tableName 表名
     * @param fields 待查询的字段
     * @param row_in_table 从该行开始，取值[1,记录总长度]，出界位置返回0长度List&lt;Map&gt;结果
     * @return 返回List&lt;Map&gt;类型的结果
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
     * @param row_in_table 行索引位置，indexRow>=1,indexRow<=表的记录数
     * @return 返回IndexNode
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
     * @param row_in_table 行索引位置，indexRow>=1,indexRow<=表的记录数
     * @param length 长度，节点的长度
     * @return 返回IndexNode数组
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
     * @param rebuildAnyway 是否无条件重建
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
     * 返回索引节点数组的长度
     */
    public int getIndexNodesLength(String tableName) {
        return db.getTable(tableName).getIndexNodes().length;
    }

    /**
     * 返回索引节点的步长
     */
    public int getIndexNodesStepLength() {
        return indexStepLength;
    }

    /**
     * 设置索引节点的步长
     */
    public void setIndexNodesStepLength(int indexStepLength) {
        this.indexStepLength = indexStepLength;
    }

    /**
     * 普通方法
     *
     * @param sqlquery 是标准查询语句,可以是任意复杂的多表查询语句,但必须是受JDBC API支持的标准查询语句
     * @return 获取查询结果ResultSet对象
     */
    @Override
    public ResultSet queryResultSet(String sqlquery) throws SQLException {
        stmt = con.createStatement();
        rs = stmt.executeQuery(sqlquery);
        return rs;
    }

    /**
     * 高级查询方法
     *
     * @param querySql 是标准查询语句,可以是任意复杂的多表查询语句,但必须是受JDBC API支持的标准查询语句
     * @param statement
     * 用户定义的Statement对象，用来实现更高级的查询，如可更新的、可滚动的、只读的、敏感的、非敏感的，<br/>可以通过 Statement
     * statement=getCon().createStatement(ResultSet.参数0，ResultSet.参数1);的方式实现
     * @return 获取查询结果ResultSet对象
     */
    @Override
    public ResultSet queryResultSet(String querySql, Statement statement) throws SQLException {
        rs = statement.executeQuery(querySql);
        return rs;
    }

    /**
     * Access不支持此项功能
     *
     * @deprecated
     * @param sqlquery 是标准查询语句,可以是任意复杂的多表查询语句,但必须是受JDBC API支持的标准查询语句
     * @param fetchSize 预取数
     * @return 获取查询结果集转化成List对象
     */
    @Override
    public List queryFetchSize(String sqlquery, int fetchSize) throws SQLException {
        List records = new ArrayList();
        return records;
    }

    /**
     * 在查询结果中获取第一条记录
     *
     * @param sqlQuery 查询语句
     * @return 获取一条记录。
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
     * @param tableName 表名
     * @return 获取表的最后一条记录
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
        Map m = this.queryOne(sql);;
        return m;
    }

    /**
     * @param tableName 是表名;
     * @param fieldName 关键字段名;
     * @param fieldValue 关键字段值.
     * @return 根据指定条件,获取一条记录,此方法主要用于标签。
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
     * 根据上次查询语句查询
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
     * 核心方法,将ResultSet结果集中的记录映射到Map对象中.
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
     * 关闭数据库联接
     *
     * @throws SQLException
     */
    @Override
    public void closeCon() throws SQLException {
        try {
            //if(stmt!=null&&!stmt.isClosed()){stmt.close();}//Access不支持条件判断，因此stmt在创建它的方法内关闭
        } finally {
            if (con != null && !con.isClosed()) {
                con.close();
            }
        }
    }

    /**
     * 插入长整形主键
     *
     * @param tableName 是表名,
     * @return
     * 当前系统时间*10000+四位自然数，如2012-11-01某时某刻系统时间：1351749144390，修改后：13517491443897256；如果将来希望根据主键获取时间，则用主键值/10000即可，精确到1/100毫秒。
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
     * 通用键值生成器，对数值型主键，插入新的键值
     * 支持的字段类型有：Long\Integer\Short\Float\Double\String(长度大于等于13)
     * 整数型自动加1；浮点型自动加1并取整，字符型插入Date的long值，如：1348315761671，字段宽度应当大于等于13
     *
     * @return 插入一条只有关键字值的记录,并返回该值。
     * @param tableName 是表名,
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
     * 用字符串的形式给数字型和字符型的主键插入键值，但键值的格式必须与字段类型相符
     *
     * @return 插入指定的键值，如果成功则返回该值，如果不成功，则返回null。
     * @param tableName 是表名,
     * @param keyValue 键值
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

    @Override
    public Db getDb() {
        return db;
    }
    /*
     * 终结守卫者,最后一道安全屏障,关闭联接。开发人员不应依赖此方法来关闭联接,但以下情况发生后，可能造成数据库连接不能关闭，因此有必要保留终结守卫者模式。长期的实践证明，终结守卫者能起到良好的作用。<br/>
     * 1、网络连接终止；<br/>
     * 2、应用程序中发生非SQLException异常。<br/>
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

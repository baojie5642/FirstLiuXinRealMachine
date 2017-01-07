/**
 * Jdml.java 2012-10-30 胡开明
 */
package sql;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

/**
 *
 * 实现数据库DML(CRUD)操作<br/>
 *
 * 1、ProcessVO()使用db.xml配置的数据库连接；<br/> 2、ProcessVO(Connection
 * con)，使用指定的数据库连接；<br/> 3、必须调用closeCon()关闭连接；<br/>
 * 4、建议用户在try{}catch(SQLException
 * ex){}finally{}结构中，打开事务，调用ProcessVO中的DML方法；<br/>
 *
 *
 * 方法名约定：<br/>
 * 操作数据库，使用insert、insertXXX、update、delete、query、queryOne、save、index命名<br/>
 * 其中，index、indexXXX是一组索引相关的查询<br/>
 *
 *
 * @author 胡开明
 *
 *
 * ======================关于方法命名========================= 将sql
 * dml操作的方法尽可能与sql的命令一致， 应为在jdbc中，不论是插入、更新、删除、以及DDl的操作均使用executeUpdate方法
 * 如果DbFree沿用这种命名方法，程序的可读性就差，
 * 使用insert、update、delete、query或其作为前缀命名，能增强程序的可读性；同时DbFree在这些不同的方法中，增加基本的sql语发验证，能有效的防止对数据库发出错误的请求，减轻数据库的负担。这样做可以有效地提高程序的健壮性。
 *
 */
public interface Jdml extends Serializable {

    int ADD_BEFORE = 0;
    int ADD_AFTER = 1;

    /**
     * 插入记录，执行executeUpdate
     *
     * @param insertSql
     * @return 返回JDBC标准插入语句的返回值
     * @throws SQLException
     */
    int insert(String insertSql) throws SQLException;

    /**
     * 插入多条记录，执行executeUpdate
     *
     * @param insertSql
     * @return 返回JDBC标准插入语句的返回值
     * @throws SQLException
     */
    int insert(String[] insertSql) throws SQLException;

    /**
     * 插入一条记录。 零长度字符串将保存为null。
     *
     * @param tableName 是表名
     * @param mapRecord 是准备插入到表中的一条记录的数据,其键名与字段名相同,顺序无关;但键名不能是字段名以外的其它名称
     * @return 返回JDBC标准插入语句的返回值
     * @throws SQLException
     */
    int insert(String tableName, Map<String, Object> mapRecord) throws SQLException;

    /**
     * 插入一条记录。 零长度字符串将保存为null。
     *
     * @param tableName 是表名
     * @param mapRecord 是准备插入到表中的一条记录的数据,其键名与字段名相同,顺序无关;但键名不能是字段名以外的其它名称
     * @param autoInsertKey 值为true时，自动插入主键
     * @return 返回JDBC标准插入语句的返回值
     * @throws SQLException
     */
    int insert(String tableName, Map<String, Object> mapRecord, boolean autoInsertKey) throws SQLException;

    /**
     * 插入多条记录
     */
    int insert(String tableName, List<Map> listRecord) throws SQLException;

    /**
     * 插入多条记录
     *
     * @param tableName 表名
     * @param listRecord 记录集
     * @param autoInsertKey 值为true时，自动插入主键
     */
    int insert(String tableName, List<Map> listRecord, boolean autoInsertKey) throws SQLException;

    /**
     *
     * 插入键值
     *
     * @return 插入一条只有关键字值的记录,并返回该值。
     * @param tableName 是表名,
     * @throws SQLException
     */
    Object insertAutoKey(String tableName) throws SQLException;

    /**
     * 插入长整形键值
     *
     * @return 插入一条只有关键字值的记录，这里以系统时间作为值，并返回该值。
     * @param tableName 是表名,
     * @throws SQLException
     *
     */
    Long insertKey(String tableName) throws SQLException;

    /**
     * 插入指定键值
     *
     * @return 插入指定的键值，如果成功则返回该值，如果不成功，则返回null。
     * @param tableName 是表名,
     * @param keyValue 第一个主键键值
     * @throws SQLException
     *
     */
    String insertKey(String tableName, String keyValue) throws SQLException;

    /**
     * 更新记录
     *
     * @param updateSql 是一条标准的SQL更新语句，可以是插入、删除、更新.
     * @return 返回标准更新语句的返回值
     * @throws SQLException
     */
    int update(String updateSql) throws SQLException;

    /**
     * 更新记录
     *
     * @param updateSql 是一组标准的SQL更新语句，可以是插入、删除、更新.
     * @return 返回标准更新语句的返回值
     * @throws SQLException
     */
    int update(String[] updateSql) throws SQLException;

    /**
     * 更新一条记录
     *
     * @param tableName 是表名
     * @param mapRecord 是准备插入到表中的一条记录的数据,其键名与字段名相同,顺序无关;但键名不能是字段名以外的其它名称;
     * @return 返回标准update方法的返回值
     * @throws SQLException
     */
    int update(String tableName, Map<String, Object> mapRecord) throws SQLException;

    /**
     * 更新一条记录
     *
     * @param tableName 是表名
     * @param mapRecord 是准备插入到表中的一条记录的数据,其键名与字段名相同,顺序无关;但键名不能是字段名以外的其它名称;
     * @param where 是一个标准的where子句.
     * @return 返回标准update方法的返回值
     * @throws SQLException
     */
    int update(String tableName, Map<String, Object> mapRecord, String where) throws SQLException;

    /**
     * 自动更新多条记录
     *
     * @param tableName 表名
     * @param listRecord 包含完整主键字段的记录集
     */
    int update(String tableName, List<Map> listRecord) throws SQLException;

    /**
     * 自动更新多条记录
     *
     * @param tableName 表名
     * @param listRecord 包含完整主键字段的记录集
     * @param where 更新条件
     */
    int update(String tableName, List<Map> listRecord, String where) throws SQLException;

    /**
     * 保存一条记录.
     * 如果该记录存在，则更新之，如果该记录不存在，则插入。如果第一个主键的值为null||""，则自动插入新的主键值，这个方法不适合对含多主键的表进行插入操作，但不影响对多主键的表进行更新
     *
     * @param tableName 是表名
     * @param mapRecord 是准备插入到表中的一条记录的数据,其键名与字段名相同,顺序无关;但键名不能是字段名以外的其它名称;
     * @return 返回标准update方法返回的状态值
     * @throws SQLException
     */
    int save(String tableName, Map<String, Object> mapRecord) throws SQLException;

    /**
     * 保存一条记录.
     * 如果该记录存在，则更新之，如果该记录不存在，则插入。如果第一个主键的值为null||""，则自动插入新的主键值，这个方法不适合对含多主键的表进行插入操作，但不影响对多主键的表进行更新
     *
     * @param tableName 是表名
     * @param mapRecord 是准备插入到表中的一条记录的数据,其键名与字段名相同,顺序无关;但键名不能是字段名以外的其它名称;
     * @return 返回第一个主键值。【注：原返回int型update返回值，更改为第一个主键值，将更方便应用】
     * @throws SQLException
     */
    Object saveOne(String tableName, Map<String, Object> mapRecord) throws SQLException;

    /**
     * 删除记录
     *
     * @param deleteSql 是一条标准的SQL更新语句，可以是插入、删除、更新.
     * @return 返回标准更新语句的返回值
     * @throws SQLException
     */
    int delete(String deleteSql) throws SQLException;

    /**
     * 多条删除
     *
     * @param deleteSql 是一条标准的SQL更新语句，可以是插入、删除、更新.
     * @return 返回标准更新语句的返回值
     * @throws SQLException
     */
    int delete(String[] deleteSql) throws SQLException;

    /**
     * 查询方法
     *
     * @param sqlquery 是标准查询语句,可以是任意复杂的多表查询语句,但必须是受JDBC API支持的标准查询语句
     * @return 将查询结果ResultSet对象转换成List&lt;Map&lt;String,Object&gt;&gt;类型的结果
     */
    List query(String sqlquery) throws SQLException;

    /**
     * 查询方法，根据上次查询语句查询
     *
     * @return 将查询结果ResultSet对象转换成List&lt;Map&lt;String,Object&gt;&gt;类型的结果
     */
    List queryByLast(int last) throws SQLException;

    /**
     * 查询记录数
     *
     * @param tableName 是表名.
     * @return 获取记录数.
     * @throws SQLException
     */
    long queryCount(String tableName) throws SQLException;

    /**
     * 查询记录数
     *
     * @param tableName 是表名
     * @param where String对象where是where子句部分
     * @return 获取记录数.
     * @throws SQLException
     */
    long queryCount(String tableName, String where) throws SQLException;

    /**
     * 查询
     *
     * @param sqlquery 是标准查询语句,可以是任意复杂的多表查询语句,但必须是受JDBC API支持的标准查询语句
     * @param fetchSize 预取数
     * @return 获取查询结果集转化成List对象
     */
    List queryFetchSize(String sqlquery, int fetchSize) throws SQLException;

    /**
     * 查询第一条记录
     *
     * @param tableName 表名
     * @return 获取表的最后一条记录
     * @throws SQLException
     */
    Map queryFirstRecord(String tableName) throws SQLException;

    /**
     * 查询最后一条记录
     *
     * @param tableName 表名
     * @return 获取表的最后一条记录
     * @throws SQLException
     */
    Map queryLastRecord(String tableName) throws SQLException;

    /**
     * 查询一条记录
     *
     * @param sqlQuery 查询语句
     * @return 获取一条记录。
     * @throws SQLException
     */
    Map queryOne(String sqlQuery) throws SQLException;

    /**
     * 查询一条记录
     *
     * @param tableName 是表名;
     * @param fieldName 字段名;
     * @param fieldValue 字段值.
     * @return 根据指定条件,获取一条记录,此方法主要用于标签。
     * @throws SQLException
     */
    Map queryOne(String tableName, String fieldName, Object fieldValue) throws SQLException;

    /**
     * 查询结果集
     *
     * @param querySql 是标准查询语句,可以是任意复杂的多表查询语句,但必须是受JDBC API支持的标准查询语句
     * @return 获取查询结果ResultSet对象
     */
    ResultSet queryResultSet(String querySql) throws SQLException;

    /**
     * 查询结果集
     *
     * @param querySql 是标准查询语句,可以是任意复杂的多表查询语句,但必须是受JDBC API支持的标准查询语句
     * @param statement
     * 用户定义的Statement对象，用来实现更高级的查询，如可更新的、可滚动的、只读的、敏感的、非敏感的，可以通过Statement
     * statement=getCon().createStatement(ResultSet.参数0，ResultSet.参数1);的方式实现
     * @return 获取查询结果ResultSet对象
     */
    ResultSet queryResultSet(String querySql, Statement statement) throws SQLException;

    /**
     * 查询数据库的结构信息
     *
     * @return 返回驱动器类型、数据库名、框架名、所有表名、所有表的所有字段名及其字段的各种信息
     */
    String queryDbInfo() throws SQLException;

    /**
     * 根据指定表名查询表的的结构信息
     *
     * @param tableName 指定表名
     * @return 返回表、及其所有字段的相关信息
     */
    String queryTableInfo(String tableName) throws SQLException;

    /**
     * 执行Statement的execute方法 负责执行创建、更新表的结构、...
     *
     * @param updateSql 是一条标准的SQL更新语句.
     * @return 返回标准更新语句的返回值
     * @throws SQLException
     */
    boolean execute(String updateSql) throws SQLException;

    /**
     * 执行Statement的executeUpdate方法，如：更新记录
     *
     * @param updateSql 是一组标准的SQL更新语句，可以是插入、删除、更新.
     * @return 返回标准更新语句的返回值
     * @throws SQLException
     */
    boolean execute(String[] updateSql) throws SQLException;

    /**
     * 调用并执行sql文件
     *
     * @param sqlFileName 是一组标准的SQL更新语句，可以是插入、删除、更新.
     * @return 返回是否执行成功
     * @throws SQLException
     */
    public boolean executeSqlFile(String sqlFileName) throws SQLException;

    /**
     * 获取连接
     *
     * @return 返回Connection con对象
     */
    Connection getCon();

    /**
     * 是否关闭连接
     *
     * @return 返回Connection con对象
     */
    boolean isClosed() throws SQLException;

    /**
     * 关闭连接
     */
    void closeCon() throws SQLException;

    /**
     * @return 返回Db实例
     */
    Db getDb();
}

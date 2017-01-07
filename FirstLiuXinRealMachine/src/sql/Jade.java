/*
 * Jade.java
 * Kaiming Hu
 * 2012-11-06
 * 
 * 责任链模式？
 * 
 * 创建Jade对象时，就创建了一个ProcessVO对象，并开启了非自动的事务。
 * 与ProcessVO对应的插入、更新、保存、删除等CRUD方法，均为非自动事务中游离状态的方法。在执行commit方法后，它们的结果才被保存在数据库中；在执行cancel方法后，它们的结果被取消。查询方法不受影响。
 * 发生异常时，均关闭数据库连接。
 * commit方法，实现一次完整的try{}catch(SQLException ex){}finally{}执行注册方法、事务提交或回滚、关闭连接的活动过程。
 * cancel方法，实现一次完整的try{}catch(SQLException ex){}finally{}执行注册方法、事务回滚、关闭连接的活动过程。
 * 无论如可，最后都应调用commit()或者cancel()方法，以保证结束非自动事务、关闭数据库连接。
 * 
 */
package sql;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * 方法名约定：<br/>
 * 操作数据库，使用insert、insertXXX、update、delete、query、queryOne、save、index命名<br/>
 * 其中，index、indexXXX是一组索引相关的查询<br/> 尽可能保持与标准的sql命令一致。
 *
 *
 * @author 胡开明
 * @since jadepool 1.0
 */
public class Jade implements java.io.Serializable {

    ProcessVO pvo = null;

    /**
     * 使用/META-INF/db.xml配置的数据库连接
     */
    public Jade() {
        try {
            pvo = new ProcessVO(DbConnection.getDefaultCon());
            pvo.setAutoCommit(false);
        } catch (SQLException ex) {
            Logger.getLogger(Jade.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    /**
     * 构造函数.
     *
     * @param con 使用指定的数据库连接
     */
    public Jade(Connection con) {
        try {
            pvo = new ProcessVO(con);
            pvo.setAutoCommit(false);
        } catch (SQLException ex) {
            Logger.getLogger(Jade.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
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
    public Jade(Connection con, int connectionType) {
        try {
            pvo = new ProcessVO(con, connectionType);
            pvo.setAutoCommit(false);
        } catch (SQLException ex) {
            Logger.getLogger(Jade.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

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
     * @param transactionIsolation 取值应当是Connection给定的常量之一 :
     */
    public void setTransactionIsolation(int transactionIsolation) {
        try {
            if (!pvo.isClosed()) {//没有关闭、没有回滚、没有自动提交
                pvo.setTransactionIsolation(transactionIsolation);
            }
        } catch (SQLException ex) {
            Logger.getLogger(Jade.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    /**
     * 插入记录
     *
     * @param insertSql 插入语句
     * @return 返回JDBC标准插入语句的返回值
     */
    public int insert(String insertSql) {//m000//插入方法以m0开头
        boolean commit = false;
        int num = 0;
        try {
            if (!pvo.isClosed()) {
                try {
                    num = pvo.insert(insertSql);
                    commit = true;
                } catch (SQLException ex) {
                    Logger.getLogger(Jade.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                    pvo.rollback();
                } finally {
                    if (!commit) {
                        pvo.closeCon();
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Jade.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return num;
    }

    /**
     * 插入一组记录
     *
     * @param insertSql 插入语句数组
     * @return 返回JDBC标准插入语句的返回值
     */
    public int insert(String[] insertSql) {//m001
        boolean commit = false;
        int num = 0;
        try {
            if (!pvo.isClosed()) {
                try {
                    num = pvo.insert(insertSql);
                    commit = true;
                } catch (SQLException ex) {
                    Logger.getLogger(Jade.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                    pvo.rollback();
                } finally {
                    if (!commit) {
                        pvo.closeCon();
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Jade.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return num;
    }

    /**
     * 插入一条记录
     *
     * @param tableName 是表名
     * @param mapRecord
     * 是准备插入到表中的一条记录的数据,其键名与字段名相同,顺序无关;但键名不能是字段名以外的其它名称，自动过滤无效字段数据
     * @return 返回JDBC标准插入语句的返回值
     */
    public int insert(String tableName, Map<String, Object> mapRecord) {//m002
        boolean commit = false;
        int num = 0;
        try {
            if (!pvo.isClosed()) {
                try {
                    num = pvo.insert(tableName, mapRecord);
                    commit = true;
                } catch (SQLException ex) {
                    Logger.getLogger(Jade.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                    pvo.rollback();
                } finally {
                    if (!commit) {
                        pvo.closeCon();
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Jade.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return num;
    }

    /**
     * 插入一条记录
     *
     * @param tableName 是表名
     * @param mapRecord 是准备插入到表中的一条记录的数据,其键名与字段名相同,顺序无关 ,自动过滤无效字段数据
     * @param autoInsertKey 值为true时，自动插入主键
     * @return 返回JDBC标准插入语句的返回值
     */
    public int insert(String tableName, Map<String, Object> mapRecord, boolean autoInsertKey) {//m003
        boolean commit = false;
        int num = 0;
        try {
            if (!pvo.isClosed()) {
                try {
                    num = pvo.insert(tableName, mapRecord, autoInsertKey);
                    commit = true;
                } catch (SQLException ex) {
                    Logger.getLogger(Jade.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                    pvo.rollback();
                } finally {
                    if (!commit) {
                        pvo.closeCon();
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Jade.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return num;
    }

    /**
     * 插入多条记录
     *
     * @param tableName 是表名
     * @param listRecord 是准备插入到表中的多条记录的数据,其键名与字段名相同,顺序无关 ,自动过滤无效字段数据
     * @return 返回JDBC标准插入语句的返回值
     */
    public int insert(String tableName, List<Map> listRecord) {//m004
        boolean commit = false;
        int num = 0;
        try {
            if (!pvo.isClosed()) {
                try {
                    num = pvo.insert(tableName, listRecord);
                    commit = true;
                } catch (SQLException ex) {
                    Logger.getLogger(Jade.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                    pvo.rollback();
                } finally {
                    if (!commit) {
                        pvo.closeCon();
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Jade.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return num;
    }

    /**
     * 插入多条记录
     *
     * @param tableName 是表名
     * @param listRecord 是准备插入到表中的多条记录的数据,其键名与字段名相同,顺序无关 ,自动过滤无效字段数据
     * @param autoInsertKey 值为true时，自动插入主键
     * @return 返回JDBC标准插入语句的返回值
     */
    public int insert(String tableName, List<Map> listRecord, boolean autoInsertKey) {//m005
        boolean commit = false;
        int num = 0;
        try {
            if (!pvo.isClosed()) {
                try {
                    num = pvo.insert(tableName, listRecord, autoInsertKey);
                    commit = true;
                } catch (SQLException ex) {
                    Logger.getLogger(Jade.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                    pvo.rollback();
                } finally {
                    if (!commit) {
                        pvo.closeCon();
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Jade.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return num;
    }

    /**
     * 通用键值生成器，只对第一个主键或第一个字段有效<br/>
     * 支持的字段类型有：Long\Integer\Short\Float\Double\String(长度大于等于13)<br/>
     * 整数型自动加1；浮点型自动加1并取整，字符型插入Date的long值，如：1348315761671，字段宽度应当大于等于13<br/>
     * 使用单例模式和唯一性验证<br/>
     *
     * @param tableName 是表名,
     * @return 返回经过唯一性验证，自动增量值
     */
    public Object insertAutoKey(String tableName) {//非注册方法，用户很可能需要立即执行，并将返回值付给其它方法，开启了事务，但未提交。插入的键值处于游离状态，必须在执行action后才能真正保存到数据库中
        boolean commit = false;
        Object o = null;
        try {
            if (!pvo.isClosed()) {
                try {
                    o = pvo.insertAutoKey(tableName);
                    commit = true;
                } catch (SQLException ex) {
                    Logger.getLogger(Jade.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                    pvo.rollback();
                } finally {
                    if (!commit) {
                        pvo.closeCon();
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Jade.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return o;
    }
    //private Object return006 = null;

    /**
     * 插入长整形主键值，只对第一个主键或第一个字段有效 使用单例模式和唯一性验证<br/>
     *
     * @param tableName 是表名,
     * @return 返回经过唯一性验证值，
     * 当前系统时间*10000+四位自然数，如2012-11-01某时某刻系统时间：1351749144390，修改后：13517491443897256；如果将来希望根据主键获取时间，则用主键值/10000即可，误差到1/100毫秒。
     */
    public Long insertKey(String tableName) {//非注册方法，用户很可能需要立即执行，并将返回值付给其它方法，开启了事务，但未提交。插入的键值处于游离状态，必须在执行action后才能真正保存到数据库中
        boolean commit = false;
        Long o = null;
        try {
            if (!pvo.isClosed()) {
                try {
                    o = pvo.insertKey(tableName);
                    commit = true;
                } catch (SQLException ex) {
                    Logger.getLogger(Jade.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                    pvo.rollback();
                } finally {
                    if (!commit) {
                        pvo.closeCon();
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Jade.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return o;
    }

    /**
     * 插入指定键值<br/> 用字符串的形式给数字型和字符型的主键插入键值，但键值的格式必须与字段类型相符<br/> 使用单例模式和唯一性验证<br/>
     *
     * @return 插入指定的键值，如果成功则返回该值，如果不成功，则返回null。
     * @param tableName 是表名,
     * @param keyValue 键值
     * @return 若keyValue有效，则返回keyValue，否则返回null。
     */
    public String insertKey(String tableName, String keyValue) {//非注册方法，用户很可能需要立即执行，并将返回值付给其它方法，开启了事务，但未提交。插入的键值处于游离状态，必须在执行action后才能真正保存到数据库中
        boolean commit = false;
        String o = null;
        try {
            if (!pvo.isClosed()) {
                try {
                    o = pvo.insertKey(tableName, keyValue);
                    commit = true;
                } catch (SQLException ex) {
                    Logger.getLogger(Jade.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                    pvo.rollback();
                } finally {
                    if (!commit) {
                        pvo.closeCon();
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Jade.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return o;
    }

    /**
     * 执行Statement的executeUpdate方法，如：插入、删除、更新记录
     *
     * @param updateSql 是一条标准的SQL更新语句，可以是插入、删除、更新.
     * @return 返回标准更新语句的返回值
     */
    public int update(String updateSql) {//m100//更新方法以m1开头
        boolean commit = false;
        int num = 0;
        try {
            if (!pvo.isClosed()) {
                try {
                    num = pvo.update(updateSql);
                    commit = true;
                } catch (SQLException ex) {
                    Logger.getLogger(Jade.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                    pvo.rollback();
                } finally {
                    if (!commit) {
                        pvo.closeCon();
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Jade.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return num;
    }

    /**
     * 执行Statement的executeUpdate方法，如：更新记录
     *
     * @param updateSql 是一组标准的SQL更新语句，可以是插入、删除、更新.
     * @return 返回标准更新语句的返回值
     */
    public int update(String[] updateSql) {//m101
        boolean commit = false;
        int num = 0;
        try {
            if (!pvo.isClosed()) {
                try {
                    num = pvo.update(updateSql);
                    commit = true;
                } catch (SQLException ex) {
                    Logger.getLogger(Jade.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                    pvo.rollback();
                } finally {
                    if (!commit) {
                        pvo.closeCon();
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Jade.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return num;
    }

    /**
     * 更新一条记录. 零长度字符串将保存为null。
     *
     * @param tableName 是表名
     * @param mapRecord 是准备插入到表中的一条记录的数据,其键名与字段名相同,顺序无关;但键名不能是字段名以外的其它名称;
     * @return 返回标准update方法的返回值
     */
    public int update(String tableName, Map<String, Object> mapRecord) {//m102
        boolean commit = false;
        int num = 0;
        try {
            if (!pvo.isClosed()) {
                try {
                    num = pvo.update(tableName, mapRecord);
                    commit = true;
                } catch (SQLException ex) {
                    Logger.getLogger(Jade.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                    pvo.rollback();
                } finally {
                    if (!commit) {
                        pvo.closeCon();
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Jade.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return num;
    }

    /**
     * 更新一条记录. 零长度字符串将保存为null。
     *
     * @param tableName 是表名
     * @param mapRecord 是准备插入到表中的一条记录的数据,其键名与字段名相同,顺序无关;但键名不能是字段名以外的其它名称;
     * @param where 是一个标准的where子句.
     * @return 返回标准update方法的返回值
     */
    public int update(String tableName, Map<String, Object> mapRecord, String where) {//m103
        boolean commit = false;
        int num = 0;
        try {
            if (!pvo.isClosed()) {
                try {
                    num = pvo.update(tableName, mapRecord, where);
                    commit = true;
                } catch (SQLException ex) {
                    Logger.getLogger(Jade.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                    pvo.rollback();
                } finally {
                    if (!commit) {
                        pvo.closeCon();
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Jade.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return num;
    }

    /**
     * 通过PreparedStatement自动更新多条记录
     *
     * @param tableName 表名
     * @param listRecord 包含完整主键字段的记录集
     * @return 返回标准update方法的返回值
     */
    public int update(String tableName, List<Map> listRecord) {//m104
        boolean commit = false;
        int num = 0;
        try {
            if (!pvo.isClosed()) {
                try {
                    num = pvo.update(tableName, listRecord);
                    commit = true;
                } catch (SQLException ex) {
                    Logger.getLogger(Jade.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                    pvo.rollback();
                } finally {
                    if (!commit) {
                        pvo.closeCon();
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Jade.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
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
     */
    public int update(String tableName, List<Map> listRecord, String where) {//m105
        boolean commit = false;
        int num = 0;
        try {
            if (!pvo.isClosed()) {
                try {
                    num = pvo.update(tableName, listRecord, where);
                    commit = true;
                } catch (SQLException ex) {
                    Logger.getLogger(Jade.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                    pvo.rollback();
                } finally {
                    if (!commit) {
                        pvo.closeCon();
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Jade.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return num;
    }

    /**
     * 保存一条记录.
     * 如果该记录存在，则更新之，如果该记录不存在，则插入。如果第一个主键的值为null||""，则自动插入新的主键值，这个方法不适合对含多主键的表进行插入操作，但不影响对多主键的表进行更新
     *
     * @param tableName 是表名
     * @param mapRecord 是准备插入到表中的一条记录的数据,其键名与字段名相同,顺序无关;但键名不能是字段名以外的其它名称;
     * @return 返回标准update方法返回的状态值
     */
    public int save(String tableName, Map<String, Object> mapRecord) {//m200//保存方法以m2开头
        boolean commit = false;
        int num = 0;
        try {
            if (!pvo.isClosed()) {
                try {
                    num = pvo.save(tableName, mapRecord);
                    commit = true;
                } catch (SQLException ex) {
                    Logger.getLogger(Jade.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                    pvo.rollback();
                } finally {
                    if (!commit) {
                        pvo.closeCon();
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Jade.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return num;
    }

    /**
     * 保存一条记录.
     * 如果该记录存在，则更新之，如果该记录不存在，则插入。如果第一个主键的值为null||""，则自动插入新的主键值，这个方法不适合对含多主键的表进行插入操作，但不影响对多主键的表进行更新
     *
     * @param tableName 是表名
     * @param mapRecord 是准备插入到表中的一条记录的数据,其键名与字段名相同,顺序无关;但键名不能是字段名以外的其它名称;
     * @return 返回第一个主键值。【注：原返回int型update返回值，更改为第一个主键值，将更方便应用】
     */
    public Object saveOne(String tableName, Map<String, Object> mapRecord) {//m200//保存方法以m2开头
        boolean commit = false;
        Object num = null;
        try {
            if (!pvo.isClosed()) {
                try {
                    num = pvo.saveOne(tableName, mapRecord);
                    commit = true;
                } catch (SQLException ex) {
                    Logger.getLogger(Jade.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                    pvo.rollback();
                } finally {
                    if (!commit) {
                        pvo.closeCon();
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Jade.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return num;
    }

    /**
     * 删除记录， 执行Statement的executeUpdate方法
     *
     * @param deleteSql 是一条标准的SQL删除语句
     * @return 返回标准更新语句的返回值
     */
    public int delete(String deleteSql) {//m300//删除方法以m3开头
        boolean commit = false;
        int num = 0;
        try {
            if (!pvo.isClosed()) {
                try {
                    num = pvo.delete(deleteSql);
                    commit = true;
                } catch (SQLException ex) {
                    Logger.getLogger(Jade.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                    pvo.rollback();
                } finally {
                    if (!commit) {
                        pvo.closeCon();
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Jade.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return num;
    }

    /**
     * 删除记录， 执行Statement的executeUpdate方法
     *
     * @param deleteSql 是多条标准的SQL删除语句
     * @return 返回标准更新语句的返回值
     */
    public int delete(String[] deleteSql) {//m301
        boolean commit = false;
        int num = 0;
        try {
            if (!pvo.isClosed()) {
                try {
                    num = pvo.delete(deleteSql);
                    commit = true;
                } catch (SQLException ex) {
                    Logger.getLogger(Jade.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                    pvo.rollback();
                } finally {
                    if (!commit) {
                        pvo.closeCon();
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Jade.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return num;
    }

    /**
     * 查询方法
     *
     * @param sqlquery 是标准查询语句,可以是任意复杂的多表查询语句,但必须是受JDBC API支持的标准查询语句
     * @return 将查询结果ResultSet对象转换成List&lt;Map&lt;String,Object&gt;&gt;类型的结果
     */
    public List query(String sqlquery) {//m400//查询方法以m4开头
        boolean commit = false;
        List num = new ArrayList();
        try {
            if (!pvo.isClosed()) {
                try {
                    num = pvo.query(sqlquery);
                    commit = true;
                } catch (SQLException ex) {
                    Logger.getLogger(Jade.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                    pvo.rollback();
                } finally {
                    if (!commit) {
                        pvo.closeCon();
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Jade.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return num;
    }

    /**
     * 查询方法
     *
     * @param sqlquery 是标准查询语句,可以是任意复杂的多表查询语句,但必须是受JDBC API支持的标准查询语句
     * @param timeout 设定查询时限，单位：秒；timeout=0,则查询不受时间限制
     * @return 将查询结果ResultSet对象转换成List&lt;Map&lt;String,Object&gt;&gt;类型的结果
     */
    public List query(String sqlquery, int timeout) {//m400//查询方法以m4开头
        boolean commit = false;
        List num = new ArrayList();
        try {
            if (!pvo.isClosed()) {
                try {
                    num = pvo.query(sqlquery, timeout);
                    commit = true;
                } catch (SQLException ex) {
                    Logger.getLogger(Jade.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                    pvo.rollback();
                } finally {
                    if (!commit) {
                        pvo.closeCon();
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Jade.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return num;
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
     */
    public List query(String sqlquery, int position, int length, boolean isScrollSenstive) {//m402
        boolean commit = false;
        List num = new ArrayList();
        try {
            if (!pvo.isClosed()) {
                try {
                    num = pvo.query(sqlquery, position, length, isScrollSenstive);
                    commit = true;
                } catch (SQLException ex) {
                    Logger.getLogger(Jade.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                    pvo.rollback();
                } finally {
                    if (!commit) {
                        pvo.closeCon();
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Jade.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return num;
    }

    /**
     * 根据上次查询语句查询
     *
     * @param last 查询语句栈中的倒序数
     */
    public List queryByLast(int last) {//m400//查询方法以m4开头
        boolean commit = false;
        List num = new ArrayList();
        try {
            if (!pvo.isClosed()) {
                try {
                    num = pvo.queryByLast(last);
                    commit = true;
                } catch (SQLException ex) {
                    Logger.getLogger(Jade.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                    pvo.rollback();
                } finally {
                    if (!commit) {
                        pvo.closeCon();
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Jade.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return num;
    }

    /**
     * 查询方法
     *
     * @param sqlquery 是标准查询语句,可以是任意复杂的多表查询语句,但必须是受JDBC API支持的标准查询语句
     * @param fetchSize 预取数
     * @return 获取查询结果集转化成List对象
     */
    public List queryFetchSize(String sqlquery, int fetchSize) {//m402
        boolean commit = false;
        List num = new ArrayList();
        try {
            if (!pvo.isClosed()) {
                try {
                    num = pvo.queryFetchSize(sqlquery, fetchSize);
                    commit = true;
                } catch (SQLException ex) {
                    Logger.getLogger(Jade.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                    pvo.rollback();
                } finally {
                    if (!commit) {
                        pvo.closeCon();
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Jade.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return num;
    }

    /**
     * 查询表中第一条记录
     *
     * @param tableName 表名
     * @return 获取表的第一条记录
     */
    public Map queryFirstRecord(String tableName) {//m403
        boolean commit = false;
        Map num = new LinkedHashMap();
        try {
            if (!pvo.isClosed()) {
                try {
                    num = pvo.queryFirstRecord(tableName);
                    commit = true;
                } catch (SQLException ex) {
                    Logger.getLogger(Jade.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                    pvo.rollback();
                } finally {
                    if (!commit) {
                        pvo.closeCon();
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Jade.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return num;
    }

    /**
     * 查询表中最后一条记录
     *
     * @param tableName 表名
     * @return 获取表的最后一条记录
     */
    public Map queryLastRecord(String tableName) {//m404
        boolean commit = false;
        Map num = new LinkedHashMap();
        try {
            if (!pvo.isClosed()) {
                try {
                    num = pvo.queryLastRecord(tableName);
                    commit = true;
                } catch (SQLException ex) {
                    Logger.getLogger(Jade.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                    pvo.rollback();
                } finally {
                    if (!commit) {
                        pvo.closeCon();
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Jade.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return num;
    }

    /**
     * 查询一条记录
     *
     * @param querySql 查询语句
     * @return 获取一条记录。
     */
    public Map queryOne(String querySql) {//m405
        boolean commit = false;
        Map num = new LinkedHashMap();
        try {
            if (!pvo.isClosed()) {
                try {
                    num = pvo.queryOne(querySql);
                    commit = true;
                } catch (SQLException ex) {
                    Logger.getLogger(Jade.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                    pvo.rollback();
                } finally {
                    if (!commit) {
                        pvo.closeCon();
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Jade.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return num;
    }

    /**
     * 查询一条记录
     *
     * @param tableName 是表名;
     * @param fieldName 关键字段名;
     * @param fieldValue 关键字段值.
     * @return 根据指定条件,获取一条记录。
     */
    public Map queryOne(String tableName, String fieldName, Object fieldValue) {//m406
        boolean commit = false;
        Map num = new LinkedHashMap();
        try {
            if (!pvo.isClosed()) {
                try {
                    num = pvo.queryOne(tableName, fieldName, fieldValue);
                    commit = true;
                } catch (SQLException ex) {
                    Logger.getLogger(Jade.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                    pvo.rollback();
                } finally {
                    if (!commit) {
                        pvo.closeCon();
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Jade.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return num;
    }

    /**
     * 索引查询一条记录
     *
     * @param tableName 表名
     * @param fields 待查询的字段
     * @param indexRow 从该行开始，取值[1,记录总长度]，出界位置返回0长度List&lt;Map&gt;结果
     * @return 返回List&lt;Map&gt;类型的结果
     */
    public Map indexByRow(String tableName, String[] fields, int indexRow) {//m407
        boolean commit = false;
        Map num = new LinkedHashMap();
        try {
            if (!pvo.isClosed()) {
                try {
                    num = pvo.indexByRow(tableName, fields, indexRow);
                    commit = true;
                } catch (SQLException ex) {
                    Logger.getLogger(Jade.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                    pvo.rollback();
                } finally {
                    if (!commit) {
                        pvo.closeCon();
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Jade.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return num;
    }

    /**
     * 索引查询多条记录
     *
     * @param tableName 表名
     * @param fields 待查询的字段
     * @param indexRow 从该行开始，取值[1,记录总长度]，出界位置返回0长度List&lt;Map&gt;结果
     * @param length 查询长度，row_in_table后记录数少于length，则返回其后的全部记录
     * @param asc 排列顺序，true顺序，false逆序
     * @return 返回List&lt;Map&gt;类型的结果
     */
    public List indexByRow(String tableName, String[] fields, int indexRow, int length, boolean asc) {//m408
        boolean commit = false;
        List num = new ArrayList();
        try {
            if (!pvo.isClosed()) {
                try {
                    num = pvo.indexByRow(tableName, fields, indexRow, length, asc);
                    commit = true;
                } catch (SQLException ex) {
                    Logger.getLogger(Jade.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                    pvo.rollback();
                } finally {
                    if (!commit) {
                        pvo.closeCon();
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Jade.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return num;
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
    public List indexByIndexNodes(String tableName, String[] fields, int position_of_indexNodes, boolean asc, boolean rebuildIndexNodes) {//m409
        boolean commit = false;
        List num = new ArrayList();
        try {
            if (!pvo.isClosed()) {
                try {
                    num = pvo.indexByIndexNodes(tableName, fields, position_of_indexNodes, asc, rebuildIndexNodes);
                    commit = true;
                } catch (SQLException ex) {
                    Logger.getLogger(Jade.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                    pvo.rollback();
                } finally {
                    if (!commit) {
                        pvo.closeCon();
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Jade.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return num;
    }

    /**
     * 索引节点数组的长度
     *
     * @param tableName 表名
     * @return 返回索引节点数组的长度
     */
    public int getIndexNodesLength(String tableName) {
        return pvo.getIndexNodesLength(tableName);
    }

    /**
     * @return 返回索引节点的步长
     */
    public int getIndexNodesStepLength() {
        return pvo.getIndexNodesStepLength();
    }

    /**
     * 设置索引节点的步长
     *
     * @param indexStepLength 新的索引节点的步长
     */
    public void setIndexNodesStepLength(int indexStepLength) {
        pvo.setIndexNodesStepLength(indexStepLength);
    }

    /**
     * 返回Db实例，以便于用户查询数据库及其表、字段的结构信息
     *
     * @return 当前使用的Db实例
     */
    public Db getDb() {
        return pvo.getDb();
    }

    /**
     * 查询表中记录
     *
     * @param tableName 是表名.
     * @return 获取记录数.
     */
    public long queryCount(String tableName) {//m500
        boolean commit = false;
        long num = 0;
        try {
            if (!pvo.isClosed()) {
                try {
                    num = pvo.queryCount(tableName);
                    commit = true;
                } catch (SQLException ex) {
                    Logger.getLogger(Jade.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                    pvo.rollback();
                } finally {
                    if (!commit) {
                        pvo.closeCon();
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Jade.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return num;
    }

    /**
     * 查询表中记录
     *
     * @param tableName 是表名
     * @param where String对象where是where子句部分
     * @return 获取记录数.
     */
    public long queryCount(String tableName, String where) {//m501
        boolean commit = false;
        long num = 0;
        try {
            if (!pvo.isClosed()) {
                try {
                    num = pvo.queryCount(tableName, where);
                    commit = true;
                } catch (SQLException ex) {
                    Logger.getLogger(Jade.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                    pvo.rollback();
                } finally {
                    if (!commit) {
                        pvo.closeCon();
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Jade.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return num;
    }

    /**
     * 查询数据库结构信息
     */
    public String queryDbInfo() {
        boolean commit = false;
        String num = "";
        try {
            if (!pvo.isClosed()) {
                try {
                    num = pvo.queryDbInfo();
                    commit = true;
                } catch (SQLException ex) {
                    Logger.getLogger(Jade.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                    pvo.rollback();
                } finally {
                    if (!commit) {
                        pvo.closeCon();
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Jade.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return num;
    }

    /**
     * 查询指定表的结构信息
     *
     * @param tableName
     * @return 返回表及其所有字段信息
     */
    public String queryTableInfo(String tableName) {
        boolean commit = false;
        String num = "";
        try {
            if (!pvo.isClosed()) {
                try {
                    num = pvo.queryTableInfo(tableName);
                    commit = true;
                } catch (SQLException ex) {
                    Logger.getLogger(Jade.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                    pvo.rollback();
                } finally {
                    if (!commit) {
                        pvo.closeCon();
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Jade.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return num;
    }

    /**
     * 执行Statement的execute方法 负责执行创建、更新表的结构、...
     *
     * @param sql 是一条标准的SQL更新语句.
     * @return 返回标准更新语句的返回值
     */
    public boolean execute(String sql) {
        boolean commit = false;
        boolean num = false;
        try {
            if (!pvo.isClosed()) {
                try {
                    num = pvo.execute(sql);
                    commit = true;
                } catch (SQLException ex) {
                    Logger.getLogger(Jade.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                    pvo.rollback();
                } finally {
                    if (!commit) {
                        pvo.closeCon();
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Jade.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return num;
    }

    /**
     * 执行Statement的executeUpdate方法，如：更新记录
     *
     * @param sql 是一组标准的SQL更新语句，可以是插入、删除、更新.
     * @return 返回标准更新语句的返回值
     */
    public boolean execute(String[] sql) {
        boolean commit = false;
        boolean num = false;
        try {
            if (!pvo.isClosed()) {
                try {
                    num = pvo.execute(sql);
                    commit = true;
                } catch (SQLException ex) {
                    Logger.getLogger(Jade.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                    pvo.rollback();
                } finally {
                    if (!commit) {
                        pvo.closeCon();
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Jade.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return num;
    }

    /**
     * 调用、执行一个sql文件
     *
     * @param sqlFileName 一个sql文件.
     * @return 是否执行成功
     */
    public boolean executeSqlFile(String sqlFileName) {
        boolean commit = false;
        boolean num = false;
        try {
            if (!pvo.isClosed()) {
                try {
                    num = pvo.executeSqlFile(sqlFileName);
                    commit = true;
                } catch (SQLException ex) {
                    Logger.getLogger(Jade.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                    pvo.rollback();
                } finally {
                    if (!commit) {
                        pvo.closeCon();
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Jade.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return num;
    }

    /**
     * 执行。先提交事务、后关闭，完成一次完整的事务性ProcessVO对象生命周期。
     */
    public void commit() {
        try {
            if (!pvo.isClosed()) {
                try {
                    pvo.commit();
                } catch (SQLException ex) {
                    Logger.getLogger(Jade.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                    pvo.rollback();
                } finally {
                    pvo.closeCon();
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Jade.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    /**
     * 取消。先回滚、后关闭。
     */
    public void cancel() {
        try {
            if (!pvo.isClosed()) {
                try {
                    pvo.rollback();
                } catch (SQLException ex) {
                    Logger.getLogger(Jade.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                } finally {
                    pvo.closeCon();
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Jade.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
    }
}

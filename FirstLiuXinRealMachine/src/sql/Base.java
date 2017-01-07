/*
 * Base.java
 * Kaiming Hu
 * 2012-11-07
 * 
 * Base.java主要是实现桌面型数据库的CRUD操作，桌面型数据库指的是Access、嵌入式JavaDB等一类的数据库，它们不提供数据库服务的支持、不支持事务。
 * 
 * 采用先注册，后执行模式。与Access对应的插入、更新、保存、删除、查询等CRUD方法，均为注册方法，它们将在action方法中执行。用户注册的方法，被保存在链表中，将按链表中的秩序被执行。
 * action方法，实现一次完整的try{}catch(SQLException ex){}finally{}执行注册方法、打开连接和关闭连接的活动过程。
 * 由于在Java中不能实现数值型指针，因此将插入、更新、删除的结果保存在State对象中，State是一个接口，其中唯一的方法num()保存了插入数、更新数、删除数等记录。
 * 查询结果与Access查询方法的结果对应不变。单条记录是Map<String,Object>对象，多条记录是List<Map<String,Object>>对象。
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
//import java.util.logging.Level;
//import java.util.logging.Logger;

/**
 *
 * @author hkm
 */
public class Base implements java.io.Serializable {

    Access pvo = null;

    public Base() {
        pvo = new Access(DbConnection.getDefaultCon());
    }

    public Base(Connection con) {
        pvo = new Access(con);
    }

    public int insert(String insertSql) {//m000//插入方法以m0开头
        boolean commit = false;
        int num = 0;
        try {
            if (!pvo.isClosed()) {
                try {
                    num = pvo.insert(insertSql);
                    commit = true;
                } catch (SQLException ex) {
                    Logger.getLogger(Base.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                } finally {
                    if (!commit) {
                        pvo.closeCon();
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Base.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return num;
    }

    public int insert(String[] insertSql) {//m001
        boolean commit = false;
        int num = 0;
        try {
            if (!pvo.isClosed()) {
                try {
                    num = pvo.insert(insertSql);
                    commit = true;
                } catch (SQLException ex) {
                    Logger.getLogger(Base.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                } finally {
                    if (!commit) {
                        pvo.closeCon();
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Base.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return num;
    }

    public int insert(String tableName, Map<String, Object> mapRecord) {//m002
        boolean commit = false;
        int num = 0;
        try {
            if (!pvo.isClosed()) {
                try {
                    num = pvo.insert(tableName, mapRecord);
                    commit = true;
                } catch (SQLException ex) {
                    Logger.getLogger(Base.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                } finally {
                    if (!commit) {
                        pvo.closeCon();
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Base.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return num;
    }

    public int insert(String tableName, Map<String, Object> mapRecord, boolean autoInsertKey) {//m003
        boolean commit = false;
        int num = 0;
        try {
            if (!pvo.isClosed()) {
                try {
                    num = pvo.insert(tableName, mapRecord, autoInsertKey);
                    commit = true;
                } catch (SQLException ex) {
                    Logger.getLogger(Base.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                } finally {
                    if (!commit) {
                        pvo.closeCon();
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Base.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return num;
    }

    public int insert(String tableName, List<Map> listRecord) {//m004
        boolean commit = false;
        int num = 0;
        try {
            if (!pvo.isClosed()) {
                try {
                    num = pvo.insert(tableName, listRecord);
                    commit = true;
                } catch (SQLException ex) {
                    Logger.getLogger(Base.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                } finally {
                    if (!commit) {
                        pvo.closeCon();
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Base.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return num;
    }

    public int insert(String tableName, List<Map> listRecord, boolean autoInsertKey) {//m005
        boolean commit = false;
        int num = 0;
        try {
            if (!pvo.isClosed()) {
                try {
                    num = pvo.insert(tableName, listRecord, autoInsertKey);
                    commit = true;
                } catch (SQLException ex) {
                    Logger.getLogger(Base.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                } finally {
                    if (!commit) {
                        pvo.closeCon();
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Base.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return num;
    }

    /**
     *
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
                    Logger.getLogger(Base.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                } finally {
                    if (!commit) {
                        pvo.closeCon();
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Base.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return o;
    }
    //private Object return006 = null;

    public Long insertKey(String tableName) {//非注册方法，用户很可能需要立即执行，并将返回值付给其它方法，开启了事务，但未提交。插入的键值处于游离状态，必须在执行action后才能真正保存到数据库中
        boolean commit = false;
        Long o = null;
        try {
            if (!pvo.isClosed()) {
                try {
                    o = pvo.insertKey(tableName);
                    commit = true;
                } catch (SQLException ex) {
                    Logger.getLogger(Base.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                } finally {
                    if (!commit) {
                        pvo.closeCon();
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Base.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return o;
    }

    public String insertKey(String tableName, String keyValue) {//非注册方法，用户很可能需要立即执行，并将返回值付给其它方法，开启了事务，但未提交。插入的键值处于游离状态，必须在执行action后才能真正保存到数据库中
        boolean commit = false;
        String o = null;
        try {
            if (!pvo.isClosed()) {
                try {
                    o = pvo.insertKey(tableName, keyValue);
                    commit = true;
                } catch (SQLException ex) {
                    Logger.getLogger(Base.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                } finally {
                    if (!commit) {
                        pvo.closeCon();
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Base.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return o;
    }
    //private String return008 = null;

    public int update(String updateSql) {//m100//更新方法以m1开头
        boolean commit = false;
        int num = 0;
        try {
            if (!pvo.isClosed()) {
                try {
                    num = pvo.update(updateSql);
                    commit = true;
                } catch (SQLException ex) {
                    Logger.getLogger(Base.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                } finally {
                    if (!commit) {
                        pvo.closeCon();
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Base.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return num;
    }

    public int update(String[] updateSql) {//m101
        boolean commit = false;
        int num = 0;
        try {
            if (!pvo.isClosed()) {
                try {
                    num = pvo.update(updateSql);
                    commit = true;
                } catch (SQLException ex) {
                    Logger.getLogger(Base.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                } finally {
                    if (!commit) {
                        pvo.closeCon();
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Base.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return num;
    }

    public int update(String tableName, Map<String, Object> mapRecord) {//m102
        boolean commit = false;
        int num = 0;
        try {
            if (!pvo.isClosed()) {
                try {
                    num = pvo.update(tableName, mapRecord);
                    commit = true;
                } catch (SQLException ex) {
                    Logger.getLogger(Base.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                } finally {
                    if (!commit) {
                        pvo.closeCon();
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Base.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return num;
    }

    public int update(String tableName, Map<String, Object> mapRecord, String where) {//m103
        boolean commit = false;
        int num = 0;
        try {
            if (!pvo.isClosed()) {
                try {
                    num = pvo.update(tableName, mapRecord, where);
                    commit = true;
                } catch (SQLException ex) {
                    Logger.getLogger(Base.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                } finally {
                    if (!commit) {
                        pvo.closeCon();
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Base.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return num;
    }

    public int update(String tableName, List<Map> listRecord) {//m104
        boolean commit = false;
        int num = 0;
        try {
            if (!pvo.isClosed()) {
                try {
                    num = pvo.update(tableName, listRecord);
                    commit = true;
                } catch (SQLException ex) {
                    Logger.getLogger(Base.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                } finally {
                    if (!commit) {
                        pvo.closeCon();
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Base.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return num;
    }

    public int update(String tableName, List<Map> listRecord, String where) {//m105
        boolean commit = false;
        int num = 0;
        try {
            if (!pvo.isClosed()) {
                try {
                    num = pvo.update(tableName, listRecord, where);
                    commit = true;
                } catch (SQLException ex) {
                    Logger.getLogger(Base.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                } finally {
                    if (!commit) {
                        pvo.closeCon();
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Base.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return num;
    }

    public int save(String tableName, Map<String, Object> mapRecord) {//m200//保存方法以m2开头
        boolean commit = false;
        int num = 0;
        try {
            if (!pvo.isClosed()) {
                try {
                    num = pvo.save(tableName, mapRecord);
                    commit = true;
                } catch (SQLException ex) {
                    Logger.getLogger(Base.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                } finally {
                    if (!commit) {
                        pvo.closeCon();
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Base.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return num;
    }

    public Object saveOne(String tableName, Map<String, Object> mapRecord) {
        boolean commit = false;
        Object num = null;
        try {
            if (!pvo.isClosed()) {
                try {
                    num = pvo.saveOne(tableName, mapRecord);
                    commit = true;
                } catch (SQLException ex) {
                    Logger.getLogger(Base.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                } finally {
                    if (!commit) {
                        pvo.closeCon();
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Base.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return num;
    }

    public int delete(String deleteSql) {//m300//删除方法以m3开头
        boolean commit = false;
        int num = 0;
        try {
            if (!pvo.isClosed()) {
                try {
                    num = pvo.delete(deleteSql);
                    commit = true;
                } catch (SQLException ex) {
                    Logger.getLogger(Base.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                } finally {
                    if (!commit) {
                        pvo.closeCon();
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Base.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return num;
    }

    public int delete(String[] deleteSql) {//m301
        boolean commit = false;
        int num = 0;
        try {
            if (!pvo.isClosed()) {
                try {
                    num = pvo.delete(deleteSql);
                    commit = true;
                } catch (SQLException ex) {
                    Logger.getLogger(Base.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                } finally {
                    if (!commit) {
                        pvo.closeCon();
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Base.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return num;
    }

    public List query(String querySql) {//m400//查询方法以m4开头
        boolean commit = false;
        List num = new ArrayList();
        try {
            if (!pvo.isClosed()) {
                try {
                    num = pvo.query(querySql);
                    commit = true;
                } catch (SQLException ex) {
                    Logger.getLogger(Base.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                } finally {
                    if (!commit) {
                        pvo.closeCon();
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Base.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return num;
    }

    public List queryByLast(int last) {//m400//查询方法以m4开头
        boolean commit = false;
        List num = new ArrayList();
        try {
            if (!pvo.isClosed()) {
                try {
                    num = pvo.queryByLast(last);
                    commit = true;
                } catch (SQLException ex) {
                    Logger.getLogger(Base.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                } finally {
                    if (!commit) {
                        pvo.closeCon();
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Base.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return num;
    }

    public List queryFetchSize(String querySql, int fetchSize) {//m402
        boolean commit = false;
        List num = new ArrayList();
        try {
            if (!pvo.isClosed()) {
                try {
                    num = pvo.queryFetchSize(querySql, fetchSize);
                    commit = true;
                } catch (SQLException ex) {
                    Logger.getLogger(Base.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);

                } finally {
                    if (!commit) {
                        pvo.closeCon();
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Base.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return num;
    }

    public Map queryFirstRecord(String tableName) {//m403
        boolean commit = false;
        Map num = new LinkedHashMap();
        try {
            if (!pvo.isClosed()) {
                try {
                    num = pvo.queryFirstRecord(tableName);
                    commit = true;
                } catch (SQLException ex) {
                    Logger.getLogger(Base.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);

                } finally {
                    if (!commit) {
                        pvo.closeCon();
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Base.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return num;
    }

    public Map queryLastRecord(String tableName) {//m404
        boolean commit = false;
        Map num = new LinkedHashMap();
        try {
            if (!pvo.isClosed()) {
                try {
                    num = pvo.queryLastRecord(tableName);
                    commit = true;
                } catch (SQLException ex) {
                    Logger.getLogger(Base.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);

                } finally {
                    if (!commit) {
                        pvo.closeCon();
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Base.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return num;
    }

    public Map queryOne(String querySql) {//m405
        boolean commit = false;
        Map num = new LinkedHashMap();
        try {
            if (!pvo.isClosed()) {
                try {
                    num = pvo.queryOne(querySql);
                    commit = true;
                } catch (SQLException ex) {
                    Logger.getLogger(Base.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);

                } finally {
                    if (!commit) {
                        pvo.closeCon();
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Base.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return num;
    }

    public Map queryOne(String tableName, String fieldName, Object fieldValue) {//m406
        boolean commit = false;
        Map num = new LinkedHashMap();
        try {
            if (!pvo.isClosed()) {
                try {
                    num = pvo.queryOne(tableName, fieldName, fieldValue);
                    commit = true;
                } catch (SQLException ex) {
                    Logger.getLogger(Base.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);

                } finally {
                    if (!commit) {
                        pvo.closeCon();
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Base.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return num;
    }

    public Map indexByRow(String tableName, String[] fields, int indexRow) {//m407
        boolean commit = false;
        Map num = new LinkedHashMap();
        try {
            if (!pvo.isClosed()) {
                try {
                    num = pvo.indexByRow(tableName, fields, indexRow);
                    commit = true;
                } catch (SQLException ex) {
                    Logger.getLogger(Base.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);

                } finally {
                    if (!commit) {
                        pvo.closeCon();
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Base.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return num;
    }

    public List indexByRow(String tableName, String[] fields, int indexRow, int length, boolean asc) {//m408
        boolean commit = false;
        List num = new ArrayList();
        try {
            if (!pvo.isClosed()) {
                try {
                    num = pvo.indexByRow(tableName, fields, indexRow, length, asc);
                    commit = true;
                } catch (SQLException ex) {
                    Logger.getLogger(Base.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);

                } finally {
                    if (!commit) {
                        pvo.closeCon();
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Base.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return num;
    }

    public List indexByIndexNodes(String tableName, String[] fields, int position_of_indexNodes, boolean asc, boolean rebuildIndexNodes) {//m409
        boolean commit = false;
        List num = new ArrayList();
        try {
            if (!pvo.isClosed()) {
                try {
                    num = pvo.indexByIndexNodes(tableName, fields, position_of_indexNodes, asc, rebuildIndexNodes);
                    commit = true;
                } catch (SQLException ex) {
                    Logger.getLogger(Base.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);

                } finally {
                    if (!commit) {
                        pvo.closeCon();
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Base.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return num;
    }

    public int getIndexNodesLength(String tableName) {
        return pvo.getIndexNodesLength(tableName);
    }

    public int getIndexNodesStepLength() {
        return pvo.getIndexNodesStepLength();
    }

    public void setIndexNodesStepLength(int stepLength) {
        pvo.setIndexNodesStepLength(stepLength);
    }
    
    public Db getDb() {
        return pvo.getDb();
    }

    public long queryCount(String tableName) {//m500
        boolean commit = false;
        long num = 0;
        try {
            if (!pvo.isClosed()) {
                try {
                    num = pvo.queryCount(tableName);
                    commit = true;
                } catch (SQLException ex) {
                    Logger.getLogger(Base.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);

                } finally {
                    if (!commit) {
                        pvo.closeCon();
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Base.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return num;
    }

    public long queryCount(String tableName, String where) {//m501
        boolean commit = false;
        long num = 0;
        try {
            if (!pvo.isClosed()) {
                try {
                    num = pvo.queryCount(tableName, where);
                    commit = true;
                } catch (SQLException ex) {
                    Logger.getLogger(Base.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                } finally {
                    if (!commit) {
                        pvo.closeCon();
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Base.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return num;
    }

    public String queryDbInfo() {
        boolean commit = false;
        String num = "";
        try {
            if (!pvo.isClosed()) {
                try {
                    num = pvo.queryDbInfo();
                    commit = true;
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

    public String queryTableInfo(String tableName) {
        boolean commit = false;
        String num = "";
        try {
            if (!pvo.isClosed()) {
                try {
                    num = pvo.queryTableInfo(tableName);
                    commit = true;
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

    public boolean execute(String executeSql) {
        boolean commit = false;
        boolean num = false;
        try {
            if (!pvo.isClosed()) {
                try {
                    num = pvo.execute(executeSql);
                    commit = true;
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

    public boolean execute(String[] executeSql) {
        boolean commit = false;
        boolean num = false;
        try {
            if (!pvo.isClosed()) {
                try {
                    num = pvo.execute(executeSql);
                    commit = true;
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

    public boolean executeSqlFile(String sqlFileName) {
        boolean commit = false;
        boolean num = false;
        try {
            if (!pvo.isClosed()) {
                try {
                    num = pvo.executeSqlFile(sqlFileName);
                    commit = true;
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
     * 关闭，完成一次完整的事务性Access对象生命周期。
     */
    public void closeCon() {
        try {
            if (!pvo.isClosed()) {
                pvo.closeCon();
            }
        } catch (SQLException ex) {
            Logger.getLogger(Base.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
    }
}

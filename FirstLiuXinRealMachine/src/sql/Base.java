/*
 * Base.java
 * Kaiming Hu
 * 2012-11-07
 * 
 * Base.java��Ҫ��ʵ�����������ݿ��CRUD���������������ݿ�ָ����Access��Ƕ��ʽJavaDB��һ������ݿ⣬���ǲ��ṩ���ݿ�����֧�֡���֧������
 * 
 * ������ע�ᣬ��ִ��ģʽ����Access��Ӧ�Ĳ��롢���¡����桢ɾ������ѯ��CRUD��������Ϊע�᷽�������ǽ���action������ִ�С��û�ע��ķ������������������У����������е�����ִ�С�
 * action������ʵ��һ��������try{}catch(SQLException ex){}finally{}ִ��ע�᷽���������Ӻ͹ر����ӵĻ���̡�
 * ������Java�в���ʵ����ֵ��ָ�룬��˽����롢���¡�ɾ���Ľ��������State�����У�State��һ���ӿڣ�����Ψһ�ķ���num()�����˲���������������ɾ�����ȼ�¼��
 * ��ѯ�����Access��ѯ�����Ľ����Ӧ���䡣������¼��Map<String,Object>���󣬶�����¼��List<Map<String,Object>>����
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

    public int insert(String insertSql) {//m000//���뷽����m0��ͷ
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
    public Object insertAutoKey(String tableName) {//��ע�᷽�����û��ܿ�����Ҫ����ִ�У���������ֵ�����������������������񣬵�δ�ύ������ļ�ֵ��������״̬��������ִ��action������������浽���ݿ���
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

    public Long insertKey(String tableName) {//��ע�᷽�����û��ܿ�����Ҫ����ִ�У���������ֵ�����������������������񣬵�δ�ύ������ļ�ֵ��������״̬��������ִ��action������������浽���ݿ���
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

    public String insertKey(String tableName, String keyValue) {//��ע�᷽�����û��ܿ�����Ҫ����ִ�У���������ֵ�����������������������񣬵�δ�ύ������ļ�ֵ��������״̬��������ִ��action������������浽���ݿ���
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

    public int update(String updateSql) {//m100//���·�����m1��ͷ
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

    public int save(String tableName, Map<String, Object> mapRecord) {//m200//���淽����m2��ͷ
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

    public int delete(String deleteSql) {//m300//ɾ��������m3��ͷ
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

    public List query(String querySql) {//m400//��ѯ������m4��ͷ
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

    public List queryByLast(int last) {//m400//��ѯ������m4��ͷ
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
     * �رգ����һ��������������Access�����������ڡ�
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

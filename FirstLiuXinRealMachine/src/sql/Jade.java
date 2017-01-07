/*
 * Jade.java
 * Kaiming Hu
 * 2012-11-06
 * 
 * ������ģʽ��
 * 
 * ����Jade����ʱ���ʹ�����һ��ProcessVO���󣬲������˷��Զ�������
 * ��ProcessVO��Ӧ�Ĳ��롢���¡����桢ɾ����CRUD��������Ϊ���Զ�����������״̬�ķ�������ִ��commit���������ǵĽ���ű����������ݿ��У���ִ��cancel���������ǵĽ����ȡ������ѯ��������Ӱ�졣
 * �����쳣ʱ�����ر����ݿ����ӡ�
 * commit������ʵ��һ��������try{}catch(SQLException ex){}finally{}ִ��ע�᷽���������ύ��ع����ر����ӵĻ���̡�
 * cancel������ʵ��һ��������try{}catch(SQLException ex){}finally{}ִ��ע�᷽��������ع����ر����ӵĻ���̡�
 * ������ɣ����Ӧ����commit()����cancel()�������Ա�֤�������Զ����񡢹ر����ݿ����ӡ�
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
 * ������Լ����<br/>
 * �������ݿ⣬ʹ��insert��insertXXX��update��delete��query��queryOne��save��index����<br/>
 * ���У�index��indexXXX��һ��������صĲ�ѯ<br/> �����ܱ������׼��sql����һ�¡�
 *
 *
 * @author ������
 * @since jadepool 1.0
 */
public class Jade implements java.io.Serializable {

    ProcessVO pvo = null;

    /**
     * ʹ��/META-INF/db.xml���õ����ݿ�����
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
     * ���캯��.
     *
     * @param con ʹ��ָ�������ݿ�����
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
     * ���캯��.������캯�����Ը���connectionTypeֵ������ͬ��Dbʵ�����������ڲ�ͬ���ݿ�֮�����ݵĽ���
     *
     * @param con ʹ��ָ�������ݿ�����
     * @param connectionType ���ӵ����� ���ӵ����ͣ�Ŀǰ������������<br/>
     * DbConnectionType.USING_CONFIG_OF_DEFAULT��ʵ�ʵ�ֵ1����Ҫdb.xml�����ļ�<br/>
     * DbConnectionType.USING_CONFIG_OF_NONE��ʵ�ʵ�ֵ0����Ҫָ����con<br/>
     * DbConnectionType.USING_DB_01��ʵ�ʵ�ֵ101����Ҫָ����con<br/>
     * DbConnectionType.USING_DB_02��ʵ�ʵ�ֵ102����Ҫָ����con<br/>
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
     * ����������뼶��Ĭ�ϵ�������뼶����Connection.TRANSACTION_READ_COMMITTED�����������Connection���ĵ�
     * <pre>
     *         Connection.TRANSACTION_NONE=              0;        ����ʹ��
     *         Connection.TRANSACTION_READ_UNCOMMITTED = 1;        dirty reads, non-repeatable reads and phantom reads can occur
     *         Connection.TRANSACTION_READ_COMMITTED   = 2;        dirty reads are prevented; non-repeatable reads and phantom reads can occur
     *         Connection.TRANSACTION_REPEATABLE_READ  = 4;        dirty reads and non-repeatable reads are prevented; phantom reads can occur.
     *         Connection.TRANSACTION_SERIALIZABLE     = 8;        dirty reads, non-repeatable reads and phantom reads are prevented
     * </pre>
     *
     * @param transactionIsolation ȡֵӦ����Connection�����ĳ���֮һ :
     */
    public void setTransactionIsolation(int transactionIsolation) {
        try {
            if (!pvo.isClosed()) {//û�йرա�û�лع���û���Զ��ύ
                pvo.setTransactionIsolation(transactionIsolation);
            }
        } catch (SQLException ex) {
            Logger.getLogger(Jade.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    /**
     * �����¼
     *
     * @param insertSql �������
     * @return ����JDBC��׼�������ķ���ֵ
     */
    public int insert(String insertSql) {//m000//���뷽����m0��ͷ
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
     * ����һ���¼
     *
     * @param insertSql �����������
     * @return ����JDBC��׼�������ķ���ֵ
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
     * ����һ����¼
     *
     * @param tableName �Ǳ���
     * @param mapRecord
     * ��׼�����뵽���е�һ����¼������,��������ֶ�����ͬ,˳���޹�;�������������ֶ���������������ƣ��Զ�������Ч�ֶ�����
     * @return ����JDBC��׼�������ķ���ֵ
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
     * ����һ����¼
     *
     * @param tableName �Ǳ���
     * @param mapRecord ��׼�����뵽���е�һ����¼������,��������ֶ�����ͬ,˳���޹� ,�Զ�������Ч�ֶ�����
     * @param autoInsertKey ֵΪtrueʱ���Զ���������
     * @return ����JDBC��׼�������ķ���ֵ
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
     * ���������¼
     *
     * @param tableName �Ǳ���
     * @param listRecord ��׼�����뵽���еĶ�����¼������,��������ֶ�����ͬ,˳���޹� ,�Զ�������Ч�ֶ�����
     * @return ����JDBC��׼�������ķ���ֵ
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
     * ���������¼
     *
     * @param tableName �Ǳ���
     * @param listRecord ��׼�����뵽���еĶ�����¼������,��������ֶ�����ͬ,˳���޹� ,�Զ�������Ч�ֶ�����
     * @param autoInsertKey ֵΪtrueʱ���Զ���������
     * @return ����JDBC��׼�������ķ���ֵ
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
     * ͨ�ü�ֵ��������ֻ�Ե�һ���������һ���ֶ���Ч<br/>
     * ֧�ֵ��ֶ������У�Long\Integer\Short\Float\Double\String(���ȴ��ڵ���13)<br/>
     * �������Զ���1���������Զ���1��ȡ�����ַ��Ͳ���Date��longֵ���磺1348315761671���ֶο��Ӧ�����ڵ���13<br/>
     * ʹ�õ���ģʽ��Ψһ����֤<br/>
     *
     * @param tableName �Ǳ���,
     * @return ���ؾ���Ψһ����֤���Զ�����ֵ
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
     * ���볤��������ֵ��ֻ�Ե�һ���������һ���ֶ���Ч ʹ�õ���ģʽ��Ψһ����֤<br/>
     *
     * @param tableName �Ǳ���,
     * @return ���ؾ���Ψһ����ֵ֤��
     * ��ǰϵͳʱ��*10000+��λ��Ȼ������2012-11-01ĳʱĳ��ϵͳʱ�䣺1351749144390���޸ĺ�13517491443897256���������ϣ������������ȡʱ�䣬��������ֵ/10000���ɣ���1/100���롣
     */
    public Long insertKey(String tableName) {//��ע�᷽�����û��ܿ�����Ҫ����ִ�У���������ֵ�����������������������񣬵�δ�ύ������ļ�ֵ��������״̬��������ִ��action������������浽���ݿ���
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
     * ����ָ����ֵ<br/> ���ַ�������ʽ�������ͺ��ַ��͵����������ֵ������ֵ�ĸ�ʽ�������ֶ��������<br/> ʹ�õ���ģʽ��Ψһ����֤<br/>
     *
     * @return ����ָ���ļ�ֵ������ɹ��򷵻ظ�ֵ��������ɹ����򷵻�null��
     * @param tableName �Ǳ���,
     * @param keyValue ��ֵ
     * @return ��keyValue��Ч���򷵻�keyValue�����򷵻�null��
     */
    public String insertKey(String tableName, String keyValue) {//��ע�᷽�����û��ܿ�����Ҫ����ִ�У���������ֵ�����������������������񣬵�δ�ύ������ļ�ֵ��������״̬��������ִ��action������������浽���ݿ���
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
     * ִ��Statement��executeUpdate�������磺���롢ɾ�������¼�¼
     *
     * @param updateSql ��һ����׼��SQL������䣬�����ǲ��롢ɾ��������.
     * @return ���ر�׼�������ķ���ֵ
     */
    public int update(String updateSql) {//m100//���·�����m1��ͷ
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
     * ִ��Statement��executeUpdate�������磺���¼�¼
     *
     * @param updateSql ��һ���׼��SQL������䣬�����ǲ��롢ɾ��������.
     * @return ���ر�׼�������ķ���ֵ
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
     * ����һ����¼. �㳤���ַ���������Ϊnull��
     *
     * @param tableName �Ǳ���
     * @param mapRecord ��׼�����뵽���е�һ����¼������,��������ֶ�����ͬ,˳���޹�;�������������ֶ����������������;
     * @return ���ر�׼update�����ķ���ֵ
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
     * ����һ����¼. �㳤���ַ���������Ϊnull��
     *
     * @param tableName �Ǳ���
     * @param mapRecord ��׼�����뵽���е�һ����¼������,��������ֶ�����ͬ,˳���޹�;�������������ֶ����������������;
     * @param where ��һ����׼��where�Ӿ�.
     * @return ���ر�׼update�����ķ���ֵ
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
     * ͨ��PreparedStatement�Զ����¶�����¼
     *
     * @param tableName ����
     * @param listRecord �������������ֶεļ�¼��
     * @return ���ر�׼update�����ķ���ֵ
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
     * ����һ����¼.
     * ����ü�¼���ڣ������֮������ü�¼�����ڣ�����롣�����һ��������ֵΪnull||""�����Զ������µ�����ֵ������������ʺ϶Ժ��������ı���в������������Ӱ��Զ������ı���и���
     *
     * @param tableName �Ǳ���
     * @param listRecord ��׼�����뵽���еĶ�����¼������,��������ֶ�����ͬ,˳���޹�;�������������ֶ����������������;
     * @param where ��һ����׼��where�Ӿ�.
     * @return ���ر�׼update�������ص�״ֵ̬
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
     * ����һ����¼.
     * ����ü�¼���ڣ������֮������ü�¼�����ڣ�����롣�����һ��������ֵΪnull||""�����Զ������µ�����ֵ������������ʺ϶Ժ��������ı���в������������Ӱ��Զ������ı���и���
     *
     * @param tableName �Ǳ���
     * @param mapRecord ��׼�����뵽���е�һ����¼������,��������ֶ�����ͬ,˳���޹�;�������������ֶ����������������;
     * @return ���ر�׼update�������ص�״ֵ̬
     */
    public int save(String tableName, Map<String, Object> mapRecord) {//m200//���淽����m2��ͷ
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
     * ����һ����¼.
     * ����ü�¼���ڣ������֮������ü�¼�����ڣ�����롣�����һ��������ֵΪnull||""�����Զ������µ�����ֵ������������ʺ϶Ժ��������ı���в������������Ӱ��Զ������ı���и���
     *
     * @param tableName �Ǳ���
     * @param mapRecord ��׼�����뵽���е�һ����¼������,��������ֶ�����ͬ,˳���޹�;�������������ֶ����������������;
     * @return ���ص�һ������ֵ����ע��ԭ����int��update����ֵ������Ϊ��һ������ֵ����������Ӧ�á�
     */
    public Object saveOne(String tableName, Map<String, Object> mapRecord) {//m200//���淽����m2��ͷ
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
     * ɾ����¼�� ִ��Statement��executeUpdate����
     *
     * @param deleteSql ��һ����׼��SQLɾ�����
     * @return ���ر�׼�������ķ���ֵ
     */
    public int delete(String deleteSql) {//m300//ɾ��������m3��ͷ
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
     * ɾ����¼�� ִ��Statement��executeUpdate����
     *
     * @param deleteSql �Ƕ�����׼��SQLɾ�����
     * @return ���ر�׼�������ķ���ֵ
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
     * ��ѯ����
     *
     * @param sqlquery �Ǳ�׼��ѯ���,���������⸴�ӵĶ���ѯ���,����������JDBC API֧�ֵı�׼��ѯ���
     * @return ����ѯ���ResultSet����ת����List&lt;Map&lt;String,Object&gt;&gt;���͵Ľ��
     */
    public List query(String sqlquery) {//m400//��ѯ������m4��ͷ
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
     * ��ѯ����
     *
     * @param sqlquery �Ǳ�׼��ѯ���,���������⸴�ӵĶ���ѯ���,����������JDBC API֧�ֵı�׼��ѯ���
     * @param timeout �趨��ѯʱ�ޣ���λ���룻timeout=0,���ѯ����ʱ������
     * @return ����ѯ���ResultSet����ת����List&lt;Map&lt;String,Object&gt;&gt;���͵Ľ��
     */
    public List query(String sqlquery, int timeout) {//m400//��ѯ������m4��ͷ
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
     * ͨ��һ���ɹ����������ȡָ����ʼλ�á�ָ�����ȵ��ӽ����,����isScrollSenstive��񣬽�������趨Ϊֻ��.
     *
     * @param sqlquery �Ǳ�׼��ѯ���,���������⸴�ӵĶ���ѯ���,����������JDBC API֧�ֵı�׼��ѯ���
     * @param position ��¼��ʼλ��,ע����м�¼�Ǵ�1��ʼ;Խ���򷵻�0����¼
     * @param length ��ָ����¼����,����������,��position���ȫ����¼
     * @param isScrollSenstive ָ��������Ƿ�����
     * @return
     * ��ȡ��ѯ�����ת����List����,ÿһ����¼ӳ���һ��HashMap����,���HashMap����ļ����Ǳ��е��ֶ����������ֶεı�������ֵΪ�ֶ�ֵ����ֵ���������ֶ�����Ӧ��JDBC
     * API��Java�ࡣ���޼�¼�򷵻��㳤��List����
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
     * �����ϴβ�ѯ����ѯ
     *
     * @param last ��ѯ���ջ�еĵ�����
     */
    public List queryByLast(int last) {//m400//��ѯ������m4��ͷ
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
     * ��ѯ����
     *
     * @param sqlquery �Ǳ�׼��ѯ���,���������⸴�ӵĶ���ѯ���,����������JDBC API֧�ֵı�׼��ѯ���
     * @param fetchSize Ԥȡ��
     * @return ��ȡ��ѯ�����ת����List����
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
     * ��ѯ���е�һ����¼
     *
     * @param tableName ����
     * @return ��ȡ��ĵ�һ����¼
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
     * ��ѯ�������һ����¼
     *
     * @param tableName ����
     * @return ��ȡ������һ����¼
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
     * ��ѯһ����¼
     *
     * @param querySql ��ѯ���
     * @return ��ȡһ����¼��
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
     * ��ѯһ����¼
     *
     * @param tableName �Ǳ���;
     * @param fieldName �ؼ��ֶ���;
     * @param fieldValue �ؼ��ֶ�ֵ.
     * @return ����ָ������,��ȡһ����¼��
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
     * ������ѯһ����¼
     *
     * @param tableName ����
     * @param fields ����ѯ���ֶ�
     * @param indexRow �Ӹ��п�ʼ��ȡֵ[1,��¼�ܳ���]������λ�÷���0����List&lt;Map&gt;���
     * @return ����List&lt;Map&gt;���͵Ľ��
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
     * ������ѯ������¼
     *
     * @param tableName ����
     * @param fields ����ѯ���ֶ�
     * @param indexRow �Ӹ��п�ʼ��ȡֵ[1,��¼�ܳ���]������λ�÷���0����List&lt;Map&gt;���
     * @param length ��ѯ���ȣ�row_in_table���¼������length���򷵻�����ȫ����¼
     * @param asc ����˳��true˳��false����
     * @return ����List&lt;Map&gt;���͵Ľ��
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
     * �����ڵ�����ĳ���
     *
     * @param tableName ����
     * @return ���������ڵ�����ĳ���
     */
    public int getIndexNodesLength(String tableName) {
        return pvo.getIndexNodesLength(tableName);
    }

    /**
     * @return ���������ڵ�Ĳ���
     */
    public int getIndexNodesStepLength() {
        return pvo.getIndexNodesStepLength();
    }

    /**
     * ���������ڵ�Ĳ���
     *
     * @param indexStepLength �µ������ڵ�Ĳ���
     */
    public void setIndexNodesStepLength(int indexStepLength) {
        pvo.setIndexNodesStepLength(indexStepLength);
    }

    /**
     * ����Dbʵ�����Ա����û���ѯ���ݿ⼰����ֶεĽṹ��Ϣ
     *
     * @return ��ǰʹ�õ�Dbʵ��
     */
    public Db getDb() {
        return pvo.getDb();
    }

    /**
     * ��ѯ���м�¼
     *
     * @param tableName �Ǳ���.
     * @return ��ȡ��¼��.
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
     * ��ѯ���м�¼
     *
     * @param tableName �Ǳ���
     * @param where String����where��where�Ӿ䲿��
     * @return ��ȡ��¼��.
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
     * ��ѯ���ݿ�ṹ��Ϣ
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
     * ��ѯָ����Ľṹ��Ϣ
     *
     * @param tableName
     * @return ���ر��������ֶ���Ϣ
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
     * ִ��Statement��execute���� ����ִ�д��������±�Ľṹ��...
     *
     * @param sql ��һ����׼��SQL�������.
     * @return ���ر�׼�������ķ���ֵ
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
     * ִ��Statement��executeUpdate�������磺���¼�¼
     *
     * @param sql ��һ���׼��SQL������䣬�����ǲ��롢ɾ��������.
     * @return ���ر�׼�������ķ���ֵ
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
     * ���á�ִ��һ��sql�ļ�
     *
     * @param sqlFileName һ��sql�ļ�.
     * @return �Ƿ�ִ�гɹ�
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
     * ִ�С����ύ���񡢺�رգ����һ��������������ProcessVO�����������ڡ�
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
     * ȡ�����Ȼع�����رա�
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

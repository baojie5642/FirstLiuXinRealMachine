/**
 * Jdml.java 2012-10-30 ������
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
 * ʵ�����ݿ�DML(CRUD)����<br/>
 *
 * 1��ProcessVO()ʹ��db.xml���õ����ݿ����ӣ�<br/> 2��ProcessVO(Connection
 * con)��ʹ��ָ�������ݿ����ӣ�<br/> 3���������closeCon()�ر����ӣ�<br/>
 * 4�������û���try{}catch(SQLException
 * ex){}finally{}�ṹ�У������񣬵���ProcessVO�е�DML������<br/>
 *
 *
 * ������Լ����<br/>
 * �������ݿ⣬ʹ��insert��insertXXX��update��delete��query��queryOne��save��index����<br/>
 * ���У�index��indexXXX��һ��������صĲ�ѯ<br/>
 *
 *
 * @author ������
 *
 *
 * ======================���ڷ�������========================= ��sql
 * dml�����ķ�����������sql������һ�£� ӦΪ��jdbc�У������ǲ��롢���¡�ɾ�����Լ�DDl�Ĳ�����ʹ��executeUpdate����
 * ���DbFree����������������������Ŀɶ��ԾͲ
 * ʹ��insert��update��delete��query������Ϊǰ׺����������ǿ����Ŀɶ��ԣ�ͬʱDbFree����Щ��ͬ�ķ����У����ӻ�����sql�﷢��֤������Ч�ķ�ֹ�����ݿⷢ����������󣬼������ݿ�ĸ�����������������Ч����߳���Ľ�׳�ԡ�
 *
 */
public interface Jdml extends Serializable {

    int ADD_BEFORE = 0;
    int ADD_AFTER = 1;

    /**
     * �����¼��ִ��executeUpdate
     *
     * @param insertSql
     * @return ����JDBC��׼�������ķ���ֵ
     * @throws SQLException
     */
    int insert(String insertSql) throws SQLException;

    /**
     * ���������¼��ִ��executeUpdate
     *
     * @param insertSql
     * @return ����JDBC��׼�������ķ���ֵ
     * @throws SQLException
     */
    int insert(String[] insertSql) throws SQLException;

    /**
     * ����һ����¼�� �㳤���ַ���������Ϊnull��
     *
     * @param tableName �Ǳ���
     * @param mapRecord ��׼�����뵽���е�һ����¼������,��������ֶ�����ͬ,˳���޹�;�������������ֶ����������������
     * @return ����JDBC��׼�������ķ���ֵ
     * @throws SQLException
     */
    int insert(String tableName, Map<String, Object> mapRecord) throws SQLException;

    /**
     * ����һ����¼�� �㳤���ַ���������Ϊnull��
     *
     * @param tableName �Ǳ���
     * @param mapRecord ��׼�����뵽���е�һ����¼������,��������ֶ�����ͬ,˳���޹�;�������������ֶ����������������
     * @param autoInsertKey ֵΪtrueʱ���Զ���������
     * @return ����JDBC��׼�������ķ���ֵ
     * @throws SQLException
     */
    int insert(String tableName, Map<String, Object> mapRecord, boolean autoInsertKey) throws SQLException;

    /**
     * ���������¼
     */
    int insert(String tableName, List<Map> listRecord) throws SQLException;

    /**
     * ���������¼
     *
     * @param tableName ����
     * @param listRecord ��¼��
     * @param autoInsertKey ֵΪtrueʱ���Զ���������
     */
    int insert(String tableName, List<Map> listRecord, boolean autoInsertKey) throws SQLException;

    /**
     *
     * �����ֵ
     *
     * @return ����һ��ֻ�йؼ���ֵ�ļ�¼,�����ظ�ֵ��
     * @param tableName �Ǳ���,
     * @throws SQLException
     */
    Object insertAutoKey(String tableName) throws SQLException;

    /**
     * ���볤���μ�ֵ
     *
     * @return ����һ��ֻ�йؼ���ֵ�ļ�¼��������ϵͳʱ����Ϊֵ�������ظ�ֵ��
     * @param tableName �Ǳ���,
     * @throws SQLException
     *
     */
    Long insertKey(String tableName) throws SQLException;

    /**
     * ����ָ����ֵ
     *
     * @return ����ָ���ļ�ֵ������ɹ��򷵻ظ�ֵ��������ɹ����򷵻�null��
     * @param tableName �Ǳ���,
     * @param keyValue ��һ��������ֵ
     * @throws SQLException
     *
     */
    String insertKey(String tableName, String keyValue) throws SQLException;

    /**
     * ���¼�¼
     *
     * @param updateSql ��һ����׼��SQL������䣬�����ǲ��롢ɾ��������.
     * @return ���ر�׼�������ķ���ֵ
     * @throws SQLException
     */
    int update(String updateSql) throws SQLException;

    /**
     * ���¼�¼
     *
     * @param updateSql ��һ���׼��SQL������䣬�����ǲ��롢ɾ��������.
     * @return ���ر�׼�������ķ���ֵ
     * @throws SQLException
     */
    int update(String[] updateSql) throws SQLException;

    /**
     * ����һ����¼
     *
     * @param tableName �Ǳ���
     * @param mapRecord ��׼�����뵽���е�һ����¼������,��������ֶ�����ͬ,˳���޹�;�������������ֶ����������������;
     * @return ���ر�׼update�����ķ���ֵ
     * @throws SQLException
     */
    int update(String tableName, Map<String, Object> mapRecord) throws SQLException;

    /**
     * ����һ����¼
     *
     * @param tableName �Ǳ���
     * @param mapRecord ��׼�����뵽���е�һ����¼������,��������ֶ�����ͬ,˳���޹�;�������������ֶ����������������;
     * @param where ��һ����׼��where�Ӿ�.
     * @return ���ر�׼update�����ķ���ֵ
     * @throws SQLException
     */
    int update(String tableName, Map<String, Object> mapRecord, String where) throws SQLException;

    /**
     * �Զ����¶�����¼
     *
     * @param tableName ����
     * @param listRecord �������������ֶεļ�¼��
     */
    int update(String tableName, List<Map> listRecord) throws SQLException;

    /**
     * �Զ����¶�����¼
     *
     * @param tableName ����
     * @param listRecord �������������ֶεļ�¼��
     * @param where ��������
     */
    int update(String tableName, List<Map> listRecord, String where) throws SQLException;

    /**
     * ����һ����¼.
     * ����ü�¼���ڣ������֮������ü�¼�����ڣ�����롣�����һ��������ֵΪnull||""�����Զ������µ�����ֵ������������ʺ϶Ժ��������ı���в������������Ӱ��Զ������ı���и���
     *
     * @param tableName �Ǳ���
     * @param mapRecord ��׼�����뵽���е�һ����¼������,��������ֶ�����ͬ,˳���޹�;�������������ֶ����������������;
     * @return ���ر�׼update�������ص�״ֵ̬
     * @throws SQLException
     */
    int save(String tableName, Map<String, Object> mapRecord) throws SQLException;

    /**
     * ����һ����¼.
     * ����ü�¼���ڣ������֮������ü�¼�����ڣ�����롣�����һ��������ֵΪnull||""�����Զ������µ�����ֵ������������ʺ϶Ժ��������ı���в������������Ӱ��Զ������ı���и���
     *
     * @param tableName �Ǳ���
     * @param mapRecord ��׼�����뵽���е�һ����¼������,��������ֶ�����ͬ,˳���޹�;�������������ֶ����������������;
     * @return ���ص�һ������ֵ����ע��ԭ����int��update����ֵ������Ϊ��һ������ֵ����������Ӧ�á�
     * @throws SQLException
     */
    Object saveOne(String tableName, Map<String, Object> mapRecord) throws SQLException;

    /**
     * ɾ����¼
     *
     * @param deleteSql ��һ����׼��SQL������䣬�����ǲ��롢ɾ��������.
     * @return ���ر�׼�������ķ���ֵ
     * @throws SQLException
     */
    int delete(String deleteSql) throws SQLException;

    /**
     * ����ɾ��
     *
     * @param deleteSql ��һ����׼��SQL������䣬�����ǲ��롢ɾ��������.
     * @return ���ر�׼�������ķ���ֵ
     * @throws SQLException
     */
    int delete(String[] deleteSql) throws SQLException;

    /**
     * ��ѯ����
     *
     * @param sqlquery �Ǳ�׼��ѯ���,���������⸴�ӵĶ���ѯ���,����������JDBC API֧�ֵı�׼��ѯ���
     * @return ����ѯ���ResultSet����ת����List&lt;Map&lt;String,Object&gt;&gt;���͵Ľ��
     */
    List query(String sqlquery) throws SQLException;

    /**
     * ��ѯ�����������ϴβ�ѯ����ѯ
     *
     * @return ����ѯ���ResultSet����ת����List&lt;Map&lt;String,Object&gt;&gt;���͵Ľ��
     */
    List queryByLast(int last) throws SQLException;

    /**
     * ��ѯ��¼��
     *
     * @param tableName �Ǳ���.
     * @return ��ȡ��¼��.
     * @throws SQLException
     */
    long queryCount(String tableName) throws SQLException;

    /**
     * ��ѯ��¼��
     *
     * @param tableName �Ǳ���
     * @param where String����where��where�Ӿ䲿��
     * @return ��ȡ��¼��.
     * @throws SQLException
     */
    long queryCount(String tableName, String where) throws SQLException;

    /**
     * ��ѯ
     *
     * @param sqlquery �Ǳ�׼��ѯ���,���������⸴�ӵĶ���ѯ���,����������JDBC API֧�ֵı�׼��ѯ���
     * @param fetchSize Ԥȡ��
     * @return ��ȡ��ѯ�����ת����List����
     */
    List queryFetchSize(String sqlquery, int fetchSize) throws SQLException;

    /**
     * ��ѯ��һ����¼
     *
     * @param tableName ����
     * @return ��ȡ������һ����¼
     * @throws SQLException
     */
    Map queryFirstRecord(String tableName) throws SQLException;

    /**
     * ��ѯ���һ����¼
     *
     * @param tableName ����
     * @return ��ȡ������һ����¼
     * @throws SQLException
     */
    Map queryLastRecord(String tableName) throws SQLException;

    /**
     * ��ѯһ����¼
     *
     * @param sqlQuery ��ѯ���
     * @return ��ȡһ����¼��
     * @throws SQLException
     */
    Map queryOne(String sqlQuery) throws SQLException;

    /**
     * ��ѯһ����¼
     *
     * @param tableName �Ǳ���;
     * @param fieldName �ֶ���;
     * @param fieldValue �ֶ�ֵ.
     * @return ����ָ������,��ȡһ����¼,�˷�����Ҫ���ڱ�ǩ��
     * @throws SQLException
     */
    Map queryOne(String tableName, String fieldName, Object fieldValue) throws SQLException;

    /**
     * ��ѯ�����
     *
     * @param querySql �Ǳ�׼��ѯ���,���������⸴�ӵĶ���ѯ���,����������JDBC API֧�ֵı�׼��ѯ���
     * @return ��ȡ��ѯ���ResultSet����
     */
    ResultSet queryResultSet(String querySql) throws SQLException;

    /**
     * ��ѯ�����
     *
     * @param querySql �Ǳ�׼��ѯ���,���������⸴�ӵĶ���ѯ���,����������JDBC API֧�ֵı�׼��ѯ���
     * @param statement
     * �û������Statement��������ʵ�ָ��߼��Ĳ�ѯ����ɸ��µġ��ɹ����ġ�ֻ���ġ����еġ������еģ�����ͨ��Statement
     * statement=getCon().createStatement(ResultSet.����0��ResultSet.����1);�ķ�ʽʵ��
     * @return ��ȡ��ѯ���ResultSet����
     */
    ResultSet queryResultSet(String querySql, Statement statement) throws SQLException;

    /**
     * ��ѯ���ݿ�Ľṹ��Ϣ
     *
     * @return �������������͡����ݿ���������������б��������б�������ֶ��������ֶεĸ�����Ϣ
     */
    String queryDbInfo() throws SQLException;

    /**
     * ����ָ��������ѯ��ĵĽṹ��Ϣ
     *
     * @param tableName ָ������
     * @return ���ر����������ֶε������Ϣ
     */
    String queryTableInfo(String tableName) throws SQLException;

    /**
     * ִ��Statement��execute���� ����ִ�д��������±�Ľṹ��...
     *
     * @param updateSql ��һ����׼��SQL�������.
     * @return ���ر�׼�������ķ���ֵ
     * @throws SQLException
     */
    boolean execute(String updateSql) throws SQLException;

    /**
     * ִ��Statement��executeUpdate�������磺���¼�¼
     *
     * @param updateSql ��һ���׼��SQL������䣬�����ǲ��롢ɾ��������.
     * @return ���ر�׼�������ķ���ֵ
     * @throws SQLException
     */
    boolean execute(String[] updateSql) throws SQLException;

    /**
     * ���ò�ִ��sql�ļ�
     *
     * @param sqlFileName ��һ���׼��SQL������䣬�����ǲ��롢ɾ��������.
     * @return �����Ƿ�ִ�гɹ�
     * @throws SQLException
     */
    public boolean executeSqlFile(String sqlFileName) throws SQLException;

    /**
     * ��ȡ����
     *
     * @return ����Connection con����
     */
    Connection getCon();

    /**
     * �Ƿ�ر�����
     *
     * @return ����Connection con����
     */
    boolean isClosed() throws SQLException;

    /**
     * �ر�����
     */
    void closeCon() throws SQLException;

    /**
     * @return ����Dbʵ��
     */
    Db getDb();
}

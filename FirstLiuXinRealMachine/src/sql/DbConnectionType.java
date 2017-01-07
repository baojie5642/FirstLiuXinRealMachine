/*
 * @(#)DbConnectionType.java
 */
package sql;

/**
 * ���ݿ��������ͣ� ProcessVO��Db��DbFree��DbConfigManager����
 *
 * @author ������ 2012-10-24
 */
public interface DbConnectionType {

    /**
     * ʹ��PVO�����ļ�����ȡ����;�����ļ�/META-INF/db.xml
     */
    public static final int USING_CONFIG_OF_DEFAULT = 1;
    /**
     * //ʹ��JPA�����ļ�����ȡ����;�����ļ�/META-INF/persistence.xml
     */
    public static final int USING_CONFIG_OF_JPA = 2;
    /**
     * ʹ��HIBERNATE�����ļ�����ȡ���ӣ������ļ�ʹ��/META-INF/hibernate.xml
     */
    public static final int USING_CONFIG_OF_HIBERNATE = 3;
    /**
     * ʹ��iBatis/MYBATIS�����ļ�����ȡ���ӣ������ļ�ʹ��/META-INF/ibatis.xml����,
     */
    public static final int USING_CONFIG_OF_MYBATIS = 4;
    /**
     * ʹ���û����������ݿ����Ӷ����������ļ���<br/>��pvo/dbfree�У�ͨ��ProcessVO(Connection
     * con)||DbFree(Connection con)�ȹ��췽�����ݸ�Ӧ��
     */
    public static final int USING_CONFIG_OF_NONE = 0;
    public static final int USING_DB_01 = 101;//ʹ��db_01����ȡ����
    public static final int USING_DB_02 = 102;//ʹ��db_02����ȡ����
}

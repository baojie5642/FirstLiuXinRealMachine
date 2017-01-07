/*
 * @(#)DbConnectionType.java
 */
package sql;

/**
 * 数据库连接类型； ProcessVO，Db、DbFree、DbConfigManager共享
 *
 * @author 胡开明 2012-10-24
 */
public interface DbConnectionType {

    /**
     * 使用PVO配置文件，获取连接;配置文件/META-INF/db.xml
     */
    public static final int USING_CONFIG_OF_DEFAULT = 1;
    /**
     * //使用JPA配置文件，获取连接;配置文件/META-INF/persistence.xml
     */
    public static final int USING_CONFIG_OF_JPA = 2;
    /**
     * 使用HIBERNATE配置文件，获取连接；配置文件使用/META-INF/hibernate.xml
     */
    public static final int USING_CONFIG_OF_HIBERNATE = 3;
    /**
     * 使用iBatis/MYBATIS配置文件，获取连接；配置文件使用/META-INF/ibatis.xml配置,
     */
    public static final int USING_CONFIG_OF_MYBATIS = 4;
    /**
     * 使用用户给定的数据库连接对象，无配置文件；<br/>在pvo/dbfree中，通过ProcessVO(Connection
     * con)||DbFree(Connection con)等构造方法传递给应用
     */
    public static final int USING_CONFIG_OF_NONE = 0;
    public static final int USING_DB_01 = 101;//使用db_01，获取连接
    public static final int USING_DB_02 = 102;//使用db_02，获取连接
}

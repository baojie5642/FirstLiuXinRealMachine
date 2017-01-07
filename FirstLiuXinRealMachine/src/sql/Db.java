/*
 * Db.java
 */
package sql;

import java.io.Serializable;
import java.sql.Connection;
import java.util.Map;

/*
 * Db实现
 * 采用有限多列模式
 * 
 * Db职责
 * 1、提取数据库结构信息；
 * 2、负责键值生成，对生成值进行唯一性验证；
 * 3、数据库的表、字段等结构发生变化后，应当重建Db实例。
 * 4、负责管理Table、Field，维护重要的用户操作信息
 *    a 维护表的索引信息
 *    b 保存上一次生成的键值信息，实现键值的唯一性验证
 *    c 保存上一次插入的记录信息
 *    d 保存上一次更新的记录信息
 *    e ProcessVO根据c或d拒绝插入、更新相同的记录
 * 5、
 *
 * @author hkm
 */
public interface Db extends Serializable{

    Connection getCon();

    String getDriverName();

    String getSchema();

    String getCatalog();

    Field getField(String tableName, String fieldName);

    String getFieldDecimal(String tableName, String fieldName);

    String getFieldDefaultValue(String tableName, String fieldName);

    String getFieldPosition(String tableName, String fieldName);

    Map<String, Field> getFieldMap(String tableName, String fieldName);

    String getFieldRegex(String tableName, String fieldName);

    String getFieldRemark(String tableName, String fieldName);

    String getFieldSize(String tableName, String fieldName);

    String getFieldBufferLength(String tableName, String fieldName);

    int getFieldSqlType(String tableName, String fieldName);

    String getFieldTypeName(String tableName, String fieldName);

    String getFieldTypeClassName(String tableName, String fieldName);

    String[] getFields(String tableName);

    String[] getKeys(String tableName);

    /**
     * 主键类型
     */
    String[] getKeysType(String tableName);

    Table getTable(String tableName);

    Map<String, Table> getTableMap();

    String[] getTableNames();

    boolean isExistField(String tableName, String fieldName);

    boolean isExistKeyField(String tableName, String fieldName);

    boolean isExistTable(String tableName);

    boolean isFieldNullable(String tableName, String fieldName);

    boolean isFieldPrimarykey(String tableName, String fieldName);

    String getLastQuerySql(int last);

    void setLastQuerySql(String lastQuerySql) ;
    
    
    /*
     * 增加创建表、删除表、修改表和字段的方法。在执行这些方法后，应当调用Db实例的内置方法重建Db实例
     */
    //void reBuild(int connectionType);//重建单例模式实例，在不重新打开应用程序的情况下，是否给应用程序留下的依然是原原单例模式的实例？如果这样，重建就没有意义。
}

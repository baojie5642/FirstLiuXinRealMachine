/*
 * Db.java
 */
package sql;

import java.io.Serializable;
import java.sql.Connection;
import java.util.Map;

/*
 * Dbʵ��
 * �������޶���ģʽ
 * 
 * Dbְ��
 * 1����ȡ���ݿ�ṹ��Ϣ��
 * 2�������ֵ���ɣ�������ֵ����Ψһ����֤��
 * 3�����ݿ�ı��ֶεȽṹ�����仯��Ӧ���ؽ�Dbʵ����
 * 4���������Table��Field��ά����Ҫ���û�������Ϣ
 *    a ά�����������Ϣ
 *    b ������һ�����ɵļ�ֵ��Ϣ��ʵ�ּ�ֵ��Ψһ����֤
 *    c ������һ�β���ļ�¼��Ϣ
 *    d ������һ�θ��µļ�¼��Ϣ
 *    e ProcessVO����c��d�ܾ����롢������ͬ�ļ�¼
 * 5��
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
     * ��������
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
     * ���Ӵ�����ɾ�����޸ı���ֶεķ�������ִ����Щ������Ӧ������Dbʵ�������÷����ؽ�Dbʵ��
     */
    //void reBuild(int connectionType);//�ؽ�����ģʽʵ�����ڲ����´�Ӧ�ó��������£��Ƿ��Ӧ�ó������µ���Ȼ��ԭԭ����ģʽ��ʵ��������������ؽ���û�����塣
}

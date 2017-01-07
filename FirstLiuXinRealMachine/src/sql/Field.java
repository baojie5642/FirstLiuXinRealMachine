/*
 * 2009-9-19 ����
 * ���ݿ��ֶ�������
 * ���ڶ�HashMap��ϵ����ӳ�似���Ľ�һ������
 * ������
 */
package sql;

/**
 * @author hkm
 */
public class Field {
    

    private String name = "";//�ֶ���
    private String typeName = "";//�ֶ�����(�磺bigint��char)
    private int sqlType = -1;//�ֶε�java.sql.Types��ֵjava.sql.Types.BIGINT
    private String typeClassName = "";//�ֶ����Ͷ�Ӧ��Java�������
    private String position = "";//�ֶ�������,��DatabaseMetaData�о���String��ʾ
    private String size = "";//�ֶγ���,��DatabaseMetaData�о���String��ʾ//��ȡCOLUMN_SIZE
    private String decimal = "";//�ֶξ���
    private String defaultValue = "";//�ֶ�Ĭ��ֵ
    private String remark = "";//����,�������ݿ�֧��
    private String format = "";//��ֵ����ϵͳ�Զ�����,���ô��ֶθ�ʽ����������ʽ������remark
    private String regex = "";//���û���д������ʽ
    private String errmsg = "";//¼���ֶδ�����Ϣ
    private String commend = "";//˵��,ע��
    private String bufferLength="";//BUFFER_LENGTH
    private boolean nullable = false;//�Ƿ�����Ϊ��ֵ��true����false������
    private boolean primarykey = false;//�Ƿ�������
    private boolean foreignkey = false;//�Ƿ��������
    
    public Field(){}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String type) {
        this.typeName = type;
    }

    public int getSqlType() {
        return sqlType;
    }

    public void setSqlType(int sqlType) {
        this.sqlType = sqlType;
    }
    
    

    public String getTypeClassName() {
        return typeClassName;
    }

    public void setTypeClassName(String typeClassName) {
        this.typeClassName = typeClassName;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

   

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getDecimal() {
        return decimal;
    }

    public void setDecimal(String decimal) {
        this.decimal = decimal;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getRegex() {
        return regex;
    }

    public void setRegex(String regex) {
        this.regex = regex;
    }

    public String getErrmsg() {
        return errmsg;
    }

    public void setErrmsg(String errmsg) {
        this.errmsg = errmsg;
    }

    public String getCommend() {
        return commend;
    }

    public void setCommend(String commend) {
        this.commend = commend;
    }

    public boolean isNullable() {
        return nullable;
    }

    public void setNullable(boolean nullable) {
        this.nullable = nullable;
    }

    public boolean isPrimarykey() {
        return primarykey;
    }

    public void setPrimarykey(boolean primarykey) {
        this.primarykey = primarykey;
    }


    public boolean isForeignkey() {
        return foreignkey;
    }

    public void setForeignkey(boolean foreignkey) {
        this.foreignkey = foreignkey;
    }

    public String getBufferLength() {
        return bufferLength;
    }

    public void setBufferLength(String bufferLength) {
        this.bufferLength = bufferLength;
    }
    
    

}

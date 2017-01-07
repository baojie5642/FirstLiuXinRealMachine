/*
 * 2009-9-19 ����
 * ���ݿ���������
 * ���ڶ�HashMap��ϵ����ӳ�似���Ľ�һ������
 * Ϊÿ��������ֵ������
 */
package sql;

import java.sql.SQLException;
import java.util.*;

/**
 * �汾��ţ�pvo4.0-SNAPSHOT<br/> �޶����ڣ�2012-10-19<br/> �޶���Ŀ��
 * 1��ȡ����ֵ��������ͨ���ⲻ�ǰ�ȫ�������������Ҫ�ָ�������Բο�pvo3.0Դ�����Db��Table�ࣻ��ȫ������������ProcessOV����ط�����ʵ�ֱȽϷ��㣻
 */
public class Table {

    private String tableName = "";
    private Map<String, Field> fieldMap = new LinkedHashMap();//����jdk����ʱ private Map fieldMap=new HashMap();//�ֶ�ӳ���
    private String[] keys = new String[0];//����������
    private String[] fields = new String[0];//�ֶ�������
    private IndexNode[] indexNodes = new IndexNode[0];//ʹ��Ĭ�ϲ���1000��ÿ����1000����¼����һ���ڵ㣬���һ���ڵ㱣�����һ����¼��Ϣ��ÿ���ڵ��¼��ĵ�һ������ֵ�Լ����ü�˳���ѯ���������<br/>
    private int position_of_indexNodes = -1;//��ѯλ�ü�¼�������Ѳ�ѯ��indexNodes�����λ����Ϣ
    private int indexStepLength = -1;//�ϴδ�������ʹ�õĲ���������б仯����Ӧ���ؽ���������
    private long lastMakeLongKeyValue;//��ֵ�Ƚ�
    private Object lastMakeObjectKeyValue;//��ֵ�Ƚ�
    private String lastStringKeyValue;//��ֵ�Ƚ�

    public Table() {
    }

    public void setName(String tableName) {
        this.tableName = tableName;
    }

    public String getName() {
        return tableName;
    }

    public String[] getKeys() {
        return keys;
    }

    public void setKeys(String[] s) {
        this.keys = s;
    }

    public String[] getFields() {
        return fields;
    }

    public void setFields(String[] s) {
        this.fields = s;
    }

    public Map<String, Field> getFieldMap() {
        return fieldMap;
    }

    public void setFieldMap(Map<String, Field> map) {
        this.fieldMap = map;
    }

    public IndexNode[] getIndexNodes() {
        return indexNodes;
    }

    public void setIndexNodes(IndexNode[] indexNodes) {
        this.indexNodes = indexNodes;
    }

    public int getPosition_of_indexNodes() {
        return position_of_indexNodes;
    }

    public void setPosition_of_indexNodes(int position_of_indexNodes) {
        this.position_of_indexNodes = position_of_indexNodes;
    }

    public int getIndexStepLength() {
        return indexStepLength;
    }

    public void setIndexStepLength(int indexStepLength) {
        this.indexStepLength = indexStepLength;
    }

    /**
     * Make long KeyValue
     *
     * @return System.currentTimeMillis*10000+Num, the Num is between -9999 and
     * 9999;for example :2012-11-01 HH:mm:ss as
     * 1351749144390��getting��13517491443897256
     */
    synchronized public long makeLongKeyValue_Backup() {
        long len = 10000;
        Random r = new Random();
        double f = r.nextGaussian() % 1.0;//ȡС������������//(1 + r.nextGaussian() % 1.0) % 1.0;//ȡС������������
        long t = System.currentTimeMillis();
        long k = t * len + (long) (f * len);
        return onlyValue_Backup(k);//only value//�ݹ飬ֱ������
    }

    private long onlyValue_Backup(long keyValue) {
        long v;
        if (keyValue != lastMakeLongKeyValue) {
            lastMakeLongKeyValue = keyValue;
            v = keyValue;
        } else {
            v = onlyValue_Backup(makeLongKeyValue_Backup());
        }
        return v;
    }

    /**
     * Make long KeyValue
     *
     * @return System.currentTimeMillis*10000+Num, the Num is between 0 and
     * 9999;for example :2012-11-01 HH:mm:ss as
     * 1351749144390��getting��13517491443897256
     */
    synchronized public long makeLongKeyValue() {
        long t = System.currentTimeMillis();
        return onlyLongValue(t, 0);//only value//�ݹ飬ֱ������
    }

    private long onlyLongValue(long time, long add) {
        long v;
        long len = 10000;
        long k = time * len + add;
        if (k > lastMakeLongKeyValue) {
            lastMakeLongKeyValue = k;
            v = k;
        } else {
            add++;
            if (add > 9999) {
                v = onlyLongValue(time + 1, 0);
            } else {
                v = onlyLongValue(time, add);
            }
        }
        return v;
    }

    /**
     * @param field
     * @param fieldValue �����ݿ��в�ѯ�����ֵ ���ɸ������͵ĵ�һ������ֵ
     */
    synchronized public Object makeObjectKeyValue(Field field, Object fieldValue) throws SQLException {
        String className = field.getTypeClassName();
        Object nextValue = null;
        if (fieldValue != null && !"".equals(fieldValue.toString())) {
            if (className.equals("java.lang.Long")) {
                if (((Long) fieldValue).longValue() == Long.MAX_VALUE) {
                    throw new SQLException("Long value must less than Long.MAX_VALUE " + Long.MAX_VALUE);
                }
                nextValue = new Long(((Long) fieldValue).longValue() + 1);
            }
            if (className.equals("java.lang.Integer")) {
                if (((Integer) fieldValue).intValue() == Integer.MAX_VALUE) {
                    throw new SQLException("Integer value must less than Integer.MAX_VALUE " + Integer.MAX_VALUE);
                }
                nextValue = new Integer(((Integer) fieldValue).intValue() + 1);
            }
            if (className.equals("java.lang.Short")) {
                if (((Short) fieldValue).shortValue() == Short.MAX_VALUE) {
                    throw new SQLException("Short value must less than Short.MAX_VALUE " + Short.MAX_VALUE);
                }
                short v = ((Short) fieldValue).shortValue();
                v++;
                nextValue = new Short(v);
            }
            if (className.equals("java.lang.Float")) {
                if (((Float) fieldValue).floatValue() == Float.MAX_VALUE) {
                    throw new SQLException("Float value must less than Float.MAX_VALUE " + Float.MAX_VALUE);
                }
                Float v = new Float(((Float) fieldValue).floatValue() + 1);
                long l = (long) v.floatValue();
                nextValue = new Float(l);

            }
            if (className.equals("java.lang.Double")) {
                if (((Double) fieldValue).doubleValue() == Double.MAX_VALUE) {
                    throw new SQLException("Double value must less than Double.MAX_VALUE " + Double.MAX_VALUE);
                }
                Double v = new Double(((Double) fieldValue).doubleValue() + 1);
                long l = (long) v.floatValue();
                nextValue = new Double(l);
            }
        } else {
            if (className.equals("java.lang.Long")) {
                nextValue = new Long(0);
            }
            if (className.equals("java.lang.Integer")) {
                nextValue = new Integer(0);
            }
            if (className.equals("java.lang.Short")) {
                short v = 0;
                nextValue = new Short(v);
            }
            if (className.equals("java.lang.Float")) {
                nextValue = new Float(0);
            }
            if (className.equals("java.lang.Double")) {
                nextValue = new Double(0);
            }
        }
        return onlyObjectValue(field, nextValue);//Ψһ����֤
    }

    private Object onlyObjectValue(Field field, Object keyValue) throws SQLException {
        Object v;
        if (!keyValue.equals(lastMakeObjectKeyValue)) {
            lastMakeObjectKeyValue = keyValue;
            v = keyValue;
        } else {
            v = onlyObjectValue(field, makeObjectKeyValue(field, keyValue));//�ݹ飬ֱ������
        }
        return v;
    }

    /**
     * Make String KeyValue ����û�������ֶ��Ƿ���ڡ���ֵ�Ƿ�Ϸ����Ƿ�Ψһ
     *
     * @param keyValue ������ֵ
     * @return �ֶδ��ڣ��ҺϷ�Ψһ���򷵻�keyValue�������򣬷���null
     */
    synchronized public String makeStringKeyValue(String keyField, String keyValue) throws SQLException {
        JadeTool tool = new JadeTool();
        if (keyValue == null) {
            throw new SQLException("the key field value must not be null. ");
        }
        String v = null;
        if (tool.isInFields(fields, keyField) && !keyValue.equals(lastStringKeyValue)) {
            lastStringKeyValue = keyValue;
            v = keyValue;
        }
        if (v == null) {
            throw new SQLException("the key field value already being existence. ");
        }
        return v;//���ܷ���nullֵ������nullʱ���û�Ӧ������������ֵ
    }
}

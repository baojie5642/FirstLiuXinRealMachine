/*
 * 2009-9-19 南陵
 * 数据库表的描述类
 * 用于对HashMap关系数据映射技术的进一步完善
 * 为每个表创建键值生成器
 */
package sql;

import java.sql.SQLException;
import java.util.*;

/**
 * 版本序号：pvo4.0-SNAPSHOT<br/> 修订日期：2012-10-19<br/> 修订项目：
 * 1、取消键值生成器，通常这不是安全的做法，如果需要恢复此项，可以参考pvo3.0源代码的Db、Table类；安全地生成主键在ProcessOV的相关方法中实现比较方便；
 */
public class Table {

    private String tableName = "";
    private Map<String, Field> fieldMap = new LinkedHashMap();//向下jdk兼容时 private Map fieldMap=new HashMap();//字段映射表
    private String[] keys = new String[0];//主键名数组
    private String[] fields = new String[0];//字段名数组
    private IndexNode[] indexNodes = new IndexNode[0];//使用默认步长1000，每增加1000条记录创建一个节点，最后一个节点保存最后一条记录信息，每个节点记录表的第一个主键值以及按该键顺序查询结果的行数<br/>
    private int position_of_indexNodes = -1;//查询位置记录，保存已查询的indexNodes数组的位置信息
    private int indexStepLength = -1;//上次创建索引使用的步长，如果有变化，则应该重建索引数组
    private long lastMakeLongKeyValue;//键值比较
    private Object lastMakeObjectKeyValue;//键值比较
    private String lastStringKeyValue;//键值比较

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
     * 1351749144390，getting：13517491443897256
     */
    synchronized public long makeLongKeyValue_Backup() {
        long len = 10000;
        Random r = new Random();
        double f = r.nextGaussian() % 1.0;//取小数，含正负数//(1 + r.nextGaussian() % 1.0) % 1.0;//取小数，不含负数
        long t = System.currentTimeMillis();
        long k = t * len + (long) (f * len);
        return onlyValue_Backup(k);//only value//递归，直到不等
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
     * 1351749144390，getting：13517491443897256
     */
    synchronized public long makeLongKeyValue() {
        long t = System.currentTimeMillis();
        return onlyLongValue(t, 0);//only value//递归，直到不等
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
     * @param fieldValue 从数据库中查询的最大值 生成各种类型的第一个主键值
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
        return onlyObjectValue(field, nextValue);//唯一性验证
    }

    private Object onlyObjectValue(Field field, Object keyValue) throws SQLException {
        Object v;
        if (!keyValue.equals(lastMakeObjectKeyValue)) {
            lastMakeObjectKeyValue = keyValue;
            v = keyValue;
        } else {
            v = onlyObjectValue(field, makeObjectKeyValue(field, keyValue));//递归，直到不等
        }
        return v;
    }

    /**
     * Make String KeyValue 检查用户输入的字段是否存在、其值是否合法和是否唯一
     *
     * @param keyValue 给定键值
     * @return 字段存在，且合法唯一，则返回keyValue本身；否则，返回null
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
        return v;//可能返回null值，返回null时，用户应当重新输入新值
    }
}

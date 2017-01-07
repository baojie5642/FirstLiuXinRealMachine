package sql;

/**
 * ������.2006��4��29���޶�,������2005��6�����. һ����Ҫ��SQL��׼����е�where�Ӿ����ɹ����ࡣ ʹ��Class���ߣ�������JDBC
 * API�ֶ�ӳ�����͵Ķ�����Ϊ����������SQL��׼����е�where�Ӿ�
 *
 */
class WhereString {

    private String where = "where";
    private String preoperator = "";
    private String field = "";
    private String operator = "";
    private QueryString subquery = null;
    private Object[] values = null;
    private Object value = null;
    private JadeTool tool = new JadeTool();
    private DbCenter db = null;

    /**
     * Ĭ�Ϲ��캯����
     * ����ͨ��setField(field)��setOperator(operator)��setValue(value)���������趨where�Ӿ��еĸ�����ɲ���.
     *
     */
    public WhereString(DbCenter db) {
        this.db = db;
    }

    /**
     * ���캯����
     * ����ͨ��setField(field)��setOperator(operator)��setValue(value)���������趨where�Ӿ��еĸ�����ɲ���.
     * field���ֶ�����operator�ǲ��������磺=��is��like�ȣ�value�Ƿ���JDBC APIӳ����ֶ����͵�ֵ����
     *
     */
    public WhereString(String field, String operator, Object value) {
        this.field = field;
        this.operator = operator;
        this.value = value;
        this.values = null;
        this.subquery = null;
    }

    /**
     * ���캯����
     * ����ͨ��setField(field)��setOperator(operator)��setValue(value)���������趨where�Ӿ��еĸ�����ɲ���.
     * field���ֶ�����operator�ǲ��������磺=��is��like�ȣ�values�Ƿ���JDBC APIӳ����ֶ����͵�ֵ��������
     *
     */
    public WhereString(String field, String operator, Object[] values) {
        this.field = field;
        this.operator = operator;
        this.values = values;
        this.value = null;
        this.subquery = null;
    }

    /**
     * ���캯����
     * ����ͨ��setField(field)��setOperator(operator)��setValue(value)���������趨where�Ӿ��еĸ�����ɲ���.
     * field���ֶ�����operator�ǲ��������磺=��is��like�ȣ�subquery������һ���Ӳ�ѯ
     *
     */
    public WhereString(String field, String operator, QueryString subquery) {
        this.field = field;
        this.operator = operator;
        this.subquery = subquery;
        this.values = null;
        this.value = null;
    }

    /**
     * ���ķ���������һ��������SQL��׼����е�where�Ӿ䡣
     *
     */
    private String getWhereSql() {
        String v = "";
        if (this.getSubquery() != null) {
            v = "(" + this.getSubquery().toString() + ")";
        }
        if (this.getValues() != null) {
            if (this.getValues().length > 1) {
                v = "(" + this.objValuesToString(this.getValues()) + ")";
            } else {
                v = this.objValuesToString(this.getValues());
            }
        }
        if (this.getValue() != null) {
            v = this.objValueToString(this.getValue());
        }
        String pre = "";
        if ("".equals(this.getPreoperator())) {
            pre = this.getWhere();
        } else {
            pre = this.getPreoperator();
        }
        String where = "";
        if (pre.equals("") || this.getOperator().equals("") || v.equals("") || this.getField().equals("")) {
            return where;
        }
        where = " " + pre + " (" + this.getField() + " " + this.getOperator() + " " + v + ") ";
        return where;
    }

    /**
     * ����ͬ�ķ���JDBC APIӳ����ֶ����͵�ֵ����ת��Ϊ�ַ�����ʹ���ΪSQL�б�׼where�Ӿ��ֵ��
     *
     */
    private String objValuesToString(Object[] obj) {
        String v = "null";
        if (obj == null) {
            return v;    //�������Ϊ��,�����޵����ŵ�null�ַ���
        }
        String clsName = obj[0].getClass().getName();
        int length = 0;
        for (int i = 0; i < obj.length; i++) {
            if (obj[i] != null) {
                length++; //�ǿ�ֵ����
            }
        }
        if (length == 0) {
            return v;  //�������������Ԫ�ؾ�Ϊ��ʱ,�����޵����ŵ�null�ַ���
        } else {
            if ((!clsName.equals("java.lang.String"))
                    && (!clsName.equals("java.lang.Integer"))
                    && (!clsName.equals("java.lang.Float"))
                    && (!clsName.equals("java.lang.Double"))
                    && (!clsName.equals("java.lang.Boolean"))
                    && (!clsName.equals("java.lang.Short"))
                    && (!clsName.equals("java.lang.Long"))
                    && (!clsName.equals("java.sql.Date"))
                    && (!clsName.equals("java.sql.Time"))
                    && (!clsName.equals("java.sql.Timestamp"))) {
                return v;     //��������Ϲ涨���ͷ����޵����ŵ�null�ַ���
            }

            String[] _obj = new String[length];
            int j = 0;
            for (int i = 0; i < obj.length; i++) {
                if (obj[i] != null) {
                    _obj[j] = obj[i].toString(); //���˿�ֵ
                    j++;
                }
            }

            if (clsName.equals("java.lang.Integer") || clsName.equals("java.lang.Float") || clsName.equals("java.lang.Long") || clsName.equals("java.lang.Double") || clsName.equals("java.lang.Short")) {
                //�����;����ӵ�����
                v = tool.arryToString(_obj, ",");
                return v;
            }

            if (clsName.equals("java.lang.Boolean")) {
                //��֪�Ƿ���ϵ�����,����æ
                v = tool.arryToString(_obj, ",");
                return v;
            }

            if (clsName.equals("java.lang.String") || clsName.equals("java.sql.Date") || clsName.equals("java.sql.Time") || clsName.equals("java.sql.Timestamp")) {
                //�ַ����;����ϵ�����
                _obj = tool.arryToArray(_obj, "'", "'");
                v = tool.arryToString(_obj, ",");
                return v;
            }

        }
        return v;
    }

    /**
     * ���ķ���������ͬ�ķ���JDBC APIӳ����ֶ����͵�ֵ����ת��Ϊ�ַ�����ʹ���ΪSQL�б�׼where�Ӿ��ֵ��
     *
     */
    private String objValueToString(Object objValue) {
        String v = "null";
        if (objValue == null) {
            return v;      //�������Ϊ��,�����޵����ŵ�null�ַ���
        }
        String clsName = objValue.getClass().getName();

        if ((!clsName.equals("java.lang.String"))
                && (!clsName.equals("java.lang.Integer"))
                && (!clsName.equals("java.lang.Float"))
                && (!clsName.equals("java.lang.Double"))
                && (!clsName.equals("java.lang.Boolean"))
                && (!clsName.equals("java.lang.Short"))
                && (!clsName.equals("java.lang.Long"))
                && (!clsName.equals("java.sql.Date"))
                && (!clsName.equals("java.sql.Time"))
                && (!clsName.equals("java.sql.Timestamp"))) {
            return v;//��������Ϲ涨���ͷ����޵����ŵ�null�ַ���
        }
        if (clsName.equals("java.lang.Integer") || clsName.equals("java.lang.Float") || clsName.equals("java.lang.Long") || clsName.equals("java.lang.Double") || clsName.equals("java.lang.Short")) {
            return objValue.toString();//�����;�������ϵ�����
        }
        if (clsName.equals("java.lang.Boolean")) {
            return objValue.toString();//��֪�Ƿ���ϵ�����,����æ
        }
        v="'" + objValue.toString() + "'";//�ַ����;����ϵ�����
        return v;
    }

    public String getField() {
        return field;
    }

    public String getPreoperator() {
        return preoperator;
    }

    public Object getValue() {
        return value;
    }

    public String getWhere() {
        return where;
    }

    public String getOperator() {
        return operator;
    }

    public QueryString getSubquery() {
        return subquery;
    }

    public Object[] getValues() {
        return values;
    }

    public void setField(String field) {
        this.field = field;
    }

    public void setPreoperator(String preoperator) {
        this.preoperator = preoperator;
    }

    public void setValue(Object value) {
        this.value = value;
        this.values = null;
        this.subquery = null;
    }

    public void setWhere(String where) {
        this.where = where;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public void setSubquery(QueryString subquery) {
        this.subquery = subquery;
        this.values = null;
        this.value = null;
    }

    public void setValues(Object[] values) {
        this.values = values;
        this.value = null;
        this.subquery = null;
    }

    /**
     * ����toString()������������������where�Ӿ�.
     *
     */
    @Override
    public String toString() {
        return this.getWhereSql();
    }
}

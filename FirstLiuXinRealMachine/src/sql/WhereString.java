package sql;

/**
 * 胡开明.2006年4月29日修订,初稿于2005年6月完成. 一个重要的SQL标准语句中的where子句生成工具类。 使用Class工具，将符合JDBC
 * API字段映射类型的对象作为参数，生成SQL标准语句中的where子句
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
     * 默认构造函数。
     * 可以通过setField(field)、setOperator(operator)、setValue(value)方法重新设定where子句中的各个组成部分.
     *
     */
    public WhereString(DbCenter db) {
        this.db = db;
    }

    /**
     * 构造函数。
     * 可以通过setField(field)、setOperator(operator)、setValue(value)方法重新设定where子句中的各个组成部分.
     * field是字段名；operator是操作符，如：=、is、like等；value是符合JDBC API映射的字段类型的值对象
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
     * 构造函数。
     * 可以通过setField(field)、setOperator(operator)、setValue(value)方法重新设定where子句中的各个组成部分.
     * field是字段名；operator是操作符，如：=、is、like等；values是符合JDBC API映射的字段类型的值对象数组
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
     * 构造函数。
     * 可以通过setField(field)、setOperator(operator)、setValue(value)方法重新设定where子句中的各个组成部分.
     * field是字段名；operator是操作符，如：=、is、like等；subquery将生成一个子查询
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
     * 核心方法，生成一个完整的SQL标准语句中的where子句。
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
     * 将不同的符合JDBC API映射的字段类型的值对象转化为字符串，使其成为SQL中标准where子句的值。
     *
     */
    private String objValuesToString(Object[] obj) {
        String v = "null";
        if (obj == null) {
            return v;    //如果对象为空,返回无单引号的null字符串
        }
        String clsName = obj[0].getClass().getName();
        int length = 0;
        for (int i = 0; i < obj.length; i++) {
            if (obj[i] != null) {
                length++; //非空值数量
            }
        }
        if (length == 0) {
            return v;  //整个对象数组的元素均为空时,返回无单引号的null字符串
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
                return v;     //如果不符合规定类型返回无单引号的null字符串
            }

            String[] _obj = new String[length];
            int j = 0;
            for (int i = 0; i < obj.length; i++) {
                if (obj[i] != null) {
                    _obj[j] = obj[i].toString(); //过滤空值
                    j++;
                }
            }

            if (clsName.equals("java.lang.Integer") || clsName.equals("java.lang.Float") || clsName.equals("java.lang.Long") || clsName.equals("java.lang.Double") || clsName.equals("java.lang.Short")) {
                //数字型均不加单引号
                v = tool.arryToString(_obj, ",");
                return v;
            }

            if (clsName.equals("java.lang.Boolean")) {
                //不知是否加上单引号,请帮个忙
                v = tool.arryToString(_obj, ",");
                return v;
            }

            if (clsName.equals("java.lang.String") || clsName.equals("java.sql.Date") || clsName.equals("java.sql.Time") || clsName.equals("java.sql.Timestamp")) {
                //字符串型均加上单引号
                _obj = tool.arryToArray(_obj, "'", "'");
                v = tool.arryToString(_obj, ",");
                return v;
            }

        }
        return v;
    }

    /**
     * 核心方法，将不同的符合JDBC API映射的字段类型的值对象转化为字符串，使其成为SQL中标准where子句的值。
     *
     */
    private String objValueToString(Object objValue) {
        String v = "null";
        if (objValue == null) {
            return v;      //如果对象为空,返回无单引号的null字符串
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
            return v;//如果不符合规定类型返回无单引号的null字符串
        }
        if (clsName.equals("java.lang.Integer") || clsName.equals("java.lang.Float") || clsName.equals("java.lang.Long") || clsName.equals("java.lang.Double") || clsName.equals("java.lang.Short")) {
            return objValue.toString();//数字型均不须加上单引号
        }
        if (clsName.equals("java.lang.Boolean")) {
            return objValue.toString();//不知是否加上单引号,请帮个忙
        }
        v="'" + objValue.toString() + "'";//字符串型均加上单引号
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
     * 重载toString()方法，以生成完整的where子句.
     *
     */
    @Override
    public String toString() {
        return this.getWhereSql();
    }
}

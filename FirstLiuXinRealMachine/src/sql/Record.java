/*
 * Record.java
 * 2012-10-31
 * 胡开明
 */
package sql;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * 本类中的方法均从ProcessVO中分离出来的方法，方法名做了修改，功能不变，对结果集操作
 *
 * @author hkm
 */
public class Record implements java.io.Serializable {

    /**
     * @param recordList 是一个已知的查询结果集
     * @param fieldName 是一个指定的字段名
     * @param fieldValue 是一个指定的字段的值
     * @return 获取记录数.
     *
     */
    public int count(List recordList, String fieldName, Object fieldValue) {
        int length = 0;
        if (recordList == null) {
            return 0;
        }
        if (fieldName == null) {
            return 0;
        }
        for (int i = 0; i < recordList.size(); i++) {
            Map m = (Map) recordList.get(i);
            if (fieldValue == null) {
                if (m.get(fieldName) == null) {
                    length++;
                }
            } else {
                Object value = m.get(fieldName);
                if (value != null) {
                    if ((fieldValue.toString()).equals(value.toString())) {
                        length++;
                    }
                }
            }
        }
        return length;
    }

    /**
     * @param resultList 一个已经存在的结果集
     * @param fieldName 一条Map型记录的域名;
     * @param fieldValue 域值
     * @return 从现有的结果集中删除符合条件的记录,操作完成后重新返回该结果集.
     */
    public List delete(List resultList, String fieldName, Object fieldValue) {
        Map map = null;
        Object ID = null;
        if (resultList != null) {
            if (resultList.size() > 0) {
                for (int i = 0; i < resultList.size(); i++) {
                    map = (Map) resultList.get(i);
                    ID = map.get(fieldName);
                    if (ID.toString().equals(fieldValue.toString())) {
                        resultList.remove(i);
                    }
                }
            }
        }
        return resultList;
    }

    /**
     * @param resultList 是一个已经存在的查询结果集
     * @param fieldName 为指定字段
     * @param fieldValue 为该字段的值,可为null
     * @param lessEqualLength 结果长度
     * @return 获取指定字段值为fieldValue,且长度小于等lessEqualLength的查询结果集的最大子集;
     * 数量小于等于lessEqualLength,当lessEqualLength>v.size()时,lessEqualLength取值为v.size();
     */
    public List list(List resultList, String fieldName, Object fieldValue, int lessEqualLength) {
        List r = new ArrayList();
        Map map = null;
        Object ID = null;
        if (lessEqualLength <= 0) {
            return r;
        }
        int m = 0;
        if (resultList != null) {
            if (resultList.size() > 0) {
                if (lessEqualLength > resultList.size()) {
                    lessEqualLength = resultList.size();
                }
                for (int i = 0; i < resultList.size(); i++) {
                    map = (Map) resultList.get(i);
                    ID = map.get(fieldName);
                    if (fieldValue == null) {
                        if (ID == null) {
                            r.add(m, map);
                            m++;
                        }
                    } else {
                        if (ID != null) {
                            if ((fieldValue.toString()).equals(ID.toString())) {
                                r.add(m, map);
                                m++;
                            }
                        }
                    }
                    if (r.size() == lessEqualLength) {
                        return r;
                    }
                }
            }
        }
        return r;
    }

    /**
     * @param resultList 是一个已经存在的查询结果集
     * @param fieldName 为指定字段
     * @param fieldValue 为该字段的值,可以设定为null
     * @return 获取指定字段值为fieldValue的查询结果集的最大子集。
     *
     */
    public List list(List resultList, String fieldName, Object fieldValue) {
        int lessEqualLength = resultList.size();
        return this.list(resultList, fieldName, fieldValue,
                lessEqualLength);
    }

    /**
     * @param resultList 是一个已经存在的查询结果集
     * @param index 为起始位置
     * @param lessEqualLength 为指定长度
     * @return 获取查询结果集中符合条件的最大子集。
     */
    public List list(List resultList, int index, int lessEqualLength) {
        List r = new ArrayList();
        Map map = null;
        if (resultList != null) {
            if (resultList.size() > 0) {
                if (lessEqualLength > resultList.size()) {
                    lessEqualLength = resultList.size();
                }
                int n = resultList.size() - index;
                if (lessEqualLength > n) {
                    lessEqualLength = n;
                }
                int m = 0;
                for (int i = index; i < index + lessEqualLength; i++) {
                    map = (Map) resultList.get(i);
                    r.add(m, map);
                    m = m + 1;
                }
            }
        }
        return r;
    }

    /**
     * 从查询结果集中返回某一字段值的集合
     *
     * @param resultList 是一个已经存在的查询结果集
     * @param fieldName 是某一字段名
     */
    public Set oneFieldSet(List resultList, String fieldName) {
        Set s = new HashSet();
        if (resultList != null) {
            if (resultList.size() > 0) {
                for (int i = 0; i < resultList.size(); i++) {
                    Map m = (Map) resultList.get(i);
                    s.add(m.get(fieldName));
                }
            }
        }
        return s;
    }

    /**
     * @param resultList 是一个结果集
     * @return 获取首条记录。
     */
    public Map first(List resultList) {
        Map m = new LinkedHashMap();
        if (resultList != null) {
            if (resultList.size() > 0) {
                m = (Map) resultList.get(0);
            }
        }
        return m;
    }

    /**
     * @param resultList 是一个结果集
     * @return 从一个现有的List型结果集中获取最后一条记录
     */
    public Map last(List resultList) {
        Map m = new LinkedHashMap();
        if (resultList != null) {
            if (resultList.size() > 0) {
                m = (Map) resultList.get(resultList.size() - 1);
            }
        }
        return m;
    }

    /**
     * @param resultList 是一个结果集
     * @param index 是索引号,第一条记录是从0开始
     * @return 从一个现有的List型结果集中,根据索引号，获取一条记录
     */
    public Map indexOne(List resultList, int index) {
        Map m = new LinkedHashMap();
        if (resultList.size() > 0) {
            m = (Map) resultList.get(index);
        }
        return m;
    }

    /**
     * @param resultList 是一个已有的结果集,
     * @param fieldName 是指定字段,
     * @param fieldValue 是指定字段的值.
     * @return 从现有的List型结果集中,获取符合指定条件的第一条记录。
     */
    public Map one(List resultList, String fieldName, Object fieldValue) {
        Map m = new LinkedHashMap();
        Map map = null;
        Object ID = null;
        if (resultList != null) {
            if (resultList.size() > 0) {
                for (int i = 0; i < resultList.size(); i++) {
                    map = (Map) resultList.get(i);
                    ID = map.get(fieldName);
                    if (ID != null && (ID.toString()).equals(fieldValue.toString())) {
                        m = map;
                        return m;
                    }
                }
            }
        }
        return m;
    }

    /**
     * @param resultList 是一个List结果集;
     * @param fieldName 是关键字段名;
     * @param fieldValue 是关键字段值.
     * @return 根据一个现有的List型结果集获取记录号.
     */
    public int index(List resultList, String fieldName, Object fieldValue) {
        int index = -1;
        Map map = null;
        Object ID = null;
        if (resultList != null) {
            if (resultList.size() > 0) {
                for (int i = 0; i < resultList.size(); i++) {
                    map = (Map) resultList.get(i);
                    ID = map.get(fieldName);
                    if ((ID.toString()).equals(fieldValue.toString())) {
                        index = i;
                        break;
                    }
                }
            }
        }
        return index;
    }

    /**
     * 用于分页显示 具体用法可参见cn.hkm.web.PvoPageTag.java,对应的标签是&lt;pvo:page/&gt;<br>
     * &lt;pvo:page/&gt;是一个利用PVO技术开发的,非常有价值的分页标签<br>
     *
     * @param resultList 是一个查询结果集,
     * @param numPerPage 是每页记录数.
     * @return 返回一个Map对象，其中包含了分页所需的相关数据
     */
    public Map page(List resultList, int numPerPage) {
        Map m = new LinkedHashMap();
        int all = resultList.size();
        int allpage = 0;//(int) Math.ceil( (float) all / numPerPage);
        int mod = all % numPerPage;

        if (mod == 0) {
            allpage = all / numPerPage;
        } else {
            allpage = 1 + all / numPerPage;
        }
        List pages = new ArrayList();
        for (int i = 1; i <= allpage; i++) {
            pages.add(new Label("" + i, "" + (i - 1)));
        }
        if (allpage == 1) {
            m.put("all", "" + all);
            m.put("first", "" + 1);
            m.put("allpage", "" + allpage);
            m.put("pageList", pages);
            m.put("current", "" + 1);
        }
        if (allpage > 1) {
            m.put("all", "" + all);//总记录数
            m.put("first", "" + 1);//第一页的标签
            m.put("allpage", "" + allpage);//总分页数
            m.put("pageList", pages);//分页的标签列表
            m.put("last", "" + allpage);//默认,最后一页的标签
            m.put("current", "" + 1);//默认,当前页的标签
        }
        return m;
    }

    /**
     * 交换两条记录的位置
     *
     * @param resultList 是一个结果集,
     * @param low 指定交换的位置，低位。
     * @param high 指定交换的位置，高位。
     */
    public void swap(List resultList, int low, int high) {
        if (resultList.size() <= 1) {
            return;
        }
        if (low > resultList.size()) {
            return;
        }
        if (low < 0) {
            return;
        }
        if (high > resultList.size()) {
            return;
        }
        if (high < 0) {
            return;
        }
        Map lm = (Map) resultList.get(low);
        Map hm = (Map) resultList.get(high);
        if (resultList.size() > 1) {
            resultList.add(low, hm);
            resultList.remove(low + 1);
            resultList.add(high, lm);
            resultList.remove(high + 1);
        }
    }

    /**
     * @param resultList 是一个结果集
     * @param fieldName 是指定的字段
     * @param asc asc为true是升序，为false是降序
     * @return 对现有的List型结果集,按指定字段排序
     */
    public List sort(List resultList, String fieldName, boolean asc) {
        SortedSet set = new TreeSet();
        List list = new ArrayList();
        List tmp = null;
        Iterator it = null;
        if (resultList.size() <= 1) {
            return resultList;
        }
        if (resultList.size() > 1) {
            for (int i = 0; i < resultList.size(); i++) {
                set.add(((Map) resultList.get(i)).get(fieldName));
            }
        }
        if (set.size() < 1) {
            return resultList;
        } else {
            it = set.iterator();
            if (asc) {
                while (it.hasNext()) {
                    tmp = list(resultList, fieldName, set.first());
                    set.remove(set.first());
                    list.addAll(tmp);
                    it = set.iterator();
                }
            } else {
                while (it.hasNext()) {
                    tmp = list(resultList, fieldName, set.last());
                    set.remove(set.last());
                    list.addAll(tmp);
                    it = set.iterator();
                }
            }
            resultList = list;
        }
        return resultList;
    }

    /**
     * 将现有一条记录,转换成xml片断
     */
    public String toXml(Map m) {
        String xml = "";
        Set set = m.keySet();
        Object[] obj = set.toArray();
        for (int i = 0; i < obj.length; i++) {
            Object value = m.get(obj[i]);
            String s = value.toString();
            s = s.replace("<", "&lt;");
            s = s.replace(">", "&gt;");
            xml = xml + "<" + obj[i] + ">" + s + "</" + obj[i] + ">";
        }
        return xml;
    }

    /**
     * 将现有结果集,根据指定字段转换成二维数组,主要供JTable表使用
     */
    public Object[][] twoDim(List<Map<String, Object>> pvoList, Object[] fields) {
        Object[][] obj = new Object[pvoList.size()][fields.length];
        for (int i = 0; i < pvoList.size(); i++) {
            Map m = (Map) pvoList.get(i);
            for (int j = 0; j < fields.length; j++) {
                obj[i][j] = m.get(fields[j]);
            }
        }
        return obj;
    }

    /**
     * 将现有结果集,转换成二维数组,主要供JTable表使用
     */
    public Object[][] twoDim(List<Map<String, Object>> pvoList) {
        Object[][] obj = new Object[pvoList.size()][((Map) pvoList.get(0)).size()];
        for (int i = 0; i < pvoList.size(); i++) {
            Map m = pvoList.get(i);
            obj[i] = m.values().toArray();
        }
        return obj;
    }
}

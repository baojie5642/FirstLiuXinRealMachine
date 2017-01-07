/*
 * Record.java
 * 2012-10-31
 * ������
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
 * �����еķ�������ProcessVO�з�������ķ����������������޸ģ����ܲ��䣬�Խ��������
 *
 * @author hkm
 */
public class Record implements java.io.Serializable {

    /**
     * @param recordList ��һ����֪�Ĳ�ѯ�����
     * @param fieldName ��һ��ָ�����ֶ���
     * @param fieldValue ��һ��ָ�����ֶε�ֵ
     * @return ��ȡ��¼��.
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
     * @param resultList һ���Ѿ����ڵĽ����
     * @param fieldName һ��Map�ͼ�¼������;
     * @param fieldValue ��ֵ
     * @return �����еĽ������ɾ�����������ļ�¼,������ɺ����·��ظý����.
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
     * @param resultList ��һ���Ѿ����ڵĲ�ѯ�����
     * @param fieldName Ϊָ���ֶ�
     * @param fieldValue Ϊ���ֶε�ֵ,��Ϊnull
     * @param lessEqualLength �������
     * @return ��ȡָ���ֶ�ֵΪfieldValue,�ҳ���С�ڵ�lessEqualLength�Ĳ�ѯ�����������Ӽ�;
     * ����С�ڵ���lessEqualLength,��lessEqualLength>v.size()ʱ,lessEqualLengthȡֵΪv.size();
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
     * @param resultList ��һ���Ѿ����ڵĲ�ѯ�����
     * @param fieldName Ϊָ���ֶ�
     * @param fieldValue Ϊ���ֶε�ֵ,�����趨Ϊnull
     * @return ��ȡָ���ֶ�ֵΪfieldValue�Ĳ�ѯ�����������Ӽ���
     *
     */
    public List list(List resultList, String fieldName, Object fieldValue) {
        int lessEqualLength = resultList.size();
        return this.list(resultList, fieldName, fieldValue,
                lessEqualLength);
    }

    /**
     * @param resultList ��һ���Ѿ����ڵĲ�ѯ�����
     * @param index Ϊ��ʼλ��
     * @param lessEqualLength Ϊָ������
     * @return ��ȡ��ѯ������з�������������Ӽ���
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
     * �Ӳ�ѯ������з���ĳһ�ֶ�ֵ�ļ���
     *
     * @param resultList ��һ���Ѿ����ڵĲ�ѯ�����
     * @param fieldName ��ĳһ�ֶ���
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
     * @param resultList ��һ�������
     * @return ��ȡ������¼��
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
     * @param resultList ��һ�������
     * @return ��һ�����е�List�ͽ�����л�ȡ���һ����¼
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
     * @param resultList ��һ�������
     * @param index ��������,��һ����¼�Ǵ�0��ʼ
     * @return ��һ�����е�List�ͽ������,���������ţ���ȡһ����¼
     */
    public Map indexOne(List resultList, int index) {
        Map m = new LinkedHashMap();
        if (resultList.size() > 0) {
            m = (Map) resultList.get(index);
        }
        return m;
    }

    /**
     * @param resultList ��һ�����еĽ����,
     * @param fieldName ��ָ���ֶ�,
     * @param fieldValue ��ָ���ֶε�ֵ.
     * @return �����е�List�ͽ������,��ȡ����ָ�������ĵ�һ����¼��
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
     * @param resultList ��һ��List�����;
     * @param fieldName �ǹؼ��ֶ���;
     * @param fieldValue �ǹؼ��ֶ�ֵ.
     * @return ����һ�����е�List�ͽ������ȡ��¼��.
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
     * ���ڷ�ҳ��ʾ �����÷��ɲμ�cn.hkm.web.PvoPageTag.java,��Ӧ�ı�ǩ��&lt;pvo:page/&gt;<br>
     * &lt;pvo:page/&gt;��һ������PVO����������,�ǳ��м�ֵ�ķ�ҳ��ǩ<br>
     *
     * @param resultList ��һ����ѯ�����,
     * @param numPerPage ��ÿҳ��¼��.
     * @return ����һ��Map�������а����˷�ҳ������������
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
            m.put("all", "" + all);//�ܼ�¼��
            m.put("first", "" + 1);//��һҳ�ı�ǩ
            m.put("allpage", "" + allpage);//�ܷ�ҳ��
            m.put("pageList", pages);//��ҳ�ı�ǩ�б�
            m.put("last", "" + allpage);//Ĭ��,���һҳ�ı�ǩ
            m.put("current", "" + 1);//Ĭ��,��ǰҳ�ı�ǩ
        }
        return m;
    }

    /**
     * ����������¼��λ��
     *
     * @param resultList ��һ�������,
     * @param low ָ��������λ�ã���λ��
     * @param high ָ��������λ�ã���λ��
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
     * @param resultList ��һ�������
     * @param fieldName ��ָ�����ֶ�
     * @param asc ascΪtrue������Ϊfalse�ǽ���
     * @return �����е�List�ͽ����,��ָ���ֶ�����
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
     * ������һ����¼,ת����xmlƬ��
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
     * �����н����,����ָ���ֶ�ת���ɶ�ά����,��Ҫ��JTable��ʹ��
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
     * �����н����,ת���ɶ�ά����,��Ҫ��JTable��ʹ��
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

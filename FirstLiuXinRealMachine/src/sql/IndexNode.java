/*
 * �����ڵ�
 */
package sql;

/**
 *
 * @author hkm
 */
public class IndexNode {
    
    /**
     * ���ݿ���е�һ�����ؼ��ֶε�ֵ�����û�йؼ��ֶΣ����ǵ�һ���ֶε�ֵ
     */
    private Object firstKeyValue;//
    /**
     * �������ؼ���˳���ѯ�õ����кţ���JDBC�У���һ����¼���к���1���к��볤����ȫ��ȣ���һ��ǰ�ĺ����һ������кž�Ϊ0��
     */
    private int row;
    
    public IndexNode(){}

    public Object getFirstKeyValue() {
        return firstKeyValue;
    }

    public void setFirstKeyValue(Object firstKeyValue) {
        this.firstKeyValue = firstKeyValue;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }
    
    

}

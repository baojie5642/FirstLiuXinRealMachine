package SpaceProtostuff.demo1;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class B implements Serializable{
	private static final long serialVersionUID=2016060810564255555l;
	
	private int num;
    private String str;
    private List<String> list;
    public B() {
        num = 3;
        str = "rpc";
 
        list = new ArrayList<String>();
        list.add("rpc-list");
    }
    public int getNum() {
        return num;
    }
    public void setNum(int num) {
        this.num = num;
    }
    public String getStr() {
        return str;
    }
    public void setStr(String str) {
        this.str = str;
    }
    public List<String> getList() {
        return list;
    }
    public void setList(List<String> list) {
        this.list = list;
    }
     
}

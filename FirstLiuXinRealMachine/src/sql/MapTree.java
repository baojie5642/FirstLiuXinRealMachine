package sql;

import java.util.*;

public class MapTree {

  private List resultSet;
  private String mainKey;
  private String parentKey;
  int[] help;

  private MapTree() {
    try {
      jbInit();
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  private MapTree(List resultset,String mainkey,String parentkey){
    this.resultSet=resultset;
    this.mainKey=mainkey;
    this.parentKey=parentkey;
    this.help=new int[resultset.size()];
  }

  public static MapTree createMapTree(List resultset,String mainkey,String parentkey) {
    return new MapTree(resultset,mainkey,parentkey);
  }

  public static MapTree createMapTree() {
    return new MapTree();
  }

  /**使用此方法时用help[]=new int[v.size()];help用于标识已处理过的结点*/
  public MapNode nodeTree(Object mainID){
    MapNode root=MapNode.createMapNode();
    return this._nodeTree(root,mainID);
  }

  /**使用此方法时用help[]=new int[v.size()];help用于标识已处理过的结点*/
  private MapNode _nodeTree(MapNode root,Object mainID){
    if(mainID==null)return null;
    Map m=null;
    for(int k=0;k<resultSet.size();k++){
      if (help[k] == 0){
        m = (Map) resultSet.get(k);
        if (mainID.equals(m.get(mainKey))){ //如果是根
          root.setPos(k);
          root.setRecord(m);
          List list = root.getChildList();
          help[k] = 1;
          for (int l = 0; l < resultSet.size(); l++)if (help[l] == 0) { //寻找该根下的直接子结点
            m = (Map) resultSet.get(l);
            if(mainID.equals(m.get(parentKey))){
              MapNode node = MapNode.createMapNode();
              list.add(_nodeTree(node,m.get(mainKey)));
            }
          }
        }
      }
    }
    return root;
  }

  public void printTree(MapNode node){
     System.out.print(node.getPos()+"  :  ");
     System.out.println(node.getRecord());
     if(node.getChildList().size()>0){
       for(int i=0;i<node.getChildList().size();i++)
         printTree((MapNode)node.getChildList().get(i));
     }
  }

  /** 将节点树中排列的记录按父子关系重新写入到List对象中 */
  public List listInTree(MapNode node){
    List v=new LinkedList();
    return _listInTree(v,node);
  }

  private List _listInTree(List v,MapNode node){
    v.add(node.getRecord());
    List list=node.getChildList();
    if(list.size()>0){
      for(int i=0;i<list.size();i++){
        node=(MapNode)list.get(i);
        _listInTree(v,node);
      }
    }
    return v;
  }

  public int[] getHelp() {
    return help;
  }

  public String getMainKey() {
    return mainKey;
  }

  public String getParentKey() {
    return parentKey;
  }

  public List getResultSet() {
    return resultSet;
  }

  public void setHelp(int[] help) {
    this.help = help;
  }

  public void setMainKey(String mainKey) {
    this.mainKey = mainKey;
  }

  public void setParentKey(String parentKey) {
    this.parentKey = parentKey;

  }

  public void setResultSet(List resultSet) {
    this.resultSet = resultSet;
  }

  private void jbInit() throws Exception {
  }

}

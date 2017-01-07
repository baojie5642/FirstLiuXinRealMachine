package sql;

import java.util.*;

/**
 用于定义单表模拟树型结构中的结点.
 * */

public class MapNode {
  private String name;
  private int pos;
  private Object selfId;
  private Object parentId;
  private Map record;
  private List childList;

  private MapNode() {
    childList=new LinkedList();
  }

  public static MapNode createMapNode(){
     return new MapNode();
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setPos(int pos) {
    this.pos = pos;
  }

  public void setSelfId(Object selfId) {
    this.selfId = selfId;
  }

  public void setParentId(Object parentId) {
    this.parentId = parentId;
  }

  public void setRecord(Map record) {
    this.record = record;
  }

  public void setChildList(List childList) {
    this.childList = childList;
  }

  public String getName() {
    return name;
  }

  public int getPos() {
    return pos;
  }

  public Object getSelfId() {
    return selfId;
  }

  public Object getParentId() {
    return parentId;
  }

  public Map getRecord() {
    return record;
  }

  public List getChildList() {
    return childList;
  }
}

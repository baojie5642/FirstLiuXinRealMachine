/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sql;

import cn.jadepool.util.DateTool;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 *负责管理pvo的配置文件；只使用db.xml的连接
 * 创建日期：2012-10-18 用途：初始化db.xml及其他配置文件；解析db.xml及其他配置文件。
 *
 * @author hkm
 */
public class FileManager {

    private FileHandler defaultLogFile = null;
    private DateTool dt = new DateTool();
    private String path = "";//使用应用程序/META-INF/目录配置相关文件
    private boolean isInit = false;
    private DbCenter db = null;//实例化时，Db使用的数据库连接类型与ProcessVO一致

    public FileManager() {
        db=DbCenter.instance(DbConnection.getDefaultCon(), DbConnectionType.USING_CONFIG_OF_DEFAULT);
        path = initPath();
    }

    /**
     * js路径的创建,由用户指定web根目录下的一个子目录
     * 通过计算path的平级目录是否有WEB-INF，或他的上级是否含有WEB-INF在web根目录下创建一个
     * 建议用户在web中在web根目录下创建一个目录,存放pvo.js、pvoVerify.js文件
     */
    public String getSubWebPath() {
        return "";
    }

    /**
     * 建议在web应用中，将path指定为"WEB-INF"的绝对路径 在Application应用程序中使用defaultPath
     *
     * @param path 用户指定的路径，系统将在其下创建path/config和path/log两个子目录
     */
    public void setPath(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public String getConfigPath() {
        String configPath = getPath();// + File.separator + "config";
        return configPath;
    }

    public String getLogPath() {
        String logPath = getPath() + File.separator + "log";
        return logPath;
    }

    /**
     * 每天一个日志文件
     */
    public String getLogFileName() {
        String f = getLogPath() + File.separator + "pvo-" + dt.date() + ".log";
        return f;
    }

    public FileHandler getLogFile() {
        if (!isInit) {
            initPath();
        }
        File f = new File(getLogFileName());
        if (!f.exists()) {
            try {
                defaultLogFile = new FileHandler(getLogFileName(), true);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return defaultLogFile;
    }

    public String getDbXmlFileName() {
        String f = getConfigPath() + File.separator + "db.xml";
        return f;
    }

    public String getRegexXmlFileName() {
        String f = getConfigPath() + File.separator + "regex.xml";
        return f;
    }

    public String getFilterLinkXmlFileName() {
        String f = getConfigPath() + File.separator + "filterLink.xml";
        return f;
    }

    public String getFilterUserXmlFileName() {
        String f = getConfigPath() + File.separator + "filterUser.xml";
        return f;
    }

    public String getFilterCodeXmlFileName() {
        String f = getConfigPath() + File.separator + "filterCode.xml";
        return f;
    }

    public void createDbXML() {
        if (!isInit) {
            initPath();
        }
        File f = new File(getDbXmlFileName());
        if (!f.exists()) {
            try {
                f.createNewFile();
                BufferedWriter bout = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f), "GBK"));
                String dbxml = "<?xml version=\"1.0\" encoding=\"GBK\"?>\n" + "<!--\n" + "    Document    : db.xml\n" + "    Created on  : " + dt.dateTime() + "\n" + "    Author      : hkm\n" + "    Description : Purpose of the document follows.\n" + "-->\n" + "<!DOCTYPE db [\n" + "<!ELEMENT db (resource|password|user|driver|url)*>\n" + "<!ELEMENT url (#PCDATA)>\n" + "<!ELEMENT driver (#PCDATA)>\n" + "<!ELEMENT user (#PCDATA)>\n" + "<!ELEMENT password (#PCDATA)>\n" + "<!ELEMENT resource (#PCDATA)>\n" + "]>\n" + "<db>\n" + "    <url>jdbc:mysql://localhost:3306/mydb?useUnicode=true&amp;characterEncoding=gbk</url>\n" + "    <driver>com.mysql.jdbc.Driver</driver>\n" + "    <user>root</user>\n" + "    <password>root</password>\n" + "    <resource>java:comp/env/jdbc/mydb</resource>\n" + "</db>\n";
                bout.write(dbxml);
                bout.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void createFilterLinkXML() {
        if (!isInit) {
            initPath();
        }
        File f = new File(getFilterLinkXmlFileName());
        if (!f.exists()) {
            try {
                f.createNewFile();
                BufferedWriter bout = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f), "GBK"));

                StringBuffer sb = new StringBuffer();
                InputStream js = FileManager.class.getResourceAsStream("resources/filterLink.xml");
                int c = -1;
                try {
                    while ((c = js.read()) >= 0) {
                        sb.append((char) c);
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                bout.write(sb.toString());

                bout.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void createFilterUserXML() {
        if (!isInit) {
            initPath();
        }
        File f = new File(getFilterUserXmlFileName());
        if (!f.exists()) {
            try {
                f.createNewFile();
                BufferedWriter bout = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f), "GBK"));

                StringBuffer sb = new StringBuffer();
                InputStream js = FileManager.class.getResourceAsStream("resources/filterUser.xml");
                int c = -1;
                try {
                    while ((c = js.read()) >= 0) {
                        sb.append((char) c);
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                bout.write(sb.toString());

                bout.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void createFilterCodeXML() {
        if (!isInit) {
            initPath();
        }
        File f = new File(getFilterCodeXmlFileName());
        if (!f.exists()) {
            try {
                f.createNewFile();
                BufferedWriter bout = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f), "GBK"));

                StringBuffer sb = new StringBuffer();
                InputStream js = FileManager.class.getResourceAsStream("resources/filterCode.xml");
                int c = -1;
                try {
                    while ((c = js.read()) >= 0) {
                        sb.append((char) c);
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                bout.write(sb.toString());

                bout.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * 此方法依赖Db是否已初始化
     */
    public void createRegexXML() {

        if (!isInit) {
            initPath();
        }
        File f = new File(getRegexXmlFileName());
        if (!f.exists()) {
            try {
                f.createNewFile();
                String[] tables = db.getTableNames();
                String xml = "";
                if (tables != null && tables.length > 0) {
                    for (int i = 0; i < tables.length; i++) {
                        xml = xml + this.outRegexXML(tables[i]);
                    }
                }
                xml = "<?xml version=\"1.0\" encoding=\"GBK\"?>\n" + "<!--\n" + "    Document   : regex.xml\n" + "    Created on : " + dt.dateTime() + "\n" + "    Author     : pvo\n" + "    Description: Purpose of the document follows. Write regex value, PVO will auto check the upload field value.  \n" + "-->\n" + " <!DOCTYPE root [\n" + " <!ELEMENT root (table)*>\n" + " <!ELEMENT table (field)*>\n" + " <!ATTLIST table name ID #REQUIRED>\n" + " <!ELEMENT field EMPTY>\n" + " <!ATTLIST field regex CDATA #IMPLIED name CDATA #IMPLIED>\n" + " ]>\n" + "<root>\n" + xml + "</root>\n";
                BufferedWriter bout = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f), "GBK"));
                bout.write(xml);
                bout.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * 依赖Db是否初始化
     */
    private String outRegexXML(String tableName) {
        String xml = "";

        Table table = db.getTableMap().get(tableName);
        String[] keys = table.getKeys();
        String[] fields = table.getFields();
        for (int i = 0; i < fields.length; i++) {
            Field f = table.getFieldMap().get(fields[i]);
            String type = f.getTypeName();
            if (type.equals("CHAR") || type.equals("VARCHAR")) {
                String name = fields[i];
                //if(isKey(name))name="id";
                xml = xml + "        <field name=\"" + name + "\" regex=\"\"></field>\n";
            }
        }
        xml = "    <table name=\"" + tableName + "\">\n" + xml + "    </table>\n";
        return xml;
    }

    private String outAllFieldRegexXML(String tableName) {
        String xml = "";

        Table table = db.getTableMap().get(tableName);
        String[] keys = table.getKeys();
        String[] fields = table.getFields();
        for (int i = 0; i < fields.length; i++) {
            Field f = table.getFieldMap().get(fields[i]);
            String name = fields[i];
            xml = xml + "        <field name=\"" + name + "\" regex=\"\"></field>\n";
        }
        xml = "    <table name=\"" + tableName + "\">\n" + xml + "    </table>\n";
        return xml;
    }

    public Map parseDbXML() {
        Document doc = null;
        Map dbConfigMap = null;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            doc = factory.newDocumentBuilder().parse(new File(getDbXmlFileName()));
            NodeList children = doc.getElementsByTagName("db");
            NodeList e = children.item(0).getChildNodes();
            dbConfigMap = new HashMap();
            for (int k = 0; k < e.getLength(); k++) {
                Node child = e.item(k);
                if (child instanceof Element) {
                    Element e1 = (Element) child;
                    Text textNode = (Text) e1.getFirstChild();
                    if (textNode != null) {
                        String text = textNode.getData().trim();
                        //<!ELEMENT db (resource|password|user|driver|url)*>
                        if (e1.getTagName().equals("url")) {
                            dbConfigMap.put("url", text);
                        }
                        if (e1.getTagName().equals("driver")) {
                            dbConfigMap.put("driver", text);
                        }
                        if (e1.getTagName().equals("user")) {
                            dbConfigMap.put("user", text);
                        }
                        if (e1.getTagName().equals("password")) {
                            dbConfigMap.put("password", text);
                        }
                        if (e1.getTagName().equals("resource")) {
                            dbConfigMap.put("resource", text);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dbConfigMap;
    }

    public void parseRegexXML() {
        
        Document doc = null;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            doc = factory.newDocumentBuilder().parse(new File(this.getRegexXmlFileName()));
            NodeList childList = doc.getElementsByTagName("table");
            for (int i = 0; i < childList.getLength(); i++) {
                Element n = (Element) childList.item(i);
                String tableName = n.getAttribute("name");
                Table table = db.getTableMap().get(tableName);
                NodeList e = n.getChildNodes();

                for (int k = 0; k < e.getLength(); k++) {
                    Node child = e.item(k);
                    if (child instanceof Element) {
                        Element e1 = (Element) child;
                        String fieldName = e1.getAttribute("name");
                        Field field = table.getFieldMap().get(fieldName);
                        field.setRegex(e1.getAttribute("regex"));
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            try {
                db.getCon().close();
            } catch (SQLException ex) {
                Logger.getLogger(FileManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public List<String> getFilterLinkList() {
        List<String> v = new ArrayList();
        Document doc = null;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            doc = factory.newDocumentBuilder().parse(new File(this.getFilterLinkXmlFileName()));
            NodeList childList = doc.getElementsByTagName("root");
            for (int i = 0; i < childList.getLength(); i++) {
                Element n = (Element) childList.item(i);
                NodeList e = n.getChildNodes();
                for (int k = 0; k < e.getLength(); k++) {
                    Node child = e.item(k);
                    if (child instanceof Element) {
                        Element e1 = (Element) child;
                        String value = e1.getAttribute("href");
                        if (value != null && !"".equals(value)) {
                            v.add(value);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return v;
    }

    public List<String> getFilterUserList() {
        List<String> v = new ArrayList();
        Document doc = null;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            doc = factory.newDocumentBuilder().parse(new File(this.getFilterUserXmlFileName()));
            NodeList childList = doc.getElementsByTagName("root");
            for (int i = 0; i < childList.getLength(); i++) {
                Element n = (Element) childList.item(i);
                NodeList e = n.getChildNodes();
                for (int k = 0; k < e.getLength(); k++) {
                    Node child = e.item(k);
                    if (child instanceof Element) {
                        Element e1 = (Element) child;
                        String value = e1.getAttribute("id");
                        if (value != null && !"".equals(value)) {
                            v.add(value);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return v;
    }

    public List<String> getFilterCodeList() {
        List<String> v = new ArrayList();
        Document doc = null;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            doc = factory.newDocumentBuilder().parse(new File(this.getFilterCodeXmlFileName()));
            NodeList childList = doc.getElementsByTagName("root");
            for (int i = 0; i < childList.getLength(); i++) {
                Element n = (Element) childList.item(i);
                NodeList e = n.getChildNodes();
                for (int k = 0; k < e.getLength(); k++) {
                    Node child = e.item(k);
                    if (child instanceof Element) {
                        Element e1 = (Element) child;
                        String value = e1.getAttribute("value");
                        if (value != null && !"".equals(value)) {
                            v.add(value);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return v;
    }

    private String initPath() {
        URL url = FileManager.class.getResource("/META-INF/db.xml");
        path = url.getPath();
        if (path.indexOf("jar:") == 0) {
            path = path.substring("jar:file:/".length());
        }
        if (path.indexOf("file:") == 0) {
            path = path.substring("file:/".length());
        }
        path = path.substring(0, path.indexOf("db.xml"));

        File f = null;
        f = new File(getConfigPath());
        f.mkdirs();
        f = new File(getLogPath());
        f.mkdirs();
        isInit = true;

        return path;
    }

    public void testConfigFilePath() {
        URL url = FileManager.class.getResource("/META-INF/db.xml");
        URL persistence = FileManager.class.getResource("/META-INF/persistence.xml");//JPA2.0的持久化配置文件
        System.out.println(url.getPath());//在JavaFaces项目中的测试结果是  /D:/SNB_2012/JavaFaces/target/classes/META-INF/db.xml
    }
}

package sql;

import java.io.File;
import java.io.Serializable;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.HashMap;
import java.util.Map;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

public class DbConnection implements Serializable {

    public DbConnection() {
    }

    /**
     * 优先返回应用程序/META-INF/db.xml配置的连接资源中JDBC连接对象
     */
    synchronized public static Connection getDefaultCon() {
        Connection _con = null;
        String dbxml = getDbXmlFileName();
        File f = new File(dbxml);
        if (!f.exists()) {
            return _con;
        }
        Map<String, String> m = parseDbXML();
        String driver = m.get("driver");
        String url = m.get("url");
        String user = m.get("user");
        String password = m.get("password");
        boolean a = (url == null || "".equals(url) || user == null
                || "".equals(user) || password == null || "".equals(password)
                || driver == null
                || "".equals(driver));
        String resource = m.get("resource");
        boolean b = (resource == null || "".equals(resource));
        if (!b) {
            Context ctx;
            try {
                ctx = new javax.naming.InitialContext();
                
                if (!resource.startsWith("java:comp/env/")) {
                    //resource = "java:comp/env/" + resource;//在GlassFish中不需要加"java:comp/env/"
                }

                DataSource ds = (DataSource) ctx.lookup(resource);
                _con = ds.getConnection();
            } catch (SQLException ex) {
                ex.printStackTrace();
            } catch (NamingException ex) {
                ex.printStackTrace();
            } finally {
                return _con;
            }
        } else if (!a) {
            try {
                Class.forName(driver);
                _con = DriverManager.getConnection(url, user, password);
            } catch (ClassNotFoundException ex) {
                ex.printStackTrace();
            } catch (SQLException ex) {
                ex.printStackTrace();
            } finally {
                return _con;
            }
        }
        //if (a) { System.out.println("Please config the file /META-INF/db.xml as following ; '<db><url>jdbc:sqlserver://localhost\\dbo:1433;databaseName=Northwin</url><driver>com.microsoft.sqlserver.jdbc.SQLServerDriver</driver><user>sa</user><password>sa123</password><resource></resource></db>'");}
        return _con;
    }

    private static String getDbXmlFileName() {
        URL url = DbConnection.class.getResource("/META-INF/db.xml");
        String path = url.getPath();
        //System.out.println(path);
        /*
         if (path.indexOf("jar:") == 0) {
         path = path.substring("jar:file:/".length());
         }
         if (path.indexOf("file:") == 0) {
         path = path.substring("file:/".length());
         }
         path = path.substring(0, path.indexOf("db.xml"));
         String f = path + File.separator + "db.xml";//
         */
        return path;
    }

    private static Map parseDbXML() {
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
}

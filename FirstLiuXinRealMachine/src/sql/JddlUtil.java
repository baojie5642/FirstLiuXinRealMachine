/*
 * JddlUtil.java
 * Kaiming Hu
 * 
 */
package sql;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 创建管理数据库表
 *
 * @author hkm
 */
public class JddlUtil implements Jddl {

    /**
     * 执行sql文件
     *
     * @param con 数据库连接
     * @param sqlFileName
     */
    public boolean executeSqlFile(Connection con, String sqlFileName) {
        boolean b = false;
        Jade j = new Jade(con);
        JadeTool tool = new JadeTool();
        try {
            String sql = new String(tool.getBytesFromFile(new File(sqlFileName)));
            b = j.execute(sql);
            j.commit();
        } catch (IOException ex) {
            Logger.getLogger(JddlUtil.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return b;
    }

    /**
     * 根据默认配置，创建数据库表。 <br/>根据/META-INF/createTable.sql文件创建数据库表；
     */
    public boolean executeCreateTableSqlFile(Connection con) {
        boolean b = false;
        Jade j = new Jade(con);
        String fileName = JddlUtil.class.getResource("/META-INF/createTable.sql").getFile();
        JadeTool tool = new JadeTool();
        try {
            String sql = new String(tool.getBytesFromFile(new File(fileName)));
            b = j.execute(sql);
            j.commit();
        } catch (IOException ex) {
            Logger.getLogger(JddlUtil.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return b;
    }

    /**
     * 根据默认配置，创建数据库表。 <br/> 用户必须配置两个文件：
     * <br/>1、根据/META-INF/createTable.sql文件创建数据库表；
     * <br/>2、根据/META-INF/db.xml文件建立数据库连接。
     */
    public boolean executeCreateTableSqlFile() {
        boolean b = false;
        Jade j = new Jade();
        String fileName = JddlUtil.class.getResource("/META-INF/createTable.sql").getFile();
        JadeTool tool = new JadeTool();
        try {
            String sql = new String(tool.getBytesFromFile(new File(fileName)));
            b = j.execute(sql);
            j.commit();
        } catch (IOException ex) {
            Logger.getLogger(JddlUtil.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return b;
    }
}

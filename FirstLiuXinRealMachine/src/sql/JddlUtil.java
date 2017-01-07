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
 * �����������ݿ��
 *
 * @author hkm
 */
public class JddlUtil implements Jddl {

    /**
     * ִ��sql�ļ�
     *
     * @param con ���ݿ�����
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
     * ����Ĭ�����ã��������ݿ�� <br/>����/META-INF/createTable.sql�ļ��������ݿ��
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
     * ����Ĭ�����ã��������ݿ�� <br/> �û��������������ļ���
     * <br/>1������/META-INF/createTable.sql�ļ��������ݿ��
     * <br/>2������/META-INF/db.xml�ļ��������ݿ����ӡ�
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

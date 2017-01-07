/*
 * @(#)JadeTool.java
 */
package sql;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.logging.*;
import java.util.regex.Pattern;

/**
 * @author ������<br/> ������2005��6����ɣ�<br/> �����ַ�����ʽ�����<br/>
 * ������еļ�����������ProcessVO��WhereString����Ƶ�����ã�<br/> �޶����ڣ�2012-10-17<br/>
 */
public class JadeTool implements java.io.Serializable {

    public JadeTool() {
    }

    /**
     * ���ַ����ֽ���ַ�������
     *
     * @param str ���ֽ���ַ���
     * @param token �ָ������磺"|"��","��"."��"---"��" "��
     * @return �����ַ�������
     */
    public String[] stringToArray(String str, String token) {
        String[] ss = new String[0];
        List a = new ArrayList();
        boolean b = false;
        StringTokenizer st = new StringTokenizer(str, token);
        while (st.hasMoreTokens()) {
            a.add(st.nextElement().toString());
        }
        if (a.size() > 0) {
            ss = new String[a.size()];
            for (int i = 0; i < ss.length; i++) {
                ss[i] = a.get(i).toString();
            }
        }
        return ss;
    }

    /**
     * ���������б��еĵ����ַ������Ϊ��ʽ�ַ���
     */
    public String listToString(AbstractList list, String linkop) {
        String s = null;
        String ms = null;
        if (list != null) {
            if (list.size() == 1) {
                s = (String) list.get(0);
            }
            if (list.size() == 2) {
                s = (String) list.get(0) + " " + linkop + " " + (String) list.get(1);
            }
            if (list.size() >= 3) {
                for (int i = 1; i < list.size() - 1; i++) {
                    ms = " " + linkop + " " + (String) list.get(i) + " " + linkop + " ";
                }
                s = (String) list.get(0) + ms + (String) list.get(list.size() - 1);
            }
        }
        return s;
    }

    public String arryToString(Object[] list, String linkop) {
        String s = null;
        if (list != null) {
            if (list.length > 0) {
                s = list[0].toString();
                if (list.length > 1) {
                    for (int i = 1; i < list.length; i++) {
                        s = s + linkop + list[i];
                    }
                }
            }
        }
        return s;
    }

    /**
     * ���ֶ�ֵ���ϵ����ţ������ڲ�������values����
     *
     * @return ����ַ���
     */
    public String arryToValues(Object[] list, String linkop) {
        String s = null;
        if (list != null) {
            if (list.length > 0) {
                s = "'" + list[0].toString() + "'";
                if (list.length > 1) {
                    for (int i = 1; i < list.length; i++) {
                        s = s + linkop + "'" + list[i].toString() + "'";
                    }
                }
            }
        }
        return s;
    }

    public String[] arryToArray(String[] list, String l, String r) {
        if (list == null) {
            return null;
        }
        String s = null;
        for (int i = 0; i < list.length; i++) {
            s = l + list[i] + r;
            list[i] = s;
        }
        return list;
    }

    public String[] intArrayToStrArray(int[] intArray) {
        if (intArray == null) {
            return null;
        }
        String[] str = new String[intArray.length];
        for (int i = 0; i < intArray.length; i++) {
            str[i] = "" + intArray[i];
        }
        return str;
    }

    /*����������һ����־�ļ�*/
    public void exceptionLogging(String logFile, String className,
            Exception exception) {
        Logger logger = Logger.getLogger(className);
        logger.setUseParentHandlers(false);
        FileHandler fh = null;
        try {
            fh = new FileHandler(logFile, 1000000, 1, true);
            fh.setFormatter(new SimpleFormatter());
            logger.addHandler(fh);
            logger.log(Level.INFO, " Uncaused Exception ", exception);
        } catch (SecurityException ex1) {
        } catch (IOException ex1) {
        }
    }

    /**
     * ��33 ��һ���ļ������ֽ�����
     *
     */
    public byte[] getBytesFromFile(File file) throws IOException {
        byte[] s = null;
        InputStream is = new FileInputStream(file);
        long length = file.length();
        if (length > Integer.MAX_VALUE) {
            throw new IOException(file.getName() + " is too large , break.");
        }
        s = new byte[(int) length];
        int offset = 0;
        int numRead = 0;
        while (offset < s.length
                && (numRead = is.read(s, offset, s.length - offset)) >= 0) {
            offset += numRead;
        }
        if (offset < s.length) {
            throw new IOException("Could not completely read file " + file.getName());
        }
        is.close();
        return s;
    }

    /**
     * ��һ���ı��ļ�����ȡ�ַ���
     *
     */
    public String getStringFromFile(File textFile) throws IOException {
        String s = new String(getBytesFromFile(textFile));
        return s;
    }

    /**
     * ��136 ��URL�ж�ȡ�ı�
     *
     */
    public String getStringFromURL(URL url) throws IOException {

        int i = 0;
        String s;
        StringBuilder sb = new StringBuilder();
        InputStream is = url.openStream();

        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        while ((s = in.readLine()) != null) {
            if (!s.endsWith("\r\n")) {
                sb.append(s).append("\r\n");//��ԭ���з�
            } else {
                sb.append(s);
            }
        }
        in.close();

        return sb.toString();
    }

    /**
     * ��ȡͼ���ļ����
     *
     * @param igmFileValue ͼƬ�ļ�����
     * @return
     * @throws java.io.IOException
     *
     */
    public int getIgmWidth(byte[] igmFileValue) throws IOException {
        int width;
        java.awt.Image image = javax.imageio.ImageIO.read(new ByteArrayInputStream(igmFileValue));
        width = image.getWidth(null);
        return width;
    }

    /**
     * ��ȡͼ���ļ����
     *
     * @param igmFileValue ͼƬ�ļ�����
     * @return
     * @throws java.io.IOException
     *
     */
    public int getIgmHeight(byte[] igmFileValue) throws IOException {
        int height;
        java.awt.Image image = javax.imageio.ImageIO.read(new ByteArrayInputStream(igmFileValue));
        height = image.getHeight(null);
        return height;
    }

    /**
     * �ж��ֶ����Ƿ����ֶ�����������
     */
    public boolean isInFields(Object[] fields, Object fieldName) {
        boolean b = false;
        for (int i = 0; i < fields.length; i++) {
            if (fields[i].equals(fieldName)) {
                b = true;
                return b;
            }
        }
        return b;
    }

    /**
     * ƥ��������ʽ
     *
     * @param pattern ������ʽ
     * @param str ��ƥ����ַ���
     * @return ����ƥ����
     */
    public boolean matches(String pattern, String str) {
        return Pattern.matches(pattern, str);
    }

    /**
     * ɾ��ָ����Ŀ¼������������Ŀ¼�����������The Java Developers Almanac 1.4��e30
     *
     * @param dir
     * @return
     */
    public boolean delDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = delDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        // The directory is now empty so delete it
        return dir.delete();
    }

    /**
     * 2014-08-04�޶�<br/> ���ֽ�����д��������ļ�<br/>
     * ע���ļ�����ʱ,�ļ����Բ�����,���ļ���·���������,���򽫷����쳣.<br/>
     *
     * @param file
     * @param buffer
     * @throws java.io.FileNotFoundException
     */
    public void addFile(File file, byte[] buffer) throws FileNotFoundException, IOException {
        InputStream streamIn = new ByteArrayInputStream(buffer);
        FileOutputStream streamOut = new FileOutputStream(file);
        try {
            int bytesRead = 0;
            while ((bytesRead = streamIn.read(buffer)) != -1) {
                streamOut.write(buffer, 0, bytesRead);
            }
            streamOut.flush();
        } finally {
            streamOut.close();
            streamIn.close();
        }
    }

    /**
     * ���ڴ�������ʱ��ǿ�ƽ��ֽ�����д�뵽�����ļ�������Java�����������27
     *
     * @param file
     * @param buffer
     * @throws java.lang.Exception
     */
    public void addFileSync(File file, byte[] buffer) throws Exception {
        FileOutputStream os = new FileOutputStream(file);
        FileDescriptor fd = os.getFD();
        os.write(buffer);
        os.flush();
        fd.sync();
    }

    /**
     *
     * ��ʹ��addFile����
     *
     * @deprecated
     *
     * @param file
     * @param buffer
     * @throws java.io.FileNotFoundException
     */
    public void setBytesToFile(File file, byte[] buffer) throws FileNotFoundException, IOException {
        addFile(file, buffer);
    }

}

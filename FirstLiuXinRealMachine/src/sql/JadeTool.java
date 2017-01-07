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
 * @author 胡开明<br/> 初稿于2005年6月完成；<br/> 用于字符串格式输出；<br/>
 * 这个类中的几个方法，在ProcessVO和WhereString类中频繁调用；<br/> 修订日期：2012-10-17<br/>
 */
public class JadeTool implements java.io.Serializable {

    public JadeTool() {
    }

    /**
     * 将字符串分解成字符串数组
     *
     * @param str 待分解的字符串
     * @param token 分隔符，如："|"、","、"."、"---"、" "等
     * @return 返回字符串数组
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
     * 将存贮在列表中的单个字符串输出为格式字符串
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
     * 给字段值加上单引号，可用于插入语句的values部分
     *
     * @return 输出字符串
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

    /*可用于生成一个日志文件*/
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
     * 例33 将一个文件读入字节数组
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
     * 从一个文本文件中提取字符串
     *
     */
    public String getStringFromFile(File textFile) throws IOException {
        String s = new String(getBytesFromFile(textFile));
        return s;
    }

    /**
     * 例136 从URL中读取文本
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
                sb.append(s).append("\r\n");//还原换行符
            } else {
                sb.append(s);
            }
        }
        in.close();

        return sb.toString();
    }

    /**
     * 提取图象文件宽度
     *
     * @param igmFileValue 图片文件数据
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
     * 提取图象文件宽度
     *
     * @param igmFileValue 图片文件数据
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
     * 判断字段名是否在字段名的数组中
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
     * 匹配正则表达式
     *
     * @param pattern 正则表达式
     * @param str 待匹配的字符串
     * @return 返回匹配结果
     */
    public boolean matches(String pattern, String str) {
        return Pattern.matches(pattern, str);
    }

    /**
     * 删除指定的目录及其下所有子目录。来自年鉴《The Java Developers Almanac 1.4》e30
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
     * 2014-08-04修订<br/> 将字节数组写入二进制文件<br/>
     * 注意文件操作时,文件可以不存在,但文件的路径必须存在,否则将发生异常.<br/>
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
     * 如在处理事务时，强制将字节数组写入到磁盘文件。来自Java开发者年鉴例27
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
     * 请使用addFile方法
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

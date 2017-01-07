package com.djj;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

/**
 * Created by djj on 2017/1/5.
 */

public class MyProperty {
    private static String profilefile;
    private static String outputpath = "d:\\", scanpath = "d:\\", appname = "xscj", dbserverip = "127.0.0.1";
    private static int maxsocket = 100, port = 12702, dbserverport = 9701;
    /**
     * 采用静态方法
     */
    private static Properties props = new Properties();
    private String mainpath = this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();

    public MyProperty() {
        profilefile = getMainPath() + "setup.properties";
    }
    /*static {
        try {
            File file=new File(profilefile);
            if(!file.exists()){
                file.createNewFile();
                System.out.println(file.getCanonicalPath());
            }
            props.load(new FileInputStream(profilefile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            //default();
        } catch (IOException e) {
            //default();
        }
    }*/
    /**
     * 读取属性文件中相应键的值
     * @param key
     *            主键
     * @return String
     */
   /* public static String getKeyValue(String key) {
        return props.getProperty(key);
    }*/

    /**
     * 根据主键key读取主键的值value
     * <p>
     * //@param filePath 属性文件路径
     *
     * @param key 键名
     */
    private static String readValue(String key) {
        Properties props = new Properties();
        try {
            File file = new File(profilefile);
            if (!file.exists()) {
                //file.createNewFile();
                file = null;
                //System.out.println(file.getCanonicalPath());
                writeProperties("port", String.valueOf(port));
                writeProperties("maxsocket", String.valueOf(maxsocket));
                writeProperties("dbserverport", String.valueOf(dbserverport));
                writeProperties("dbserverip", dbserverip);
                writeProperties("outputpath", outputpath);
                writeProperties("scanpath", scanpath);
                writeProperties("appname", appname);
            }
            InputStream in = new BufferedInputStream(new FileInputStream(
                    profilefile));
            props.load(in);
            String value = props.getProperty(key);
            try {
                value = java.net.URLDecoder.decode(value, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            //System.out.println(key +"键的值是："+ value);
            return value;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 更新（或插入）一对properties信息(主键及其键值)
     * 如果该主键已经存在，更新该主键的值；
     * 如果该主键不存在，则插件一对键值。
     *
     * @param keyname  键名
     * @param keyvalue 键值
     */
    private static void writeProperties(String keyname, String keyvalue) {
        try {
            //props.load(new FileInputStream(profilefile));
            // 调用 Hashtable 的方法 put，使用 getProperty 方法提供并行性。
            // 强制要求为属性的键和值使用字符串。返回值是 Hashtable 调用 put 的结果。
            OutputStream fos = new FileOutputStream(profilefile);
            props.setProperty(keyname, keyvalue);
            // 以适合使用 load 方法加载到 Properties 表中的格式，
            // 将此 Properties 表中的属性列表（键和元素对）写入输出流
            props.store(fos, "Update '" + keyname + "' value");
        } catch (IOException e) {
            System.err.println("属性文件更新错误");
        }
    }

    public int getMaxsocket() {
        String res = readValue("maxsocket");
        if (res == null) {
            return maxsocket;
        } else {
            return Integer.parseInt(res);
        }
    }

    public int getPort() {
        String res = readValue("port");
        if (res == null) {
            return port;
        } else {
            return Integer.parseInt(res);
        }
    }

    public int getDbserverport() {
        String res = readValue("dbserverport");
        if (res == null) {
            return dbserverport;
        } else {
            return Integer.parseInt(res);
        }
    }

    public String getScanpath() {
        String res = readValue("scanpath");
        if (res == null) {
            return scanpath;
        } else {
            return res;
        }
    }

    public String getAppname() {
        String res = readValue("appname");
        if (res == null) {
            return appname;
        } else {
            return res;
        }
    }

    public String getDbserverip() {
        String res = readValue("dbserverip");
        if (res == null) {
            return dbserverip;
        } else {
            return res;
        }
    }

    public String getOutputpath() {
        String res = readValue("outputpath");
        if (res == null) {
            return outputpath;
        } else {
            if (res.substring(res.length() - 1).equals(File.separator))
                res = res.substring(0, res.length() - 1);
            return res;
        }
    }

    public String getMainPath() {
        if (File.separator.equals("\\")) {
            mainpath = mainpath.replace("/", File.separator);
            if (!mainpath.equals("")) {
                mainpath = mainpath.substring(1, mainpath.lastIndexOf(File.separator) + 1);
            }
        } else {
            if (!mainpath.equals("")) {
                mainpath = mainpath.substring(0, mainpath.lastIndexOf(File.separator) + 1);
            }
        }
        try {
            mainpath = java.net.URLDecoder.decode(mainpath, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return mainpath;
    }

}

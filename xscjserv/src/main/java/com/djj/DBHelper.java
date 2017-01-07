package com.djj;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

/**
 * Created by djj on 2016/10/31.
 */

public class DBHelper {
    private static final String name = "com.mysql.jdbc.Driver";
    private static final String user = "user";
    private static final String password = "1111";
    private static String url;
    private PreparedStatement stmt = null;
    private ResultSet rs = null;
    private Connection conn = null;

    public DBHelper() {
        //com.mysql.jdbc.Driver b;
        MyProperty myProperty = new MyProperty();
        String ip = myProperty.getDbserverip();
        int port = myProperty.getDbserverport();
        url = "jdbc:mysql://" + ip + ":" +
                String.valueOf(port) + "/test?useSSL=true&verifyServerCertificate=false&connectTimeout=2000&socketTimeout=3000&autoReconnect=true&failOverReadOnly=false&maxReconnects=3";
        try {
            Class.forName(name);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.out.println("database connect error!");
        }

    }

    /*public boolean updatefromxls(ArrayList<MainTable> tables){
        if(!connect()) return false;
        java.util.Date date=new java.util.Date();
        Timestamp inputtime=new Timestamp(date.getTime());
        int rowcount=0;
        try {
            for (MainTable t : tables){
                stmt = conn.prepareStatement("insert main(inputtime,user,num,cnum,name,address,cellphone,phone,year,month,money) values(?,?,?,?,?,?,?,?,?,?,?)");
                stmt.setTimestamp(1, inputtime);
                stmt.setString(2, t.user);
                stmt.setString(3, t.num);
                stmt.setString(4, t.cnum);
                stmt.setString(5, t.name);
                stmt.setString(6, t.address);
                stmt.setString(7, t.cellphone);
                stmt.setString(8, t.phone);
                stmt.setString(9, t.year);
                stmt.setString(10, t.month);
                stmt.setString(11, t.money);
                rowcount+=stmt.executeUpdate();
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        close();
        if (rowcount==tables.size()){
            return true;
        }else {
            return false;
        }

    }*/

    public boolean connect() {

        try {
            //DriverManager.setLoginTimeout(3);
            conn = DriverManager.getConnection(url, user, password);//建立数据库连接
            return true;
            //conn.setNetworkTimeout();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /*public void test() {
        connect();
        try {
            PreparedStatement stmt1 = conn.prepareStatement("select id from tmp where id=1");
            ResultSet rs1 = stmt1.executeQuery();
            rs1.first();
            if (rs1.getInt(1) == 1) {
                close();
                rs1.close();
                stmt1.close();
                System.out.println("Maintain database connection success");
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        close();
        System.out.println("Maintain database connection failed");
        return;
    }*/


    /*public  void testblob(){
        int j;
        try {byte[] buffer1=new byte[10];
            for (int i=0;i<10;i++){
                buffer1[i]=(byte) (i+40);
            }
            byte[] buffer2=new byte[20];
            for (int i=0;i<20;i++){
                buffer2[i]=(byte) (i+50);
            }

            DataInputStream in=new DataInputStream(new ByteArrayInputStream(buffer1));
            stmt = conn.prepareStatement("insert tmp(photo) values(?)");
            stmt.setBinaryStream(1,in,in.available());
            in=new DataInputStream(new ByteArrayInputStream(buffer2));
            //System.out.print(buffer.length);
            stmt.setBinaryStream(1,in,in.available());
            j=stmt.executeUpdate();
            System.out.print(j);
            //ByteArrayOutputStream i=new ByteArrayOutputStream();


            //rs = stmt.executeQuery(sql);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }*/
    public int inputdatabase(MainTable table){
        try {
            stmt = conn.prepareStatement("insert main(inputtime,user,num,cnum,name,address,cellphone,phone,year,month,money) values(?,?,?,?,?,?,?,?,?,?,?)");
            stmt.setTimestamp(1, table.inputtime);
            stmt.setString(2, table.user);
            stmt.setString(3, table.num);
            stmt.setString(4, table.cnum);
            stmt.setString(5, table.name);
            stmt.setString(6, table.address);
            stmt.setString(7, table.cellphone);
            stmt.setString(8, table.phone);
            stmt.setString(9, table.year);
            stmt.setString(10, table.month);
            stmt.setString(11, table.money);
            return stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            return -2;
        }
    }

    public ArrayList<MainTable> downloaddata(String user){
        ArrayList<MainTable> mlist=new ArrayList<>();
        try {
            stmt = conn.prepareStatement("select id,inputtime,user,num,cnum,name,address,cellphone,phone,year,month,money from main where user=? and uploadtime is null order by inputtime");
            stmt.setString(1,user);
            rs = stmt.executeQuery();
            //判断 rs 非空
            if(!rs.next()) return mlist;
            rs.beforeFirst();
            while (!rs.isLast()) {
                rs.next();
                MainTable table =new MainTable();
                table.id=rs.getInt(1);
                table.inputtime=rs.getTimestamp(2);
                table.user=rs.getString(3);
                table.num=rs.getString(4);
                table.cnum=rs.getString(5);
                table.name=rs.getString(6);
                table.address=rs.getString(7);
                table.cellphone=rs.getString(8);
                table.phone=rs.getString(9);
                table.year=rs.getString(10);
                table.month=rs.getString(11);
                table.money=rs.getString(12);
                mlist.add(table);
            }
            return mlist;
        } catch (Exception e) {
            e.printStackTrace();
            mlist.clear();
            return mlist;
        }
    }

    public boolean setdownloadtime(ArrayList<MainTable> mlist){
        Timestamp downloadtime=new Timestamp(System.currentTimeMillis());
        try {
            stmt = conn.prepareStatement("update main set downloadtime=? where id=?");
            for (MainTable table : mlist){
                stmt.setTimestamp(1, downloadtime);
                stmt.setInt(2, table.id);
                /*if(stmt.executeUpdate()<0){
                   continue;
                }*/
                stmt.executeUpdate();
            }
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean uploaddata(MainTable table, ArrayList<String> filepaths){
        Timestamp uploadtime=new Timestamp(System.currentTimeMillis());
        try {
                stmt = conn.prepareStatement("update main set uploadtime=?,filenums=?,imei=? where id=?");
                stmt.setTimestamp(1, uploadtime);
                stmt.setInt(2, table.filenums);
            stmt.setString(3,table.imei);
            stmt.setInt(4,table.id);
                if(stmt.executeUpdate()<0){
                    return false;
                }

            for(String s : filepaths){
                stmt = conn.prepareStatement("insert file(mid,path) values(?,?)");
                stmt.setInt(1,table.id);
                stmt.setString(2,s);
                if(stmt.executeUpdate()<0){
                    return false;
                }
            }
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean canceldata(ArrayList<MainTable> tables){
        Timestamp uploadtime=new Timestamp(System.currentTimeMillis());
        try {
            for(MainTable table : tables){
                stmt = conn.prepareStatement("update main set uploadtime=?,filenums=?,imei=? where id=?");
                stmt.setTimestamp(1, uploadtime);
                stmt.setInt(2, 0);
                stmt.setString(3,table.imei);
                stmt.setInt(4,table.id);
                if(stmt.executeUpdate()<0){
                    return false;
                }
            }
            return true;


        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public String getPassword(String username){
        try {
            stmt = conn.prepareStatement("select password from user where username=?");
            stmt.setString(1, username);
            rs = stmt.executeQuery();
            if (!rs.next()) return "this user not exist";
            rs.first();
            return rs.getString(1);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public int writedatabase(String ip, String text, byte[] in) {
        DataInputStream photo = new DataInputStream(new ByteArrayInputStream(in));
        try {
            stmt = conn.prepareStatement("insert tmp(ip,text,photo) values(?,?,?)");
            stmt.setString(1, ip);
            stmt.setString(2, text);
            stmt.setBinaryStream(3, photo, photo.available());
            return stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            return -2;
        }
    }

    public void readdatabase(String path) {
        try {
            stmt = conn.prepareStatement("select text,photo from tmp");
            //stmt.setInt(1,3);
            rs = stmt.executeQuery();
            int i = 0;

            while (!rs.isLast()) {
                i++;
                rs.next();
                String[] patharray = rs.getString(1).split("/");
                String pathtmp = patharray[patharray.length - 1];
                Blob bb = rs.getBlob(2);
                File file = new File(path);
                if (!file.exists()) file.mkdir();
                file = new File(path + "\\" + pathtmp);
                DataInputStream inputStream = new DataInputStream(bb.getBinaryStream());
                //file = new File(path + "\\" + i + ".jpg");
                FileOutputStream fout = new FileOutputStream(file);
                byte[] b = new byte[2048];
                int length;
                while ((length = inputStream.read(b, 0, b.length)) > 0) {
                    System.out.println("收到文件，长度为" + length);
                    fout.write(b, 0, length);
                    fout.flush();
                }
                fout.close();
                inputStream.close();
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
        close();
        //return rs;
    }

    public void close() {
        try {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

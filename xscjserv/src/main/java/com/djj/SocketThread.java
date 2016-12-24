package com.djj;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by djj on 2016/11/4.
 */

public class SocketThread extends Thread {
    private static final int FTEXT = -11, FPHOTO = -12, FUPDATE = -13, FFINISHED = -14,
            UPDATESUCCESS = -21, UPDATEFAULT = -22, DATEBASEERROR = -23, NETWORKSTART = -41;
    private DBHelper dbHelper;
    private Socket socket;
    private boolean isfinished = false;
    private String text;
    private int photolength;
    private byte[] photo;
    private DataInputStream in;
    private DataOutputStream out;
    private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    //private ByteArrayInputStream photoin;

    public SocketThread(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        super.run();
        int field;
        this.dbHelper = new DBHelper();
        try {
            socket.setSoTimeout(10000);
            in = new DataInputStream(this.socket.getInputStream());
            out = new DataOutputStream(this.socket.getOutputStream());
            String s = "正在接收数据...";
            out.writeUTF(s);
            out.writeInt(NETWORKSTART);
            out.flush();
            System.out.println(df.format(new Date()) + ":" + "IP地址为" + socket.getInetAddress().toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (!isfinished) {
            try {
                field = in.readInt();
                switch (field) {
                    case FTEXT:
                        System.out.println(df.format(new Date()) + ":" + "接收text");
                        text = in.readUTF();
                        break;
                    case FPHOTO:
                        photolength = in.readInt();
                        System.out.println(df.format(new Date()) + ":" + "接收照片，长度" + String.valueOf(photolength));
                        photo = new byte[photolength];
                        //ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
                        /*byte[] b=new byte[2048];
                        int length;
                        int pos=0;
                        int l=2048;*/
                        for (int j = 0; j < photolength; j++)
                            photo[j] = (byte) in.read();
                        /*while ((length = in.read(b,0,b.length)) > 0) {
                            System.out.println("姝ｅ湪鎺ユ敹鏁版嵁..." + length);
                            for(int i=0;i<length;i++){
                                photo[pos+i]=b[i];
                            }
                            pos+=length;
                            if ((photolength-pos) < 1) l=photolength-pos-1;
                            //swapStream.write(b,0,length);
                        }*/

                        //photo=swapStream.toByteArray();
                        //in.read(photo);
                        break;
                    case FUPDATE:
                        System.out.println(df.format(new Date()) + ":" + "更新数据库");
                        if (updatedb() > 0)
                            System.out.println(df.format(new Date()) + ":" + "数据库更新成功");
                        break;
                    case FFINISHED:
                        finish("success finished");
                        break;
                    default:
                        break;
                }

            } catch (IOException e) {
                e.printStackTrace();
                finish("error");
                break;
            }
        }
        text = null;
        photo = null;
        try {
            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private int updatedb() {
        int r = -1;
        try {
            if (dbHelper.connect()) {
                if ((r = dbHelper.writedatabase(socket.getInetAddress().toString(), text, photo)) > 0) {
                    out.writeInt(UPDATESUCCESS);
                } else {
                    out.writeInt(UPDATEFAULT);
                }
                dbHelper.close();
            } else {
                out.writeInt(DATEBASEERROR);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        text = null;
        photo = null;
        return r;
    }

    public void finish(String s) {

        isfinished = true;
        System.out.println(df.format(new Date()) + ":" + this.getName() + ":" + s);
    }

    public boolean getfinished() {
        return isfinished;
    }

 /*       lob b=new Blob();
        ByteArrayOutputStream ba=new ByteArrayOutputStream();
        ByteArrayInputStream input=new ByteArrayInputStream();
        input.available();
        File f= new File("c:\ttt.txt");
        FileInputStream fi=new FileInputStream(f);
        switch (field){
            case FTEXT :
        }
        System.out.println(df.format(new Date()) + "-" + s.getInetAddress().toString().substring(1) + ":" + text);
        sql = "insert tmp(text) values(?)";
        dbHelper.dosql(sql, text);
        sqlread = "select * from tmp";
        rs = dbHelper.readsql(sqlread);
        try
        {
            while (rs.next()) {
                System.out.println(rs.getString("text"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/
}

package com.djj;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by djj on 2016/11/4.
 */

public class SocketThread extends Thread {
    private static final int FTEXT = -11, FPHOTO = -12, FUPDATE = -13, FFINISHED = -14,
            UPDATESUCCESS = -21, UPDATEFAULT = -22, DATEBASEERROR = -23, NETWORKSTART = -41;
    //private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final String appname = "xscj";
    private DBHelper dbHelper;
    private Socket socket;
   // private boolean isfinished = false;
    private DataInputStream in;
    private DataOutputStream out;
    private String username, password;
    //private final String flag_success = "success";
    //private ByteArrayInputStream photoin;

    public SocketThread(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        super.run();
        if (init()) {
            String s = "正在接收数据...";
            System.out.println(s + "IP地址为" + socket.getInetAddress().toString());
        } else {
            finish("init error");
        }
        String s = getid();
        if (s.equals("this user not exist") || s.equals("error")) {
            finish(s);
        } else {
            s = "用户" + s + "验证通过...";
            System.out.println(s);
        }
        ;

        String command;

        //while (!isfinished) {
        try {
            command = in.readUTF();
        } catch (IOException e) {
            e.printStackTrace();
            command = "command read IO error";
        }
        switch (command) {
            case "downloaddata":
                finish(downloaddata());
                break;
            case "uploaddata":
                finish(uploaddata());
                break;
            case "canceldata":
                finish(canceldata());
                break;
            case "command read IO error":
                finish(command);
                break;
            default:
                finish("command is error");
                break;
        }
    }

    private boolean init() {
        this.dbHelper = new DBHelper();
        dbHelper.connect();
        try {
            socket.setSoTimeout(10000);
            in = new DataInputStream(this.socket.getInputStream());
            out = new DataOutputStream(this.socket.getOutputStream());
            if (in.readUTF().equals(appname)) {
                out.writeUTF("application pass");
                out.flush();
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private String getid() {
        try {
            username = in.readUTF();
            password = in.readUTF();
            String res_user = dbHelper.getPassword(username);
            if (res_user.equals("this user not exist")) {
                out.writeUTF(res_user);
                out.flush();
                return res_user;
            }
            if (res_user.equals(password)) {
                out.writeUTF("user pass");
                out.flush();
                return username;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "error";
    }

    private String downloaddata() {
        ArrayList<MainTable> tables;
        DateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        try {
            tables = dbHelper.downloaddata(username);
            out.writeInt(tables.size());
            out.flush();
            for (MainTable table : tables) {
                out.writeInt(table.id);
                out.writeUTF(sdf.format(table.inputtime));
                out.writeUTF(table.user);
                out.writeUTF(table.num);
                out.writeUTF(table.cnum);
                if (table.name==null) table.name="";
                out.writeUTF(table.name);
                if (table.address==null) table.address="";
                out.writeUTF(table.address);
                if (table.cellphone==null) table.cellphone="";
                out.writeUTF(table.cellphone);
                if (table.phone==null) table.phone="";
                out.writeUTF(table.phone);
                out.writeUTF(table.year);
                out.writeUTF(table.month);
                out.writeUTF(table.money);
                out.flush();
            }
            String s;
            if ((s=in.readUTF()).equals("downloaddata pass")) {
                if (dbHelper.setdownloadtime(tables)) {
                    return "downloaddata success";
                } else {
                    return "database setdownloadtime error";
                }
            } else if(s.equals("no new item has downloaded")){
                return s;
            } else            {
                return "clients has not received the downloaddata ";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "downloaddata IO error";
    }

    private String uploaddata() {
        Date now = new Date();
        DateFormat sdf = new SimpleDateFormat("yyyy-MM");
        String spath = sdf.format(now);
        String filename;
        int filelength;
        String filepath;
        try {
            int tablenums = in.readInt();
            for (int i = 0; i < tablenums; i++) {
                MainTable table = new MainTable();
                table.id = in.readInt();
                table.user=in.readUTF();
                table.imei = in.readUTF();
                table.filenums = in.readInt();
                System.out.println("filenums="+table.filenums);
                ArrayList<String> filepaths = new ArrayList<>();
                for (int j = 0; j < table.filenums; j++) {
                    filename = in.readUTF();
                    filename.replace("/", "").replace("\\", "");
                    filepath = MainService.outputpath + File.separator + table.user + File.separator + spath;
                    File path = new File(filepath);
                    path.mkdirs();

                    File file = new File(filepath, filename);
                    FileOutputStream fos = new FileOutputStream(file);
                    filelength = in.readInt();
                    //byte[] filebuf=new byte[filelength];
                    for (int k = 0; k < filelength; k++)
                        fos.write(in.read());
                    fos.flush();
                    fos.close();
                    filepaths.add(file.getCanonicalPath());
                }
                if (dbHelper.uploaddata(table, filepaths)) {
                    out.writeUTF("update item success");
                    out.flush();
                } else {
                    return "database uploaddata error";
                }
            }
            if(in.readUTF().equals("uploaddata pass")){
                return "uploaddata success";
            }else{
                return "clients uploadata error";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "uploaddata IO error";
    }

    private String canceldata() {
        ArrayList<MainTable> tables=new ArrayList<>();
        try {
            int tablenums = in.readInt();
            for (int i = 0; i < tablenums; i++) {
                MainTable table = new MainTable();
                table.id = in.readInt();
                table.imei = in.readUTF();
                tables.add(table);
            }

                if (dbHelper.canceldata(tables)) {
                    return "canceldata success";
                } else {
                    return "database canceldata error";
                }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return "canceldata IO error";
    }


   /* private int updatedb() {
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
    }*/

    private void finish(String s) {
        System.out.println(s);
        try {
            if (!socket.isClosed()) {
                out.writeUTF(s);
                out.flush();
                out.close();
                in.close();
                socket = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (dbHelper != null) {
            dbHelper.close();
            dbHelper = null;
        }

        /*try{
            if(in!=null){
                in.close();
                in=null;
            }
            if(out!=null){
                out.close();
                out=null;
            }
        }catch (IOException e){
            e.printStackTrace();*/


        //isfinished = true;
        //System.out.println(df.format(new Date()) + ":" + this.getName() + ":" + s);
    }


    /*public boolean getfinished() {
        return isfinished;
    }*/

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

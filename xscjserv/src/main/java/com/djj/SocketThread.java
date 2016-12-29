package com.djj;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static java.awt.SystemColor.text;

/**
 * Created by djj on 2016/11/4.
 */

public class SocketThread extends Thread {
    private static final int FTEXT = -11, FPHOTO = -12, FUPDATE = -13, FFINISHED = -14,
            UPDATESUCCESS = -21, UPDATEFAULT = -22, DATEBASEERROR = -23, NETWORKSTART = -41;
    private DBHelper dbHelper;
    private Socket socket;
    private boolean isfinished = false;
    private DataInputStream in;
    private DataOutputStream out;
    //private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final String appname="xscj";
    private String username,password;
    //private ByteArrayInputStream photoin;

    public SocketThread(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        super.run();
        if (init()){
            String s = "正在接收数据...";
            System.out.println(s+"IP地址为" + socket.getInetAddress().toString());
        }else{
            failed();
        };
        if (getid()){
            String s = "用户验证通过...";
            System.out.println(s);
        }else{
            failed();
        };

        String command;

        while (!isfinished) {
            try {
                command = in.readUTF();
                switch (command) {
                    case "downloaddata":
                        if (downloaddata()){
                            String s = "客户端下载数据成功...";
                            System.out.println(s);
                            finish("downloaddata");
                        }else{
                            failed(downloaddata);
                        };
                        break;
                    case "uploaddata":
                        if (uploaddata()){
                            String s = "客户端上传数据成功...";
                            System.out.println(s);
                            finish("uploaddata");
                        }else{
                            failed();
                        };
                        break;
                    case "canceldata":
                        if (canceldata()){
                            String s = "客户端删除数据成功...";
                            System.out.println(s);
                            finish("canceldata");
                        }else{
                            failed();
                        };
                        break;
                    default:
                        failed();
                        break;
                }

            } catch (IOException e) {
                e.printStackTrace();
                failed();
                break;
            }
        }
        failed();
        try {
            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private boolean init(){
        this.dbHelper = new DBHelper();
        dbHelper.connect();
        try {
            socket.setSoTimeout(10000);
            in = new DataInputStream(this.socket.getInputStream());
            out = new DataOutputStream(this.socket.getOutputStream());
            if(in.readUTF().equals(appname)) {
                out.writeUTF("application pass");
                out.flush();
            }
            return true;
        }catch (SocketException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }
        return false;
    }
    private boolean getid(){
        try {
            username=in.readUTF();
            password=in.readUTF();
            if(dbHelper.getPassword(username).equals(password)){
                out.writeUTF("user pass");
                out.flush();
            }
            return true;
        }catch (IOException e){
            e.printStackTrace();
        }
        return false;
    }
    private boolean downloaddata(){
        ArrayList<MainTable> tables;
        DateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        try {
            tables=dbHelper.downloaddata(username);
            out.writeInt(tables.size());
            out.flush();
            for (MainTable table : tables){
                out.writeInt(table.id);
                out.writeUTF(sdf.format(table.inputtime));
                out.writeUTF(table.user);
                out.writeUTF(table.num);
                out.writeUTF(table.cnum);
                out.writeUTF(table.name);
                out.writeUTF(table.address);
                out.writeUTF(table.cellphone);
                out.writeUTF(table.phone);
                out.writeUTF(table.year);
                out.writeUTF(table.month);
                out.writeUTF(table.money);
                out.flush();
            }
            if(in.readUTF().equals("downloaddata pass")){
                if(dbHelper.setdownloadtime(tables)){
                    return true;
                }else {
                    return false;
                }
            }else {
                return false;
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        return false;
    }
    private boolean uploaddata(){
        Date now=new Date();
        DateFormat sdf = new SimpleDateFormat("yyyy-MM");
        String spath=sdf.format(now);
        String filename;
        int filelength;
        String filepath;
        try {
            int tablenums = in.readInt();
            for (int i = 0; i < tablenums; i++) {
                MainTable table = new MainTable();
                table.id = in.readInt();
                table.user = in.readUTF();
                table.imei = in.readUTF();
                table.filenums = in.readInt();
                ArrayList<String> filepaths = new ArrayList<>();
                for (int j = 0; i < table.filenums; j++) {
                    filename = in.readUTF();
                    filename.replace("/", "").replace("\\", "");
                    filepath = MainService.outputpath + File.separator + table.user + File.separator + spath ;
                    File path = new File(filepath);
                    path.mkdirs();

                    File file = new File(filepath , filename);
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
                    return true;
                } else {
                    return false;
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        return false;
    }
    private boolean canceldata(){
        try {
            int tablenums = in.readInt();
            for (int i = 0; i < tablenums; i++) {
                MainTable table = new MainTable();
                table.id = in.readInt();
                table.imei = in.readUTF();

                if (dbHelper.canceldata(table)) {
                    return true;
                } else {
                    return false;
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        return false;
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

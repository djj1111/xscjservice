package com.djj;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

public class MainService {
    private static final int PORT = 12702, MAXSOCKET = 100;
    private static boolean close = false;
    private static String mainpath="d:\\fuck";
    static String outputpath="d:\\fuck";

    public static void main(String[] args) throws IOException {
        ExecutorService fixedThreadPool = Executors.newFixedThreadPool(MAXSOCKET);
        ServerSocket server = new ServerSocket(PORT);


        new Thread(new Runnable() {
            //boolean updatefinished=true;
            @Override
            public void run() {
                /*for(String s:getfilepath()){
                    ArrayList<MainTable> mainTables=readExcel(mainpath+s);
                    if (mainTables.isEmpty()) {
                        System.out.println(""+Calendar.getInstance().getTimeInMillis());
                        File file=new File(mainpath,s);
                        file.renameTo(new File(mainpath,"文件格式错，请确认为excel03文件，或打开保存再试"+ Calendar.getInstance().getTimeInMillis()));
                    }
                }*/
                while (!close){
                    //updatefinished=false;
                    inputdatabase();
                    //updatefinished=true;
                    try{
                        Thread.sleep(3000);
                    }catch (InterruptedException e){
                        e.printStackTrace();
                    }
                }

            }
        }).start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                Scanner sc = new Scanner(System.in);
                boolean stop = false;
                while (!stop) {
                    String com = sc.nextLine();
                    if (com.equalsIgnoreCase("exit")) {
                        fixedThreadPool.shutdown();
                        close = true;
                        //dbconnect_thread.interrupt();
                        try {
                            Socket socket = new Socket("127.0.0.1", PORT);
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        sc.close();
                        stop = true;
                        //close();
                    }
                }
            }
        }).start();
        while (!close) {
            try {
                fixedThreadPool.execute(new SocketThread(server.accept()));
            } catch (RejectedExecutionException e) {
            }
        }
    }

    private static void inputdatabase() {
        ArrayList<String> paths = new ArrayList<>();
        try {
            Runtime e = Runtime.getRuntime();
            Process process = e.exec("cscript d:\\tttt.vbs \""+mainpath+ "\"");
            process.waitFor();
        }catch (IOException e){
            e.printStackTrace();
        }
        catch (InterruptedException e){
            e.printStackTrace();
        }

            //注意exec是另一个进程

            File path=new File(mainpath);
            File[] files=path.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    if(name.lastIndexOf('.')>0)
                    {
                        // get last index for '.' char
                        int lastIndex = name.lastIndexOf('.');

                        // get extension
                        String str = name.substring(lastIndex);

                        // match path name extension
                        if(str.equals(".djj"))
                        {
                            return true;
                        }
                    }
                    return false;
                }
            });
            for (int i=0;i<files.length;i++){
                System.out.println(files[i].getAbsolutePath());
                fileToDatabase(files[i]);
            }
            /*InputStreamReader isr = new InputStreamReader(is,"GBK");
            BufferedReader br = new BufferedReader(isr);
            String line = null;
            while (true) {
                *//*String line1= new String(br.readLine().getBytes("GBK"),"ISO-8859-1");
                line=new String(line1.getBytes("ISO-8859-1"),"UTF-8");*//*
                line=br.readLine();
                System.out.println(line);
                if (!line.contains(".xls"))  return paths;
                paths.add(line);
            }*/


    }

    private static boolean fileToDatabase(File file) {
        //ArrayList<MainTable> mlist = new ArrayList<>();
        //SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
        DBHelper dbHelper=new DBHelper();
        Timestamp inputtime=new Timestamp(System.currentTimeMillis());
        try {
            /*//同时支持Excel 2003、2007
            File excelFile = new File(filepath); //创建文件对象
            FileInputStream is = new FileInputStream(excelFile); //文件流
            Workbook workbook = WorkbookFactory.create(is); //这种方式 Excel 2003/2007/2010 都是可以处理的
            int sheetCount = workbook.getNumberOfSheets();  //Sheet的数量
            //遍历每个Sheet*/
            FileInputStream is = new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(is,"GBK");
            BufferedReader br = new BufferedReader(isr);
            if (!br.readLine().equals("站点\t用户号\t册本号\t户名\t地址\t上次抄码\t水量\t本次抄码\t抄表员\t抄表周期\t催缴员\t抄表年\t抄表月\t欠费金额\t违约金\t欠费笔数\t手机\t联系电话")){
                br.close();
                isr.close();
                is.close();
                file.renameTo(new File(file.getPath(),"文件格式错"+ Calendar.getInstance().getTimeInMillis()));
                return false;
            }
            if (!dbHelper.connect()) return false;
            do{
                String s=br.readLine();
                if (s==null) break;
                MainTable table = new MainTable();
                //split后为空的不进入数组，必须让其非空
                s=s.replace("\t","\t$$$$$");
                System.out.println(s);
                String[] as=s.split("\t");
                System.out.println(""+as.length);
                if (as.length!=18) continue;
                table.inputtime=inputtime;
                if(!as[1].equals("$$$$$"))
                table.num = as[1].substring(5);
                if(!as[2].equals("$$$$$"))
                table.cnum = as[2].substring(5);
                table.user = table.cnum.substring(0, 2) + table.cnum.substring(5, 8);
                if(!as[3].equals("$$$$$"))
                table.name = as[3].substring(5);
                if(!as[4].equals("$$$$$"))
                table.address = as[4].substring(5);
                if(!as[11].equals("$$$$$"))
                table.year = as[11].substring(5);
                if(!as[12].equals("$$$$$"))
                table.month = as[12].substring(5);
                if(!as[13].equals("$$$$$"))
                table.money = as[13].substring(5);
                if(!as[16].equals("$$$$$"))
                table.cellphone = as[16].substring(5);
                if(!as[17].equals("$$$$$"))
                table.phone = as[17].substring(5);
                //mlist.add(table);
                System.out.println(table.money);
                if (dbHelper.inputdatabase(table)<0) {
                    System.out.println("数据库导入错误！");
                    continue;
                }
                //inputdatabase
            }
            while(true);
            br.close();
            isr.close();
            is.close();
            file.delete();
            dbHelper.close();
            dbHelper=null;
            return true;
            /*for (int s = 0; s < sheetCount; s++) {
                Sheet sheet = workbook.getSheetAt(s);
                int rowCount = sheet.getPhysicalNumberOfRows(); //获取总行数
                //遍历每一行

                for (int r = 1; r < rowCount; r++) {
                    Row row = sheet.getRow(r);
                    int cellCount = row.getPhysicalNumberOfCells(); //获取总列数
                    //遍历每一列
                    *//*for (int c = 0; c < cellCount; c++) {
                        Cell cell = row.getCell(c);
                        int cellType = cell.getCellType();
                        String cellValue = null;
                        switch(cellType) {
                            case Cell.CELL_TYPE_STRING: //文本
                                cellValue = cell.getStringCellValue();
                                break;
                            case Cell.CELL_TYPE_NUMERIC: //数字、日期
                                if(DateUtil.isCellDateFormatted(cell)) {
                                    cellValue = fmt.format(cell.getDateCellValue()); //日期型
                                }
                                else {
                                    cellValue = String.valueOf(cell.getNumericCellValue()); //数字
                                }
                                break;
                            case Cell.CELL_TYPE_BOOLEAN: //布尔型
                                cellValue = String.valueOf(cell.getBooleanCellValue());
                                break;
                            case Cell.CELL_TYPE_BLANK: //空白
                                cellValue = cell.getStringCellValue();
                                break;
                            case Cell.CELL_TYPE_ERROR: //错误
                                cellValue = "错误";
                                break;
                            case Cell.CELL_TYPE_FORMULA: //公式
                                cellValue = "错误";
                                break;
                            default:
                                cellValue = "错误";
                        }*//*

                }
                //System.out.println();
            }*/
        } catch (IOException e) {
            e.printStackTrace();
            dbHelper.close();
            file.delete();
            return false;
        }
    }

}

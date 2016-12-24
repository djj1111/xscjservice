package com.djj;


import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

public class MainService {
    static final int PORT = 12702, MAXSOCKET = 100;
    static boolean close = false;

    public static void main(String[] args) throws IOException {
        /*System.out.println("Default Charset=" + Charset.defaultCharset());
        System.out.println("file.encoding=" + System.getProperty("file.encoding"));
        System.out.println("Default Charset in Use=" + getDefaultCharSet());
        String t = "测试字符...";
        System.out.println(t);
        String utf8 = new String(t.getBytes("UTF-8"));
        System.out.println(utf8);
        String unicode = new String(utf8.getBytes(), "UTF-8");
        System.out.println(unicode);
        String gbk = new String(unicode.getBytes("GBK"));

        System.out.println(gbk);*/
        ExecutorService fixedThreadPool = Executors.newFixedThreadPool(MAXSOCKET);
        ServerSocket server = new ServerSocket(PORT);
        /*
        * 保持MYSQL定期连接，防止休眠。
        * */
        Thread dbconnect_thread = new Thread(new Runnable() {
            @Override
            public void run() {
                DBHelper dbHelper = new DBHelper();
                while (!close) {
                    dbHelper.test();
                    try {
                        Thread.sleep(4 * 60 * 60 * 1000);
                    } catch (InterruptedException e) {

                    }
                }
                dbHelper = null;
            }
        });
        dbconnect_thread.start();
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
                        dbconnect_thread.interrupt();
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

    private ArrayList<MainTable> readExcel(String filepath) {
        ArrayList<MainTable> mlist = new ArrayList<>();
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
        try {
            //同时支持Excel 2003、2007
            File excelFile = new File(filepath); //创建文件对象
            FileInputStream is = new FileInputStream(excelFile); //文件流
            Workbook workbook = WorkbookFactory.create(is); //这种方式 Excel 2003/2007/2010 都是可以处理的
            int sheetCount = workbook.getNumberOfSheets();  //Sheet的数量
            //遍历每个Sheet
            for (int s = 0; s < sheetCount; s++) {
                Sheet sheet = workbook.getSheetAt(s);
                int rowCount = sheet.getPhysicalNumberOfRows(); //获取总行数
                //遍历每一行

                for (int r = 1; r < rowCount; r++) {
                    Row row = sheet.getRow(r);
                    int cellCount = row.getPhysicalNumberOfCells(); //获取总列数
                    //遍历每一列
                    /*for (int c = 0; c < cellCount; c++) {
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
                        }*/
                    MainTable table = new MainTable();
                    table.num = row.getCell(1).getStringCellValue();
                    table.cnum = row.getCell(2).getStringCellValue();
                    table.user=table.cnum.substring(0,2)+table.cnum.substring(5,8);
                    table.name = row.getCell(3).getStringCellValue();
                    table.address = row.getCell(4).getStringCellValue();
                    table.year = row.getCell(11).getStringCellValue();
                    table.month = row.getCell(12).getStringCellValue();
                    table.money = row.getCell(13).getStringCellValue();
                    table.cellphone = row.getCell(16).getStringCellValue();
                    table.phone = row.getCell(17).getStringCellValue();
                    mlist.add(table);
                }
                //System.out.println();
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

        return mlist;
    }
}

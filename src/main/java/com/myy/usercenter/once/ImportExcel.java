package com.myy.usercenter.once;


import com.alibaba.excel.EasyExcel;
import java.util.List;

/**
 * 导入 Excel
 */
public class ImportExcel {
    public static void main(String[] args) {

        String fileName = "E:\\新建文件夹\\user-center-backend-master\\src\\main\\resources\\prodExcel.xlsx";
        // listenerRead(fileName);
        synchronousRead(fileName);
    }

    public static void listenerRead(String fileName) {
        // 这里 需要指定读用哪个class去读，然后读取第一个sheet 文件流会自动关闭
        // 这里每次会读取100条数据 然后返回过来 直接调用使用数据就行
        EasyExcel.read(fileName, UserTableInfo.class, new UserTableInfoListener()).sheet().doRead();
    }

    public static void synchronousRead(String fileName) {
        // 这里 需要指定读用哪个class去读，然后读取第一个sheet 同步读取会自动finish
        List<UserTableInfo> list = EasyExcel.read(fileName).head(UserTableInfo.class).sheet().doReadSync();
        for (UserTableInfo data : list) {
            System.out.println(data);
        }
    }

}
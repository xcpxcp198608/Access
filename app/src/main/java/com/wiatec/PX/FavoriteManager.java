package com.wiatec.PX;

import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by xuchengpeng on 24/04/2017.
 */

public class FavoriteManager {

    private static final String FAVORITE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath()
            +"/Android/data/org.xbmc.kodi/files/.kodi/userdata/";
    private static final String TEMP_PATH = Environment.getExternalStorageDirectory().getAbsolutePath()
            +"/Android/data/";
    private static  final String FILE_NAME = "favourites.xml";

    public static void backup(){
        copyFile(FAVORITE_PATH , TEMP_PATH);
    }

    public static void restore(){
        if(copyFile(TEMP_PATH ,FAVORITE_PATH)){
            deleteFile(TEMP_PATH , FILE_NAME);
        }
    }

    private static boolean copyFile (String filePath , String targetFilePath){
        File file = new File(filePath , FILE_NAME);
        File targetFile = new File(targetFilePath , FILE_NAME);
        boolean flag = false;
        if(!file.exists()){
            Toast.makeText(Application.getContext() , "Favorite file is not exists" , Toast.LENGTH_LONG).show();
            return flag;
        }
        if(targetFile.exists()){
            targetFile.delete();
        }else {
            File targetDir = new File(TEMP_PATH);
            if(!targetDir.exists()){
                targetDir.mkdir();
            }
        }
        FileOutputStream fileOutputStream = null;
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(file);
            fileOutputStream = new FileOutputStream(targetFile);
            byte [] buffer = new byte[1024];
            int length = -1;
            while ((length = fileInputStream.read(buffer)) != -1 ){
                fileOutputStream.write(buffer , 0 , length);
            }

            Toast.makeText(Application.getContext() , "Operation success" , Toast.LENGTH_LONG).show();
            flag = true;
        } catch (IOException e) {
            e.printStackTrace();
            flag = false;
            Toast.makeText(Application.getContext() , "Operation failure , please try again" , Toast.LENGTH_LONG).show();
        }finally {
            try {
                if(fileOutputStream != null) {
                    fileOutputStream.close();
                }
                if(fileInputStream != null) {
                    fileInputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return flag;
    }

    private static void deleteFile (String filePath , String fileName) {
        File file = new File(filePath , fileName);
        if(file.exists()){
            file.delete();
        }
    }


}

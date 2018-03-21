package com.wiatec.PX;

import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Created by xuchengpeng on 24/04/2017.
 */

public class FavoriteManager {

    private static final String FAVORITE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath()
            +"/Android/data/org.xbmc.kodi/files/.kodi/userdata/favourites.xml";
    private static final String TEMP_PATH = Environment.getExternalStorageDirectory().getAbsolutePath()
            +"/Android/favourites.xml";
    private static  final String FILE_NAME = "favourites.xml";

    public static void backup(){
        deleteFile(TEMP_PATH);
        copyFile(FAVORITE_PATH, TEMP_PATH);
    }

    public static void restore(){
        if(copyFile(TEMP_PATH, FAVORITE_PATH)){
            deleteFile(TEMP_PATH);
        }
    }

    private static boolean copyFile (String filePath , String targetFilePath){
        File file = new File(filePath);
        File targetFile = new File(targetFilePath);
        boolean flag = false;
        if(!file.exists()){
            //Toast.makeText(Application.getContext() , "Favorite file is not exists" , Toast.LENGTH_LONG).show();
            return flag;
        }

        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(file);
            FileUtils.copyInputStreamToFile(fileInputStream, targetFile);
            flag = true;
        } catch (IOException e) {
            flag = false;
            //Toast.makeText(Application.getContext() , "Operation failure , please try again" , Toast.LENGTH_LONG).show();
        }finally {
            try {
                if(fileInputStream != null) {
                    fileInputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


//        if(!targetFile.exists()){
//            File targetDir = new File(targetFilePath);
//            if(!targetDir.exists()){
//                targetDir.mkdir();
//            }
//            targetFile = new File(targetDir, FILE_NAME);
//        }
//        FileOutputStream fileOutputStream = null;
//        FileInputStream fileInputStream = null;
//        try {
//            fileInputStream = new FileInputStream(file);
//            fileOutputStream = new FileOutputStream(targetFile);
//            byte [] buffer = new byte[1024];
//            int length = -1;
//            while ((length = fileInputStream.read(buffer)) != -1 ){
//                fileOutputStream.write(buffer , 0 , length);
//            }
//
//            //Toast.makeText(Application.getContext() , "Operation success" , Toast.LENGTH_LONG).show();
//            flag = true;
//        } catch (IOException e) {
//            e.printStackTrace();
//            flag = false;
//            //Toast.makeText(Application.getContext() , "Operation failure , please try again" , Toast.LENGTH_LONG).show();
//        }finally {
//            try {
//                if(fileOutputStream != null) {
//                    fileOutputStream.close();
//                }
//                if(fileInputStream != null) {
//                    fileInputStream.close();
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
        return flag;
    }

    private static void deleteFile (String filePath) {
        try {
            File file = new File(filePath);
            FileUtils.forceDelete(file);
        } catch (IOException e) {
            Log.d("file", e.getLocalizedMessage());
        }
    }


}

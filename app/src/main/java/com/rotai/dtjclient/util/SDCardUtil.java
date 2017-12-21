package com.rotai.dtjclient.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * SD卡操作工具类
 */
public class SDCardUtil {


    /**
     *
     * @param context     上下文
     * @param fileName    指定的文件名
     * @param videoName   指定的资源名
     * @return            返回的资源路径
     */

     public static String getFilePath(Context context, String fileName, String videoName) {
         //判断是否挂载SDCard
         if(android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {

             File file = new File("/mnt/sdcard");

             if (file.exists()) {

                 File filePath = new File(file+"/"+fileName);

                 if (filePath.exists()){

                     File mp4Name = new File(filePath+"/"+videoName);

                     return mp4Name.getPath() + ".mp4";

                 }else {

                     Toast.makeText(context, "文件不存在", Toast.LENGTH_SHORT).show();
                 }

             } else {

                 Toast.makeText(context, "SDCard不存在", Toast.LENGTH_SHORT).show();
             }
             return null;
         }else {

             Toast.makeText(context, "SDCard不存在", Toast.LENGTH_SHORT).show();
         }

         return null;
    }




    /**
     * 读取sd卡上指定后缀的所有文件
     *
     * @param files    返回的所有文件
     * @param filePath 路径(可传入sd卡路径)
     * @param suffere  后缀名称 比如 .gif
     * @return
     */
    public static List<File> getSuffixFile(List<File> files, String filePath, String suffere) {

        File f = new File(filePath);

        if (!f.exists()) {
            return null;
        }

        File[] subFiles = f.listFiles();
        for (File subFile : subFiles) {
            if(subFile.isFile() && subFile.getName().endsWith(suffere)){
                files.add(subFile);
            } else if(subFile.isDirectory()){
                getSuffixFile(files, subFile.getAbsolutePath(), suffere);
            } else{
                //非指定目录文件 不做处理
            }

        }

        return files;
    }



    /**
     *  获取扩展存储路径，TF卡、U盘
     *
     * @return  返回所有的扩展存储路径
     */

    public static String getExternalStorageDirectory(){
        String dir = new String();
        try {
            Runtime runtime = Runtime.getRuntime();
            Process proc = runtime.exec("mount");
            InputStream is = proc.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            String line;
            BufferedReader br = new BufferedReader(isr);
            while ((line = br.readLine()) != null) {
                if (line.contains("secure")) continue;
                if (line.contains("asec")) continue;

                if (line.contains("fat")) {
                    String columns[] = line.split(" ");
                    if (columns != null && columns.length > 1) {
                        dir = dir.concat(columns[1] + "\n");
                    }
                } else if (line.contains("fuse")) {
                    String columns[] = line.split(" ");
                    if (columns != null && columns.length > 1) {
                        dir = dir.concat(columns[1] + "\n");
                    }
                }
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return dir;
    }


    /**
     * ================================获取所有的mp4格式的视频================================
     *
     * @param fileList    路径容器
     * @param filePath    要获取的资源路径
     * @param suffere     要获取的资源后缀
     * @return            返回资源集合
     */
    public static List<String> getAllVideoFilePath(List<File> fileList, String filePath, String suffere) {

        List<String> files = null;
        List<File> mp4List = SDCardUtil.getSuffixFile(fileList, filePath, suffere);
        if (mp4List.size() != 0) {
            files = new ArrayList<>();
            for (File file : mp4List) {
                files.add(file.getPath());
            }

        }

        return files;
    }

    /**
     * 获取所有的apk文件
     */
    public static List<String> getAllAPKFilePath(List<File> fileList, String filePath, String suffere) {

        List<String> files = null;
        List<File> APKList = SDCardUtil.getSuffixFile(fileList, filePath, suffere);
        if (APKList.size() != 0) {
            files = new ArrayList<>();
            for (File file : APKList) {
                files.add(file.getPath());
            }

        }

        return files;
    }






    /**
     * ================================ 获取视频资源 ================================
     *
     */
    public static void getVideoResource(Context context){
        ContentResolver contentResolver=context.getContentResolver();
        Uri uri= MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        String[] projection={
                MediaStore.Video.Media.DISPLAY_NAME,//视频在sd卡中的名称
                MediaStore.Video.Media.DURATION,//视频时长
                MediaStore.Video.Media.SIZE,//视频文件的大小
                MediaStore.Video.Media.DATA,//视频的绝对路径
                MediaStore.Video.Media.ARTIST//艺术家
        };
        Cursor cursor=contentResolver.query(uri,projection,null,null,null);
    }

}
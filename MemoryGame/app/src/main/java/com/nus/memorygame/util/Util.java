package com.nus.memorygame.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.Toast;

import com.nus.memorygame.model.ImageFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;

public class Util {
    public static String createDirectory(String path, String dirName) {

        if(path == null || path.isEmpty()) {
            return null;
        }

//        if(dirName == null || dirName.isEmpty())
//            path += Const.IMAGES_DIR;

        File file = new File(path);
        if(file.isDirectory())
            file.mkdirs();

        return file.getPath();
    }

    public static void showToast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    private static File[] getAllFilesInDir(String path, Context context) {
        File fileDir = null;
        if (path == null || path.isEmpty()) {
            fileDir = context.getFilesDir();
        } else
            fileDir = new File(path);

        return fileDir.listFiles();
    }

    public static ArrayList<ImageFile> getImages(String path, Context context) {
        File[] files = getAllFilesInDir(path, context);
        ArrayList<ImageFile> images = new ArrayList<>(files.length);
        if (files.length == 0) return images;
        for (File file : files) {
            try {
                if(file == null || !file.isFile() || !file.getName().endsWith(".jpg")) continue;
                Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(file));
                if(bitmap != null)
                    images.add(new ImageFile(file.getName(), bitmap));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        return images;
    }

    public static ArrayList<ImageFile> getImagesByFileNames(ArrayList<String> fileNames, Context context) {
        String basePath = context.getFilesDir().getPath();
        ArrayList<ImageFile> images = new ArrayList<>(fileNames.size());
        for(String fileName : fileNames) {
            String path = basePath + "/" + fileName;
            File file = new File(path);
            try {
                if(!file.isFile() || !file.getName().endsWith(".jpg")) continue;
                Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(file));
                if(bitmap != null)
                    images.add(new ImageFile(file.getName(), bitmap));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        return images;
    }

}

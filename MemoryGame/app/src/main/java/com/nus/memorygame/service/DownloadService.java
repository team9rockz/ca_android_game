package com.nus.memorygame.service;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;

import com.nus.memorygame.util.Const;
import com.nus.memorygame.util.Util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class DownloadService extends IntentService {

    private boolean done = false;

    public DownloadService() {
        super("Download Images");
    }

    @Override
    protected void onHandleIntent(final Intent intent) {
        executeDownload(intent);
    }

    private void executeDownload(Intent intent) {
        ArrayList<String> imageUrls = retrieveHtml(intent.getStringExtra(Const.URL));

        if(imageUrls == null) return;

        emptyDir();
        String basePath = Util.createDirectory(getFilesDir().getPath(), null);

        String fileName = "";
        InputStream inputStream = null;
        FileOutputStream fos = null;
        BufferedInputStream bis = null;
        for(int i=0; i<imageUrls.size(); i++) {
            publishResponse("", true, i+1, imageUrls.size());
            String imgUrl = imageUrls.get(i);
            fileName = System.currentTimeMillis()+".jpg";

            try {
                URL url = new URL(imgUrl);
                inputStream = url.openConnection().getInputStream();
                byte[] buffer = new byte[1024];
                bis = new BufferedInputStream(inputStream);
                fos = new FileOutputStream(basePath + "/" + fileName);
                int read = -1;
                while ((read = inputStream.read(buffer)) != -1) {
                    fos.write(buffer, 0, read);
                }
                done = true;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if(bis != null)
                        bis.close();
                    if(fos != null)
                        fos.close();
                    if(inputStream != null)
                        inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        publishResponse(basePath, false, 0, imageUrls.size());
    }

    private void publishResponse(String basePath, boolean isProgress, int num, int total) {
        Intent intent = new Intent(Const.DOWNLOAD_PROGRESS);
        intent.putExtra("base_path", basePath);
        intent.putExtra("num", num);
        intent.putExtra("total", total);
        intent.putExtra("progress", isProgress);
        intent.putExtra("status", done);
        intent.putExtra("Result", Activity.RESULT_OK);
        sendBroadcast(intent);
    }

    private void stopBroadcast() {
        Intent intent = new Intent(Const.STOP_PROGRESS);
        sendBroadcast(intent);
    }

    private ArrayList<String> retrieveHtml(String link) {
        URL url = null;
        InputStream inputStream  = null;
        BufferedInputStream bis = null;
        OutputStream fos = null;
        try {
            url = new URL(link);
            inputStream = url.openConnection().getInputStream();
            byte[] buffer = new byte[1024];
            bis = new BufferedInputStream(inputStream);
            fos = new FileOutputStream(getFilesDir() + "/index.html");
            int read = -1;
            while ((read = inputStream.read(buffer)) != -1) {
                fos.write(buffer, 0, read);
            }

            return readHtml();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if(fos != null)
                    fos.close();
                if(bis != null)
                    bis.close();
                if(inputStream != null)
                    inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    private ArrayList<String> readHtml() {
        ArrayList<String> images = new ArrayList<>(Const.MAX_IMAGES);
        File file = new File(getFilesDir() + "/index.html");
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            int count = 0;
            String line = br.readLine();
            while (line != null) {
                if(line.contains("<img") && line.contains(".jpg")) {
                    String[] strings = line.split(" ");
                    for(String st : strings) {
                        if(!st.contains("src=")) continue;
                        String str = st.split("=")[1].replaceAll("\"", "");
                        images.add(str);
                        count++;
                        break;
                    }

                    if(count == Const.MAX_IMAGES) break;
                }
                line = br.readLine();
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return images;
    }

    private void emptyDir() {
        File dir = getFilesDir();
        File[] files = dir.listFiles();
        for(int i=0; i<files.length; i++) {
            if(files[i].isFile()) {
                files[i].delete();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopSelf();
        stopBroadcast();
    }

}

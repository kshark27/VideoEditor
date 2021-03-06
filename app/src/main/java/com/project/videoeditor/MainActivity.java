package com.project.videoeditor;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import android.provider.OpenableColumns;
import android.view.View;

import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import com.project.videoeditor.activity.MainEditor;
import com.project.videoeditor.codecs.ActionEditor;
import com.project.videoeditor.support.UtilUri;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;



public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_TAKE_GALLERY_VIDEO = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    private boolean checkPermissions(){

        if(isExternalStorageReadable() || isExternalStorageWriteable() || isMICReadable()){
            //Toast.makeText(this, "Внешнее хранилище не доступно", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }
    // проверяем, доступно ли внешнее хранилище для чтения и записи
    public boolean isExternalStorageWriteable(){
        
        return  ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED;
    }
    // проверяем, доступно ли внешнее хранилище хотя бы только для чтения
    public boolean isExternalStorageReadable(){

        return  ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED;
    }
    public boolean isMICReadable(){

        return  ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED;
    }
    private void getPermission() {
        String[] params = null;
        String writeExternalStorage = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        String readExternalStorage = Manifest.permission.READ_EXTERNAL_STORAGE;
        String readMIC = Manifest.permission.RECORD_AUDIO;

        int hasWriteExternalStoragePermission = ActivityCompat.checkSelfPermission(this, writeExternalStorage);
        int hasReadExternalStoragePermission = ActivityCompat.checkSelfPermission(this, readExternalStorage);
        int hasReadMIC = ActivityCompat.checkSelfPermission(this, readMIC);
        List<String> permissions = new ArrayList<String>();

        if (hasWriteExternalStoragePermission != PackageManager.PERMISSION_GRANTED)
            permissions.add(writeExternalStorage);
        if (hasReadExternalStoragePermission != PackageManager.PERMISSION_GRANTED)
            permissions.add(readExternalStorage);
        if (hasReadMIC != PackageManager.PERMISSION_GRANTED)
            permissions.add(readMIC);

        if (!permissions.isEmpty()) {
            params = permissions.toArray(new String[permissions.size()]);
        }

        if (params != null && params.length > 0) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    params,
                    100);
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_TAKE_GALLERY_VIDEO:
                    VideoInfo info = new VideoInfo();
                    Uri selectedVideoUri = data.getData();
                    String ffmpegPath;
                    if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                        ffmpegPath = UtilUri.safUriToFFmpegPath(this,selectedVideoUri);
                    else
                        ffmpegPath = UtilUri.getPath(this,selectedVideoUri);

                    String displayName = UtilUri.getInfoByUri(this,selectedVideoUri,OpenableColumns.DISPLAY_NAME);
                    String filesize = UtilUri.getInfoByUri(this,selectedVideoUri,OpenableColumns.SIZE);

                    info.setFilename(displayName);
                    info.setSizeInBytes(Long.parseLong(filesize));
                    info.parseInfoFromPath(ffmpegPath);
                    ActionEditor.setVideoInfo(info);
                    Intent intent = new Intent(this, MainEditor.class);
                    intent.putExtra(MainEditor.EDIT_VIDEO_ID,info);
                    startActivity(intent);
                    break;

            }
        }
    }

    private void UploadVideo()
    {
        try {
            Intent intent = new Intent();
            intent.setType("video/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Video"), REQUEST_TAKE_GALLERY_VIDEO);
        } catch (Exception e) {

            e.printStackTrace();
        }
    }
    public void ClickUploadVideo(View view) {
        if (!checkPermissions())
                    getPermission();
        else
            UploadVideo();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        try {
            trimCache(this);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    public static void trimCache(Context context) {
        try {
            File dir = context.getCacheDir();
            if (dir != null && dir.isDirectory()) {
                deleteDir(dir);
            }
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }

        // The directory is now empty so delete it
        return dir.delete();
    }

}

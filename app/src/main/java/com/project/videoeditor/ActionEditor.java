package com.project.videoeditor;

import android.content.Intent;

import com.arthenica.mobileffmpeg.FFmpeg;


public class ActionEditor {

    private static FFmpeg ffmpeg;
    private VideoInfo videoInf;

    public ActionEditor() {


    }
    public static void EncodeProcess(String codec,String filePath,String new_filePath) throws Exception {
        switch (codec)
        {
            case "MPEG4":
            {
                String command = "-y -i " + filePath + " -c:v mpeg4 "+new_filePath;
                FFmpeg.execute(command);
                break;
            }
            default:
            {
                throw new Exception("Неизвестный кодек");
            }

        }
    }
    public static void GetFrameProcess()
    {
        
    }

}

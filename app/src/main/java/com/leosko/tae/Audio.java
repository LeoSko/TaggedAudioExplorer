package com.leosko.tae;

import android.os.AsyncTask;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by LeoSko on 25.05.2015.
 */
public class Audio
{
    private File file;
    private int bpm = 0;
    private static final char[] BPM_PATTERN = { 0x54, 0x42, 0x50, 0x4D, 0x00, 0x00, 0x00,/* length, differs: 0x07, 0x00, 0x00, 0x01, 0xFF, 0xFE*/};
    private static final String BPM_STR = new String(BPM_PATTERN);

    Audio(File filepath)
    {
        file = filepath;
        resolveBPM();
    }

    private void resolveBPM()
    {
        try
        {
            FileReader fr = new FileReader(file);
            char[] buffer = new char[8096];
            fr.read(buffer, 0, 8096);
            String temp = new String(buffer);
            int idx = temp.indexOf(BPM_STR);
            if (idx != -1)
            {
                //we found it!
                int i = 13;
                int res = 0;
                while (buffer[idx + i] >= '0' && buffer[idx + i] <= '9')
                {
                    res *= 10;
                    res += (buffer[idx + i] - '0');
                    i += 2;
                }
                bpm = res;
            }
            fr.close();
        }
        catch (Exception e)
        {
            return;
        }
    }

    public File getFile()
    {
        return file;
    }

    public void setFile(File file)
    {
        this.file = file;
    }

    public int getBpm()
    {
        return bpm;
    }

    public void setBpm(int bpm)
    {
        this.bpm = bpm;
    }
}

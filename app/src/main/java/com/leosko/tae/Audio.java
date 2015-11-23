package com.leosko.tae;

import android.os.AsyncTask;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
    private static final char[] BPM_L = { 0x05, 0x07, 0x09 };

    Audio(File filepath)
    {
        file = filepath;
        resolveBPM();
    }

    public boolean hasBPMtag()
    {
        return (bpm != 0);
    }

    public void setNewBPM(int new_bpm)
    {
        try
        {
            String src = file.getAbsolutePath();
            String dst = src + "_temp";
            InputStream in = new FileInputStream(src);
            OutputStream out = new FileOutputStream(dst);

            // Transfer bytes from in to out
            byte[] buf = new byte[8096];
            // first time read only title of file and change bpm value
            int len;
            len = in.read(buf);
            String temp = new String(buf);
            StringBuilder sb = new StringBuilder(temp);
            int idx = temp.indexOf(BPM_STR);
            if (idx != -1)
            {
                int new_cnt = String.valueOf(new_bpm).length();
                sb.setCharAt(idx + 7, BPM_L[new_cnt - 1]);
                int cnt = String.valueOf(bpm).length();
                sb.delete(idx + 13, idx + 13 + cnt * 2);
                for (int i = 0; i < new_cnt; i++)
                {
                    sb.insert(idx + 13, '\0');
                    sb.insert(idx + 13, String.valueOf(new_bpm % 10));
                    new_bpm /= 10;
                }
            }
            // write new bpm into file
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(sb.toString().getBytes(), 0, sb.length());
            baos.writeTo(out);
            while ((len = in.read(buf)) > 0)
            {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
            file.delete();
            File ff = new File(dst);
            ff.renameTo(new File(src));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
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

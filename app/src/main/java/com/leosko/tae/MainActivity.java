package com.leosko.tae;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity
{
    private Context cntxt;
    private AudioListViewAdapter alva;
    private List<Audio> audios;
    private String curDir;
    private static DirectoryChooserDialog dcd;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        audios = new ArrayList<Audio>();
        setContentView(R.layout.activity_main);
        cntxt = getApplicationContext();
        alva = new AudioListViewAdapter(cntxt, R.layout.audio_item, audios);
        ListView lv = (ListView) findViewById(R.id.listView);
        lv.setAdapter(alva);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                Intent i = new Intent();
                i.setAction(Intent.ACTION_VIEW);
                i.setDataAndType(Uri.fromFile(alva.getItem(position).getFile()), "audio/mp3");
                startActivity(i);
            }
        });
        dcd = new DirectoryChooserDialog(MainActivity.this, new DirectoryChooserDialog.ChosenDirectoryListener()
        {
            @Override
            public void onChosenDir(String chosenDir)
            {
                alva.clear();
                File f = new File(chosenDir);
                File[] mp3files = f.listFiles(new FileFilter()
                {
                    @Override
                    public boolean accept(File pathname)
                    {
                        return pathname.getPath().endsWith(".mp3");
                    }
                });
                for (File file: mp3files)
                {
                    alva.add(new Audio(file));
                }
                alva.sort(new Comparator<Audio>()
                {
                    @Override
                    public int compare(Audio lhs, Audio rhs)
                    {
                        return lhs.getBpm() - rhs.getBpm();
                    }
                });
            }
        });
        Button btn = (Button) findViewById(R.id.changeDirBtn);
        btn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                dcd.chooseDirectory(curDir);
            }
        });
        // we don't need "new folder" button, whoaoaa
        dcd.setNewFolderEnabled(false);
        curDir = Environment.DIRECTORY_MUSIC;
        dcd.chooseDirectory(curDir);
    }
}

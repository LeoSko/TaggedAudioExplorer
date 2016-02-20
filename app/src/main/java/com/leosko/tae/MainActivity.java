package com.leosko.tae;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.Formatter;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.security.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class MainActivity extends AppCompatActivity
{
    public static final String BPM_AUTO_DEFAULT = "...";
    public static final String PLAYLIST_DIR = Environment.getExternalStorageDirectory().toString() + "/" + Environment.DIRECTORY_MUSIC;
    public static final String PLAYLIST_PATH = PLAYLIST_DIR + "/tae.m3u8";
    private Context cntxt;
    private AudioListViewAdapter alva;
    private List<Audio> audios;
    private String curDir;
    private static DirectoryChooserDialog dcd;
    private static SharedPreferences sp;
    private MyChosenDirectoryListener mcdl = new MyChosenDirectoryListener();
    private MyOnItemLongClickListener moilcl = new MyOnItemLongClickListener();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        audios = new ArrayList<Audio>();
        setContentView(R.layout.activity_main);
        cntxt = getApplicationContext();
        alva = new AudioListViewAdapter(cntxt, R.layout.audio_item, audios);
        // check for new launch and set default folder
        sp = cntxt.getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);
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
        lv.setOnItemLongClickListener(moilcl);
        dcd = new DirectoryChooserDialog(MainActivity.this, mcdl);
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
        curDir = sp.getString(getString(R.string.dir), Environment.DIRECTORY_MUSIC);
        if (!sp.contains(getString(R.string.dir)))
        {
            dcd.chooseDirectory(curDir);
        }
        else
        {
            mcdl.onChosenDir(curDir);
        }
    }

    private class MyChosenDirectoryListener implements DirectoryChooserDialog.ChosenDirectoryListener
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
                Audio a = new Audio(file);
                alva.add(a);
                //now resolve duration for each file
                MediaPlayer mp = MediaPlayer.create(getApplicationContext(), Uri.parse(file.getAbsolutePath()));
                a.setDuration(mp.getDuration());
                mp.release();
            }
            //now turn off from adding to m3u8 playlist the ones that already here
            File playlist = new File(PLAYLIST_PATH);
            //read it
            try
            {
                FileReader fr = new FileReader(playlist);
                char[] buffer = new char[8096];
                fr.read(buffer, 0, 8096);
                String temp = new String(buffer);
                String[] paths = temp.split("\r\n");
                for (String path : paths)
                {
                    //now go through audios to check if they are in the list
                    for (int i = 0; i < alva.getCount(); i++)
                    {
                        Audio a = alva.getItem(i);
                        String p = a.getFile().getAbsolutePath().substring(PLAYLIST_DIR.length() + 1);
                        if (path.equals(p))
                        {
                            a.setInPlaylist(true);
                            break;
                        }
                    }
                }
                fr.close();
            }
            catch (Exception e)
            {
                e.printStackTrace();
                return;
            }
            finally
            {
                //we finally checked them all, now sort and show
                alva.sort(new Comparator<Audio>()
                {
                    @Override
                    public int compare(Audio lhs, Audio rhs)
                    {
                        return lhs.getBpm() - rhs.getBpm();
                    }
                });
                curDir = chosenDir;
                sp.edit().putString(getString(R.string.dir), chosenDir).commit();
            }
        }
    }

    private class MyOnItemLongClickListener implements AdapterView.OnItemLongClickListener
    {
        private EditText bpmet;
        private TextView bpmautotv;
        private Button addToPlaylistBtn;

        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
        {
            final Audio a = alva.getItem(position);
            if (!a.hasBPMtag())
            {
                Toast.makeText(MainActivity.this, getString(R.string.err_no_bpm_tag), Toast.LENGTH_LONG).show();
                return true;
            }
            final AlertDialog dialog = new AlertDialog.Builder(MainActivity.this).create();
            LayoutInflater li = LayoutInflater.from(MainActivity.this);
            final View v = li.inflate(R.layout.edit_bpm_dialog, null);
            dialog.setView(v);
            bpmet = (EditText) v.findViewById(R.id.bpm_manual);
            bpmet.setText(String.valueOf(a.getBpm()));

            addToPlaylistBtn = (Button) v.findViewById(R.id.m3u_add_btn);
            addToPlaylistBtn.setEnabled(a.isInPlaylist() == false);
            addToPlaylistBtn.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    try
                    {
                        // write to the end of file new track
                        File playlist = new File(PLAYLIST_PATH);
                        String src = playlist.getAbsolutePath();
                        OutputStream out = new FileOutputStream(src, true);
                        String toWrite = a.getFile().getAbsolutePath().substring(PLAYLIST_DIR.length() + 1) + "\r\n";
                        byte s[] = toWrite.getBytes();
                        out.write(s);
                        out.close();
                        addToPlaylistBtn.setText("Ok");
                        addToPlaylistBtn.setEnabled(false);
                        a.setInPlaylist(true);
                        alva.notifyDataSetChanged();
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            });
            bpmautotv = (TextView) v.findViewById(R.id.bpm_auto);
            bpmautotv.setText(BPM_AUTO_DEFAULT);
            v.findViewById(R.id.cancel_btn).setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    dialog.dismiss();
                }
            });
            v.findViewById(R.id.ok_btn).setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    a.setNewBPM(Integer.parseInt(bpmet.getText().toString()));
                    dialog.dismiss();
                    mcdl.onChosenDir(curDir);
                }
            });
            v.findViewById(R.id.tap_btn).setOnClickListener(new View.OnClickListener()
            {
                private LinkedList<Long> clicks = new LinkedList<Long>();
                private final int BPM_CLICKS_NEEDED = 5;

                @Override
                public void onClick(View v)
                {
                    clicks.add(System.currentTimeMillis());
                    if (clicks.size() > BPM_CLICKS_NEEDED)
                    {
                        clicks.removeFirst();
                        Long[] dl = new Long[clicks.size() - 1];
                        for (int i = 0; i < clicks.size() - 1; i++)
                        {
                            dl[i] = clicks.get(i + 1) - clicks.get(i);
                        }
                        long res = 0;
                        for (int i = 0; i < dl.length; i++)
                        {
                            res += dl[i];
                        }
                        res /= dl.length;
                        bpmautotv.setText(String.valueOf(Math.round(60 / ((double) (res) / 1000))));
                    }
                    else
                    {
                        bpmautotv.setText(".." + String.valueOf(BPM_CLICKS_NEEDED - clicks.size()));
                    }
                }
            });
            v.findViewById(R.id.copy_btn).setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    bpmet.setText(bpmautotv.getText());
                }
            });
            dialog.show();
            return true;
        }
    }
}

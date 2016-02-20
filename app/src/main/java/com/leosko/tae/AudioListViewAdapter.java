package com.leosko.tae;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.leosko.tae.Audio;
import com.leosko.tae.R;

import java.util.List;

/**
 * Created by LeoSko on 25.05.2015.
 */
public class AudioListViewAdapter extends ArrayAdapter<Audio>
{
    private Context mContext;
    private int id;
    private List<Audio> items;

    public AudioListViewAdapter(Context context, int resource, List objects)
    {
        super(context, resource, objects);
        mContext = context;
        id = resource;
        items = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View mView = convertView;
        if(mView == null)
        {
            LayoutInflater vi = (LayoutInflater)mContext.getSystemService(mContext.LAYOUT_INFLATER_SERVICE);
            mView = vi.inflate(id, null);
        }

        TextView text = (TextView) mView.findViewById(R.id.fileNameText);
        TextView bpmtext = (TextView) mView.findViewById(R.id.bpmText);
        TextView duration = (TextView) mView.findViewById(R.id.durationText);
        final CheckBox inPlaylist = (CheckBox) mView.findViewById(R.id.inPlaylist);

        if (items.get(position) != null)
        {
            Audio a = items.get(position);
            int mins = a.getDuration() / 60000;
            int secs = (a.getDuration() - mins * 60000) / 1000;
            duration.setText(String.format("%02d:%02d", mins,  secs));
            inPlaylist.setEnabled(true);
            inPlaylist.setChecked(a.isInPlaylist());
            /*inPlaylist.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
                @Override
                public void onCheckedChanged(CompoundButton buttonView,
                                             boolean isChecked) {
                    inPlaylist.setChecked(!isChecked);
                }
            });*/

            text.setText(a.getFile().getName());
            if (a.getBpm() > 0)
            {
                bpmtext.setText(String.valueOf(a.getBpm()));
            }
            else
            {
                bpmtext.setText("N/A");
            }
        }

        return mView;
    }
}

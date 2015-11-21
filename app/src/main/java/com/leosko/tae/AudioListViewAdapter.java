package com.leosko.tae;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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

        if (items.get(position) != null)
        {
            Audio a = items.get(position);
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

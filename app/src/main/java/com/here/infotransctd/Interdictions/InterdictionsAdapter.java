package com.here.infotransctd.Interdictions;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.here.infotransctd.R;
import java.util.List;

public class InterdictionsAdapter extends BaseAdapter
{
    private final Context context;
    private final List<Interdiction> list;

    public InterdictionsAdapter(Context context, List<Interdiction> list)
    {
        this.context = context;
        this.list = list;
    }

    @Override
    public int getCount()
    {
        return list.size();
    }

    @Override
    public Object getItem(int position)
    {
        return list.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        Interdiction interdiction = list.get(position);
        View layout = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.interdictions, null);

        try
        {
            ((TextView)layout.findViewById(R.id.txtDescription)).
                    setText(interdiction.getDescription() + " entre " +
                            interdiction.getOrigin().getStreet().split(",")[0] + " e " +
                            interdiction.getDestination().getStreet().split(",")[0] + " até "
                            + interdiction.getEndDate().split(",")[0] + " GMT");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return layout;
    }
}
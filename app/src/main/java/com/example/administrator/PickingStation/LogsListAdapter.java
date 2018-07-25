package com.example.administrator.PickingStation;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

public class LogsListAdapter extends BaseAdapter {
    private Context mContext;
    private LayoutInflater mInflater;
    private ArrayList<LogListObject> mDataSource;

    public LogsListAdapter(Context context, ArrayList<LogListObject> items) {
        mContext = context;
        mDataSource = items;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mDataSource.size();
    }

    @Override
    public Object getItem(int position) {
        return mDataSource.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = mInflater.inflate(R.layout.listitem_log, parent, false);

        TextView characteristicName = rowView.findViewById(R.id.log_listItem_textField);
        ProgressBar percentBar = rowView.findViewById(R.id.log_listItem_progressbar);


        LogListObject item = (LogListObject) getItem(position);

        characteristicName.setText(item.getCharacteristic() + ": " + item.getNumberWithThisCharacteristic());
        percentBar.setMax(100);
        percentBar.setProgress(item.getPercent());

        return rowView;
    }

}

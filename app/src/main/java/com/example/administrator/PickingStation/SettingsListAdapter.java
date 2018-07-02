package com.example.administrator.PickingStation;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.ArrayList;

public class SettingsListAdapter extends BaseAdapter {
    private Context mContext;
    private LayoutInflater mInflater;
    private ArrayList<SettingObject> mDataSource;

    public SettingsListAdapter(Context context, ArrayList<SettingObject> items) {
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

        View rowView = mInflater.inflate(R.layout.listitem_settings, parent, false);
        TextView setting = (TextView) rowView.findViewById(R.id.settingsList_textView_setting);
        setting.setText(mDataSource.get(position).setting);

        TextView value = (TextView) rowView.findViewById(R.id.settingsList_textView_value);
        if(mDataSource.get(position).value.equals("")) {
            value.setTextColor(Color.parseColor("#ff0d00"));
            value.setText("Not set");
        } else {
            value.setTextColor(Color.parseColor("#000000"));
            try {
                JSONObject JSONparser = new JSONObject(mDataSource.get(position).value);
                String ip = JSONparser.getString("ip");
                String port = JSONparser.getString("port");
                value.setText(ip + " : " + port);
            } catch (Exception jsonExc) {
                Log.e("JSON Exception", jsonExc.getMessage());
            }
        }

        return rowView;
    }

}

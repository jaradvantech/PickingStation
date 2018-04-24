package com.example.administrator.Display4;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;


public class Alarms extends Fragment {

    private AlertDialog.Builder builder;
    private AlarmListAdapter adapter;
    private int lastSelectedItem = 0;
    private TextView infoOutput;
    private ArrayList<AlarmObject> theAlarms;
    private ListView mListView;

    public Alarms() {
    }


    public static Alarms newInstance(String param1, String param2) {
        Alarms fragment = new Alarms();
        Bundle args = new Bundle();

        return fragment;
    }


    public void onCreate(LayoutInflater inflater, ViewGroup container,
                         Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_alarms, container, false);

        TextView textView_recent = (TextView) view.findViewById(R.id.alarms_textView_recent);
        infoOutput = (TextView) view.findViewById(R.id.alarms_textView_output);

        /*LIST VIEW*****************************************/
        mListView = (ListView) view.findViewById(R.id.alarms_ListView);
        theAlarms = new ArrayList<>();

        //Set adapter
        adapter = new AlarmListAdapter(getActivity(), theAlarms);
        mListView.setAdapter(adapter);


        builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.Warning));
        builder.setMessage(getString(R.string.Doyoureallywanttodeletethisalarm));
        //menu buttons listener
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch(which){
                    case DialogInterface.BUTTON_POSITIVE:
                            theAlarms.remove(lastSelectedItem);
                            adapter.notifyDataSetChanged();
                     break;

                    case DialogInterface.BUTTON_NEGATIVE:
                    break;
                }
            }
        };
        builder.setPositiveButton(getString(R.string.Delete), dialogClickListener);
        builder.setNegativeButton(getString(R.string.Cancel), dialogClickListener);



        //User interaction: Delete Alarm
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                lastSelectedItem = position;
                AlertDialog dialog = builder.create();
                dialog.setIcon(R.mipmap.warning);
                dialog.show();
                return true;
             }
        });


        //User interaction: Select Alarm
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AlarmObject selectedAlarm = theAlarms.get(position);
                infoOutput.setText("\n");

                infoOutput.append(getString(R.string.Alarmoftype) + " " + selectedAlarm.getType() + "\n");
                infoOutput.append(getString(R.string.Source)+ " " + selectedAlarm.getSource() + "\n");
                infoOutput.append(getString(R.string.Raisedat) + " "  +selectedAlarm.getTime() + "\n");
                infoOutput.append(getString(R.string.Reason));

                infoOutput.append(getString(R.string.Longpresstodismiss));
            }
        });

        //Easter egg: delete all alarms by clicking on "recent alarms"
        textView_recent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                theAlarms.clear();
                adapter.notifyDataSetChanged();
                infoOutput.setText("");
            }
        });


        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void updateAlarms(ArrayList<AlarmObject> newAlarms){
        //Append new alarms to alarm list
        theAlarms.addAll(newAlarms);

        //Sort alarms by decreasing timestamp, so new ones are on top
        //theAlarms.sort(new AlarmObject.SortByDate);

        //Update listview
        adapter.notifyDataSetChanged();
    }


    public interface OnFragmentInteractionListener {
        void onSendCommand(String command);
    }
}


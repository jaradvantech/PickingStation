package com.example.administrator.PickingStation;



import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Random;


public class Logs extends Fragment {

    private final int GRADES = 0;
    private final int COLORS = 1;
    private Logs.OnFragmentInteractionListener mListener;
    private View view;
    private TextView from, to, title;
    private ListView results;
    private ImageButton searchButton;
    private int fromYear, fromMonth, fromDay, toYear, toMonth, toDay;
    private ArrayList<LogListObject> resultsArray = new ArrayList<LogListObject>();
    private LogsListAdapter resultListAdapter;
    private int sortBy = GRADES;
    private JSONArray qtyByColor;
    private JSONArray qtyByGrade;
    private int totalForPeriod;

    public Logs() {
    }

    @Override
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
        view = inflater.inflate(R.layout.fragment_logs, container, false);

        from = view.findViewById(R.id.logs_textView_fromDate);
        to = view.findViewById(R.id.logs_textView_toDate);
        title = view.findViewById(R.id.logs_textView_resultTitle);
        results = view.findViewById(R.id.logs_arrayList_results);
        searchButton = view.findViewById(R.id.logs_imageButton_search);
        resultListAdapter = new LogsListAdapter(getActivity(), resultsArray);
        results.setAdapter(resultListAdapter);

        final Calendar calendar = Calendar.getInstance();
        fromYear = calendar.get(Calendar.YEAR);
        fromMonth = calendar.get(Calendar.MONTH);
        fromDay = calendar.get(Calendar.DAY_OF_MONTH);
        toYear = calendar.get(Calendar.YEAR);
        toMonth = calendar.get(Calendar.MONTH);
        toDay = calendar.get(Calendar.DAY_OF_MONTH);

        from.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
                    public void onDateSet(DatePicker view, int newYear, int newMonth, int newDay) {
                        fromYear = newYear;
                        fromMonth = newMonth;
                        fromDay = newDay;
                        updateDates();
                    }
                };
                DatePickerDialog newFragment = new DatePickerDialog(getActivity(), dateSetListener, fromYear, fromMonth, fromDay);
                newFragment.show();
            }
        });

        to.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
                    public void onDateSet(DatePicker view, int newYear, int newMonth, int newDay) {
                        toYear = newYear;
                        toMonth = newMonth;
                        toDay = newDay;
                        updateDates();
                    }
                };
                DatePickerDialog newFragment = new DatePickerDialog(getActivity(), dateSetListener, toYear, toMonth, toDay);
                newFragment.show();
            }
        });

        searchButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                requestData();
            }
        });

        title.setText("No Data to show");
        updateDates();

        return view;
    }

    //Query Database ReQuest
    private void requestData() {
        try {
            JSONObject REMVCommand = new JSONObject();
            REMVCommand.put("command_ID", "QDRQ");
            REMVCommand.put("fromYear", fromYear);
            REMVCommand.put("fromMonth", fromMonth);
            REMVCommand.put("fromDay", fromDay);
            REMVCommand.put("toYear", toYear);
            REMVCommand.put("toMonth", toMonth);
            REMVCommand.put("toDay", toDay);

            mListener.onSendCommand(REMVCommand.toString());
        } catch(JSONException exc) {
            Log.d("JSON exception", exc.getMessage());
        }

        //debug
        updateResultList();
    }

    @Override
    public void onAttach( Context context ) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


    public void onTCPReply(String receivedString) {
        try {
            JSONObject JSONparser = new JSONObject(receivedString);
            int fromTimestamp = JSONparser.getInt("from");
            int totimestamp = JSONparser.getInt("to");
            totalForPeriod =  JSONparser.getInt("totalProduced");

            updateResultList();
            //convert from timestamp to date, updateDates();
            title.setText("Total Bricks produced in the selected period; " + totalForPeriod);

        } catch (JSONException exc) {
            Log.d("Logs", exc.getMessage());
        }
    }

    private void updateResultList() {
        resultsArray.clear();
        Random rand = new Random();
        if(sortBy == GRADES) {
            for(int i=0; i<BrickManager.getTotalGrades(); i++) {
                /*
                try {
                    resultsArray.add(new LogListObject(BrickManager.getGrade(i), totalForPeriod, qtyByGrade.getInt(i)));
                } catch (JSONException exc) {
                    Log.d("populateDataList()", exc.getMessage());
                }
                */
                resultsArray.add(new LogListObject(BrickManager.getGrade(i), 30, rand.nextInt(10)));
                Log.d("", resultsArray.get(i).getPercent() + "");
            }

        } else if (sortBy == COLORS) {
            for(int i=0; i<BrickManager.getTotalColors(); i++) {
                try {
                    resultsArray.add(new LogListObject(BrickManager.getColorName(i), totalForPeriod, qtyByColor.getInt(i)));
                } catch (JSONException exc) {
                    Log.d("populateDataList()", exc.getMessage());
                }
            }
        }
        Collections.sort(resultsArray);
        boolean descendingOrder = true;
        if(descendingOrder) {
            Collections.reverse(resultsArray);
        }
        resultListAdapter.notifyDataSetChanged();
    }


    private void updateDates(){
        to.setText(toYear + "-" + toMonth + "-" + toDay);
        from.setText(fromYear + "-" + fromMonth + "-" + fromDay);
    }

    public void whenEnteringFragment() {

    }

    public void whenLeavingFragment() {

    }

    public interface OnFragmentInteractionListener {
        void onSendCommand( String command );
    }
}

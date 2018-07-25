package com.example.administrator.PickingStation;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;


public class Tiles extends Fragment {

    private View view;

    public Tiles() {
    }

    @Override
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
        view = inflater.inflate(R.layout.fragment_tiles, container, false);




        return view;
    }


    @Override

    public void onAttach( Context context ) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


    public void whenEnteringFragment() {

    }

    public void whenLeavingFragment() {

    }

}

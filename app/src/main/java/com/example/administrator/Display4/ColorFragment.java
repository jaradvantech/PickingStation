/**
 * Copyright 2015 Bartosz Lipinski
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.administrator.Display4;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

//import com.bartoszlipinski.flippablestackview.sample.R;

/**
 * Created by Bartosz Lipinski
 * 28.01.15
 */
public class ColorFragment extends Fragment {

    private static final String EXTRA_COLOR = "com.bartoszlipinski.flippablestackview.fragment.ColorFragment.EXTRA_COLOR";
    private static final String EXTRA_IMAGE = "com.bartoszlipinski.flippablestackview.fragment.ColorFragment.EXTRA_IMAGE";
    private static final String EXTRA_TEXT = "com.bartoszlipinski.flippablestackview.fragment.ColorFragment.EXTRA_TEXT";
    FrameLayout mMainLayout;

    public static ColorFragment newInstanceOnlyBackground(int backgroundColor, String text) {
        ColorFragment fragment = new ColorFragment();
        Bundle bdl = new Bundle();
        bdl.putString(EXTRA_TEXT, text);
        bdl.putInt(EXTRA_COLOR, backgroundColor);
        fragment.setArguments(bdl);
        return fragment;
    }

    public static ColorFragment newInstancePicture(int image) {
        ColorFragment fragment = new ColorFragment();
        Bundle bdl = new Bundle();
        bdl.putInt(EXTRA_IMAGE, image);
        fragment.setArguments(bdl);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_dummy, container, false);
        Bundle bdl = getArguments();

        mMainLayout = (FrameLayout) v.findViewById(R.id.main_layout);
        mMainLayout.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        mMainLayout.setAlpha((float)1);


        if(bdl.getInt(EXTRA_IMAGE)!=0) {
            ImageView image = new ImageView(getContext());
            image.setImageResource(bdl.getInt(EXTRA_IMAGE));
            image.setBackgroundColor(getResources().getColor(android.R.color.transparent));
            image.setImageAlpha(255);
            this.mMainLayout.addView(image);
        } else if(bdl.getInt(EXTRA_COLOR)!=0) {
            TextView textView = new TextView(getContext());

            textView.setText(bdl.getString(EXTRA_TEXT));
            this.mMainLayout.addView(textView);
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) textView.getLayoutParams();
            params.gravity= Gravity.CENTER;
            params.height =250;
            params.width = 250;
            textView.setLayoutParams(params);
            textView.setGravity(Gravity.CENTER);
            textView.setTextSize(18);
            textView.setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);

            GradientDrawable gd = new GradientDrawable();
            gd.setColor(bdl.getInt(EXTRA_COLOR)); // Changes this drawbale to use a single color instead of a gradient
            gd.setCornerRadius(1);
            gd.setStroke(1, 0xFF000000);

            textView.setBackground(gd);
        }


        return v;
    }


}

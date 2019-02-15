package com.cse5236.bowlbuddy;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import static java.lang.Thread.sleep;

public class LoadingScreenFragment extends Fragment {

    private View viewVar;
    private int loadPercent = 0;
    private int maxPercent = 100;
    private int minPercent = 0;
    private int sleepTime = 250;

    public LoadingScreenFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewVar = inflater.inflate(R.layout.loading_screen_fragment, container, false);
        return viewVar;
    }

    @Override
    public void onStart() {
        super.onStart();

        ProgressBar loadingBar = viewVar.findViewById(R.id.loadingBar);
        loadingBar.setMax(maxPercent);
        loadingBar.setProgress(loadPercent);
        try {
            while (loadPercent < maxPercent) {
                loadPercent++;
                loadingBar.setProgress(loadPercent);
                sleep(sleepTime);

            }
        } catch (InterruptedException e) {

        } finally {
            try {
                sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            HomePageFragment homeFragment = new HomePageFragment();
            FragmentManager manager = getFragmentManager();
            manager.beginTransaction().replace(R.id.loadingScreen, homeFragment, homeFragment.getTag()).commit();

        }

    }


}

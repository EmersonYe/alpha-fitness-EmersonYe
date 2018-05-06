package edu.sjsu.emerson.alphafitness;

import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by emersonsjsu on 4/18/18.
 */

public class RecordWorkoutLandscapeFragment extends Fragment implements RecordWorkoutActivity.onNewStepCounterData
{
    private static final String TAG = "DetailsFragment";
    User mUser;
    TextView averageSpeedText, maxSpeedText, minSpeedText;
    LineChart lineChart;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mUser = User.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.fragment_details, container, false);
        averageSpeedText = v.findViewById(R.id.averageSpeed);
        maxSpeedText = v.findViewById(R.id.maxSpeed);
        minSpeedText = v.findViewById(R.id.minSpeed);
        lineChart = v.findViewById(R.id.lineChart);
        return v;
    }

    @Override
    public void onNewStepData(List<Integer> steps, int updateInterval, int totalSteps)
    {
        int updateIntervalSec = updateInterval / 1000;
        System.out.println("updateIntervalSec: " + updateIntervalSec);
        double totalDistanceKm = stepsToKm(totalSteps);
        long minutes = 0;
        int seconds = 0;
        if (totalDistanceKm != 0) {
            System.out.println("totalDistanceKm : " + totalDistanceKm);
            int totalSec = steps.size() * updateIntervalSec;
            System.out.println("totalSec : " + totalSec);

            // TODO: keep min as sec to preserve accuracy
            double minPerKm = (totalSec / 60) / totalDistanceKm;
            System.out.println("minPerKm : " + minPerKm);
            minutes = (long) ((minPerKm / 1000) / 60);
            System.out.println("minutes : " + minutes);
            seconds = (int) ((minPerKm / 1000) % 60);
            System.out.println("seconds : " + seconds);
        }

        String formattedMinPerKM = String.format("Avg %d:%d min/km", minutes, seconds);
        averageSpeedText.setText(formattedMinPerKM);

        /*
        int maxStepsPerInterval = Collections.max(steps);
        // Log.e(TAG, "Max: " + maxStepsPerInterval);
        double maxMinPerKm = (updateIntervalSec / 60) / stepsToKm(maxStepsPerInterval);
        minutes = (long) ((maxMinPerKm / 1000) / 60);
        seconds = (int) ((maxMinPerKm / 1000) % 60);
        String formattedMaxMinPerKM = String.format("Max %d:%d min/km", minutes, seconds);
        maxSpeedText.setText(formattedMaxMinPerKM);


        int minStepsPerInterval = Collections.min(steps);
        double minMinPerKm = (updateIntervalSec / 60) / stepsToKm(minStepsPerInterval);
        minutes = (long) ((minMinPerKm / 1000) / 60);
        seconds = (int) ((minMinPerKm / 1000) % 60);
        String formattedMinMinPerKM = String.format("Min %d:%d min/km", minutes, seconds);
        minSpeedText.setText(formattedMinMinPerKM);
        */

        drawChart(steps, updateInterval);
    }

    private void drawChart(List<Integer> steps, int updateInterval)
    {
        List<Entry> entries = new ArrayList<Entry>();
        for (int i = 0; i < steps.size(); i++) {
            int data = steps.get(i);
            // turn your data into Entry objects
            entries.add(new Entry(i, data));
        }
        LineDataSet dataSet = new LineDataSet(entries, "Steps"); // add entries to dataset
        // dataSet.setColor(...);
        // dataSet.setValueTextColor(...); // styling, ...
        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);

        Description description = new Description();
        String formattedDescription = String.format("Steps per %d minutes and calories burnt over time", updateInterval / 1000);
        description.setText(formattedDescription);
        lineChart.setDescription(description);
        lineChart.invalidate();
    }

    private double stepsToKm(int steps)
    {
        double strideLengthCm = (mUser.getGender().equals("Female")) ? 134 : 152;
        return steps * strideLengthCm / 100000;
    }

}

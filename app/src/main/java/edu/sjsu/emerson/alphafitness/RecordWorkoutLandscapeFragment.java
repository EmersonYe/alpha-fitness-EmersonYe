package edu.sjsu.emerson.alphafitness;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static edu.sjsu.emerson.alphafitness.RecordWorkoutActivity.delay;

/**
 * Created by emersonsjsu on 4/18/18.
 */

public class RecordWorkoutLandscapeFragment extends Fragment implements RecordWorkoutActivity.onNewStepCounterData
{
    private static final String TAG = "DetailsFragment";
    private static final String STEPS = "steps";
    User mUser;
    ArrayList<Integer> steps = new ArrayList<>();
    TextView averageSpeedText, maxSpeedText, minSpeedText;
    LineChart lineChart;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mUser = User.getInstance();
        try {
            steps = savedInstanceState.getIntegerArrayList(STEPS);
        } catch (NullPointerException e){
        steps = null;
    }
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
        if (steps != null) drawChart(steps, delay);
        return v;
    }


    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putIntegerArrayList(STEPS, steps);
    }

    @Override
    public void onNewStepData(ArrayList<Integer> steps, int updateInterval, int totalSteps)
    {
        this.steps = steps;
        int updateIntervalSec = updateInterval / 1000;
        long minutes = 0;
        int seconds = 0;

        // Average speed
        double totalDistanceKm = stepsToKm(totalSteps);
        if (totalDistanceKm != 0) {
            int totalSec = steps.size() * updateIntervalSec;
            double secPerKm = totalSec / totalDistanceKm;
            minutes = (long) (secPerKm / 60);
            seconds = (int) (secPerKm % 60);
        }
        String formattedMinPerKM = String.format("Avg %d:%02d min/km", minutes, seconds);
        averageSpeedText.setText(formattedMinPerKM);

        // Max speed
        int maxStepsPerInterval = Collections.max(steps);
        double maxKm = stepsToKm(maxStepsPerInterval);
        if (maxKm != 0) {
            double maxSecPerKm = updateIntervalSec / maxKm;
            minutes = (long) (maxSecPerKm / 60);
            seconds = (int) (maxSecPerKm % 60);
        } else {
            minutes = 0;
            seconds = 0;
        }
        String formattedMaxMinPerKM = String.format("Max %d:%02d min/km", minutes, seconds);
        maxSpeedText.setText(formattedMaxMinPerKM);

        // Min speed
        int minStepsPerInterval = Collections.min(steps);
        double minKm = stepsToKm(minStepsPerInterval);
        if (minKm != 0) {
            double minSecPerKm = updateIntervalSec / minKm;
            minutes = (long) (minSecPerKm / 60);
            seconds = (int) (minSecPerKm % 60);
        } else {
            minutes = 0;
            seconds = 0;
        }
        String formattedMinMinPerKM = String.format("Min %d:%02d min/km", minutes, seconds);
        minSpeedText.setText(formattedMinMinPerKM);

        drawChart(steps, updateInterval);
    }

    private void drawChart(ArrayList<Integer> steps, int updateInterval)
    {
        ArrayList<ILineDataSet> lines = new ArrayList<>();
        // Steps over time
        List<Entry> entries = new ArrayList<>();
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

        // Calories over time
        List<Entry> entries1 = new ArrayList<>();
        for (int i = 0; i < steps.size(); i++) {
            int data = steps.get(i)/20;
            // turn your data into Entry objects
            entries1.add(new Entry(i, data));
        }
        LineDataSet dataSet1 = new LineDataSet(entries1, "Calories"); // add entries to dataset

        lines.add(dataSet);
        lines.add(dataSet1);
        LineData data = new LineData(lines);
        lineChart.setData(data);
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

package edu.sjsu.emerson.alphafitness;

import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by emersonsjsu on 4/18/18.
 */

public class RecordWorkoutLandscapeFragment extends Fragment implements RecordWorkoutActivity.onNewStepCounterData
{
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
        double totalDistanceKm = stepsToKm(totalSteps);
        int totalMs = steps.size() * updateInterval;
        int msPerKm = (int) (totalMs / totalDistanceKm);
        long minutes = (msPerKm / 1000) / 60;
        int seconds = (msPerKm / 1000) % 60;

        String formattedKmRate = String.format("Avg %d:%d min/km", minutes, seconds);
        averageSpeedText.setText(formattedKmRate);


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
        // TODO: add x axis label
        lineChart.invalidate();
    }

    private double stepsToKm(int steps)
    {
        double strideLengthCm = (mUser.getGender().equals("Female")) ? 134 : 152;
        return steps * strideLengthCm * 100000;
    }

}

package edu.sjsu.emerson.alphafitness;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by emersonsjsu on 4/18/18.
 */

public class RecordWorkoutLandscapeFragment extends Fragment
{
    TextView averageSpeedText, maxSpeedText, minSpeedText;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.fragment_details, container, false);
        averageSpeedText = v.findViewById(R.id.averageSpeed);
        maxSpeedText = v.findViewById(R.id.maxSpeed);
        minSpeedText = v.findViewById(R.id.minSpeed);
        return v;
    }
}

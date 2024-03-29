package edu.sjsu.emerson.alphafitness;

import android.app.Fragment;
import android.database.Cursor;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;

import edu.sjsu.emerson.alphafitness.database.MyContentProvider;
import edu.sjsu.emerson.alphafitness.database.workoutDbHelper;

public class ProfileFragment extends Fragment implements RecordWorkoutActivity.onNewStepCounterData
{
    private static final String TAG = "profileFragment";
    private User mUser;
    private EditText nameField, genderField, weightField;
    private TextView weeklyDistanceText, weeklyTimeText, weeklyWorkoutsText, weeklyCaloriesText, allDistanceText, allTimeText, allWorkoutsText, allCaloriesText;
    private Button updateButton;

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
        View v = inflater.inflate(R.layout.fragment_profile, container, false);
        // Obtain reference to UI elements
        nameField = v.findViewById(R.id.nameField);
        genderField = v.findViewById(R.id.genderField);
        weightField = v.findViewById(R.id.weightField);

        weeklyDistanceText = v.findViewById(R.id.weekly_distance);
        weeklyTimeText = v.findViewById(R.id.weekly_time);
        weeklyWorkoutsText = v.findViewById(R.id.weekly_workouts);
        weeklyCaloriesText = v.findViewById(R.id.weekly_calories);
        allDistanceText = v.findViewById(R.id.all_distance);
        allTimeText = v.findViewById((R.id.all_time));
        allWorkoutsText = v.findViewById(R.id.all_workouts);
        allCaloriesText = v.findViewById(R.id.all_calories);

        updateButton = v.findViewById(R.id.updateButton);

        updateIdentityFields();

        updateButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                mUser.setName(nameField.getText().toString());
                mUser.setGender(genderField.getText().toString());
                mUser.setWeight(Float.parseFloat(weightField.getText().toString()));
            }
        });

        updateFromDb();

        return v;
    }

    private void updateIdentityFields()
    {
        nameField.setText(mUser.getName());
        genderField.setText(mUser.getGender());
        weightField.setText(String.valueOf(mUser.getWeight()));
    }

    @Override
    public void onNewStepData(ArrayList<Integer> steps, int updateInterval, int totalSteps)
    {
        updateFromDb();
    }

    private void updateFromDb()
    {
        int totalWorkouts = 0;
        double totalDistance = 0;
        int totalDuration = 0;
        int totalCalories = 0;
        Cursor c = getActivity().getContentResolver().query(MyContentProvider.URI, null, null, null, "date");
        if (c.moveToFirst()) {
            do {
                totalWorkouts++;
                totalDistance += c.getColumnIndex(workoutDbHelper.DISTANCE);
                totalCalories += c.getColumnIndex(workoutDbHelper.CALORIES);
                totalDuration += c.getColumnIndex(workoutDbHelper.DURATION);
            } while (c.moveToNext());
        }
        allWorkoutsText.setText(totalWorkouts+" workouts");
        allDistanceText.setText(String.valueOf(totalDistance) + " miles");
        allTimeText.setText(totalDuration+" seconds");
        allCaloriesText.setText(totalCalories+" calories");

        weeklyWorkoutsText.setText(totalWorkouts+" workouts");
        weeklyDistanceText.setText(String.valueOf(totalDistance) + " miles");
        weeklyTimeText.setText(totalDuration+" seconds");
        weeklyCaloriesText.setText(totalCalories+" calories");
        c.close();
    }
}

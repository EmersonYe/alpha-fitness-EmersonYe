package edu.sjsu.emerson.alphafitness;

import android.app.Fragment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

public class ProfileFragment extends Fragment
{
    private User mUser;
    private EditText nameField, genderField, weightField;

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
        nameField = v.findViewById(R.id.nameField);
        genderField = v.findViewById(R.id.genderField);
        weightField = v.findViewById(R.id.weightField);

        nameField.setText(mUser.getName());
        genderField.setText(mUser.getGender());
        weightField.setText(String.valueOf(mUser.getWeight()));

        return v;
    }
}

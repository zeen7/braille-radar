package com.example.brailleradar;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;


import com.example.brailleradar.databinding.SignFilterBinding;
import com.example.brailleradar.models.SettingsConfig;


public class SignFilterFragment extends Fragment {
    private SignFilterBinding binding;
    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = SignFilterBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // LOCAL SAVING
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = preferences.edit();
        Boolean storedCheckbox1 = preferences.getBoolean("checkbox1", true);
        binding.checkBox1.setChecked(storedCheckbox1);
        Boolean storedCheckbox2 = preferences.getBoolean("checkbox2", true);
        binding.checkBox2.setChecked(storedCheckbox2);
        Boolean storedCheckbox3= preferences.getBoolean("checkbox3", true);
        binding.checkBox3.setChecked(storedCheckbox3);
        Boolean storedCheckbox4 = preferences.getBoolean("checkbox4", true);
        binding.checkBox4.setChecked(storedCheckbox4);
        binding.checkBox1.setOnClickListener(new View.OnClickListener() {
            String key = "checkbox1";
            @Override
            public void onClick(View view) {
                if(((CompoundButton) view).isChecked()){

                    editor.putBoolean(key, true);

                } else {
                    editor.putBoolean(key, false);
                }
                editor.commit();
            }
        });
        binding.checkBox2.setOnClickListener(new View.OnClickListener() {
            String key = "checkbox2";
            @Override
            public void onClick(View view) {
                if(((CompoundButton) view).isChecked()){
                    System.out.println("Checked");
                    editor.putBoolean(key, true);

                } else {
                    System.out.println("Un-Checked");
                    editor.putBoolean(key, false);
                }
                editor.commit();
            }
        });
        binding.checkBox3.setOnClickListener(new View.OnClickListener() {
            String key = "checkbox3";
            @Override
            public void onClick(View view) {
                if(((CompoundButton) view).isChecked()){
                    System.out.println("Checked");
                    editor.putBoolean(key, true);

                } else {
                    System.out.println("Un-Checked");
                    editor.putBoolean(key, false);

                }
                editor.commit();
            }
        });
        binding.checkBox4.setOnClickListener(new View.OnClickListener() {
            String key = "checkbox4";
            @Override
            public void onClick(View view) {
                if(((CompoundButton) view).isChecked()){
                    System.out.println("Checked");
                    editor.putBoolean(key, true);
                } else {
                    System.out.println("Un-Checked");
                    editor.putBoolean(key, false);
                }
                editor.commit();
            }
        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

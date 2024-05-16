package com.example.brailleradar;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;


import com.example.brailleradar.databinding.SettingsLayoutBinding;

import java.util.LinkedList;


public class SettingsFragment extends Fragment {
    //public LinkedList filterList = new LinkedList<String>();
    private SettingsLayoutBinding binding;
    private Vibrator myVib;
    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = SettingsLayoutBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // LOCAL SAVING
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = preferences.edit();

        // HAPTIC TOGGLE BUTTON
        Switch toggleHaptic = (Switch) view.findViewById(R.id.haptic_switch);

        Boolean storedToggleHaptic = preferences.getBoolean("toggleHaptic", true);
        toggleHaptic.setChecked(storedToggleHaptic);

        myVib = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        toggleHaptic.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                editor.putBoolean("toggleHaptic", isChecked);
                editor.apply();
                if(isChecked)
                {
                    myVib.vibrate( 1000);
                }
                else
                {

                }

            }
        });
        // SOUND TOGGLE BUTTON
        Switch toggleSound = (Switch) view.findViewById(R.id.sound_switch);

        Boolean storedToggleSound = preferences.getBoolean("toggleSound", true);
        toggleSound.setChecked(storedToggleSound);

        MediaPlayer mp = MediaPlayer.create(getContext(), R.raw.beep);
        toggleSound.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                editor.putBoolean("toggleSound", isChecked);
                editor.apply();
                if(isChecked)
                {
                    mp.start();
                }
                else
                {

                }

            }
        });


        // Accessiblity Settings
        Button accessiblityMode = (Button) view.findViewById(R.id.accessibility_settings_button);

        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);

        accessiblityMode.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    startActivity(intent);
                                                }
                                            });


        // FOR TESTING: Connect to database
        // ...

        // Make a Backend API requests to get all users

        // Make sure you change the UserService.java .baseUrl() line 19
        // with the host ip of your computer running the backend locally
        // This ip can be found by using ipconfig Windows / ifconfig (MacOS terminal)

//        UserService userService = new UserService();
//        userService.getUsers(new ServiceCallback() {
//            @Override
//            public void onSuccess(JsonNode jsonData) {
////                String username = jsonData.get("username").toString();
////                System.out.println(username);
//                System.out.println(jsonData.toString());
//            }
//
//            @Override
//            public void onFailure(Throwable t) {
//                System.out.println(t.toString());
//                System.out.println("Error: getUsers()");
//            }
//        });

    }
}

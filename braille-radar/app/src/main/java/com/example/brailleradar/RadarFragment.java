package com.example.brailleradar;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;


import com.example.brailleradar.databinding.RadarFragmentBinding;
import com.example.brailleradar.models.Coordinates;
import com.example.brailleradar.models.TagInfo;
import com.example.brailleradar.services.ApiService;
import com.example.brailleradar.services.JsonCallback;
import com.example.brailleradar.services.ServiceCallback;
import com.example.brailleradar.services.TagService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

import org.json.*;


import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.http.Tag;

public class RadarFragment extends Fragment {

    private @NonNull RadarFragmentBinding binding;
    private boolean radar_on = false;
//    private Scanner s;
//    private Thread scannerThread;
//    private GPS g;
//    private Compass c;
    private TextView sensor_log;
//    public List tagList = new ArrayList();
    LocationManager locationManager;
    LocationListener locationListener;


    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), result -> {}
    );
    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = RadarFragmentBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }


    // button logic
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        sensor_log = view.findViewById(R.id.tagHistory);
        sensor_log.setMovementMethod(new ScrollingMovementMethod());



        final Button on_off_button = (Button) view.findViewById(R.id.on_off_button_radar);
        final GifImageView radarImage = (GifImageView) view.findViewById(R.id.radar_gif);
        final TextView transmittingText = view.findViewById(R.id.radar_transmit_text);
        try {
            GifDrawable gifDrawable = new GifDrawable(getResources(), R.drawable.radar_animation);
            radarImage.setImageDrawable(gifDrawable);
            // turn ON/OFF RADAR BUTTON
            // Initialization
            // radar button on
            if (radar_on) {
                on_off_button.setBackgroundColor(ContextCompat.getColor(getContext(), android.R.color.holo_green_dark));
                on_off_button.setText("RADAR ON");
                transmittingText.setText("Detecting Signal");
                gifDrawable.start();
            }
            // radar button off
            else {
                on_off_button.setBackgroundColor(Color.RED);
                on_off_button.setText("RADAR OFF");
                transmittingText.setText("Not Detecting Signal");
                gifDrawable.stop();
            }
            // ON/OFF Button Logic
            on_off_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    radar_on = !radar_on;
                    if (radar_on) {
                        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION);
                            radar_on = !radar_on;
                            return;
                        }
                        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                            requestPermissionLauncher.launch(android.Manifest.permission.BLUETOOTH_SCAN);
                            radar_on = !radar_on;
                            return;
                        }
                        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                            requestPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT);
                            radar_on = !radar_on;
                            return;
                        }

                        on_off_button.setBackgroundColor(ContextCompat.getColor(getContext(), android.R.color.holo_green_dark));
                        on_off_button.setText("RADAR ON");
                        transmittingText.setText("Detecting Signal");
                        gifDrawable.start();



                    } else {
                        on_off_button.setBackgroundColor(Color.RED);
                        on_off_button.setText("RADAR OFF");
                        transmittingText.setText("Not Detecting Signal");
                        gifDrawable.stop();
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

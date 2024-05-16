package com.example.brailleradar;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.brailleradar.databinding.NavigationFragmentBinding;
import com.example.brailleradar.models.ClusterInfo;
import com.example.brailleradar.models.SearchListItem;
import com.example.brailleradar.models.TagInfo;

import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.http.HEAD;

public class NavigationFragment extends Fragment {
    private NavigationFragmentBinding binding;

    private TagInfo targetTag;
    private TagInfo nextTag = null;

    private GPS.LocationUpdateListener locationUpdateListener;
    private Compass.CompassUpdateListener compassUpdateListener;

    MediaPlayer beepAudio;
    MediaPlayer arrivedAudio;

    private float distanceFromTarget;
    private float targetBearing;
    private boolean locationReady;
    private boolean playedArriveSound;
    private long lastBeepTime;
    public List<TagInfo> filteredTagList;

    private final static int vibrateDuration = 500;
    private final static float arriveFrequency = 1000;
    private final static int arriveDistance = 3;
    private final static int correctBearingRange = 15;
    private final static int turnSlightlyBearingRange = 60;
    private final static int turnBearingRange = 150;
    private List<SearchListItem>  waypointList = new ArrayList<>();

    private ImageView directionArrowImage;
    private TextView targetTagText;
    private TextView targetDistanceText;
    private TextView targetTagLocationText;
    private TextView targetBearingText;
    private TextView instructionText;
    private WaypointAdapter adapter;

    VibratorManager vibratorManager;
    VibrationEffect vibrationEffect;
    Vibrator vibrator;

    boolean vibratorEnable;
    boolean toneEnable;

    private boolean inMultiPhaseNavigation = false;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = NavigationFragmentBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    @SuppressLint("SetTextI18n")
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        init(view);

        LocationObjectsSingleton.getGPS().setLocationUpdateListener(locationUpdateListener = () -> getGPSLocationUpdateListener());
        LocationObjectsSingleton.getCompass().setCompassUpdateListener(compassUpdateListener = () -> getCompassUpdateListener(view));
    }

    private void init(View view) {
        targetTag = (TagInfo) TagListFragment.targetBundle.get("target");
        nextTag = (TagInfo) TagListFragment.targetBundle.get("nextTarget");

        System.out.println("targetTag: " + targetTag + ", nextTag: " + nextTag);

        directionArrowImage = view.findViewById(R.id.direction_arrow);
        targetTagText = view.findViewById(R.id.tracking_tag_text);
        targetDistanceText = view.findViewById(R.id.tag_distance_text);
        targetBearingText = view.findViewById(R.id.tag_bearing_text);
        instructionText = view.findViewById(R.id.instruction_text);

        targetTagText.setText("Tracking: " + targetTag.getName());

        beepAudio = MediaPlayer.create(requireContext(), R.raw.beep1);
        arrivedAudio = MediaPlayer.create(requireContext(), R.raw.mixkit_alert_quick_chime_766);
        lastBeepTime = System.currentTimeMillis();

        vibratorManager = (VibratorManager) requireContext().getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
        vibrationEffect = VibrationEffect.createOneShot(vibrateDuration, VibrationEffect.DEFAULT_AMPLITUDE);
        vibrator = vibratorManager.getDefaultVibrator();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(requireActivity());
        vibratorEnable = preferences.getBoolean("toggleHaptic", true);
        toneEnable = preferences.getBoolean("toggleSound", true);

        locationReady = false;
        playedArriveSound = false;


        // Floor Nav
        if(inMultiPhaseNavigation) {
            System.out.println("Next Tag is NOT NULL: exiting floor nav init (short circuiting/avoiding)");
            return;
        }

        int currentFloor = LocationObjectsSingleton.getScanner().getCurrentFloor();
//        // TESTING (1 diff, 2 same, -1 unknown)
//        int currentFloor = 1;
        // TESTING: Tag2
        int destinationFloor = targetTag.getFloor();
        System.out.println("targetTag: " + targetTag.toString() + ", floor: " + targetTag.getFloor());

        // NOTE: The current Multi-floor solution assumes all elevators and stairwells
        //      span the entire building's floors (vertically)
        if (currentFloor != -1) {
            // CASE 1: If the currentFloor exists and is DIFFERENT than the destinationFloor
            if (currentFloor != destinationFloor) {
                // Search for elevator or staircase on that floor
                List<TagInfo> tagList = TagListSingleton.getTagList();
                System.out.println("tagList: " + tagList.toString());
//                List<TagInfo> currentFloorTags = tagList
//                        .stream().filter((tag) ->
//                                (
////                                        targetTag.getClusters().size() == 0 ||
//                                        inCluster(tag, targetTag.getClusters().get(0).getClusterId()))
////                                    && onFloor(tag, currentFloor)
//                        )
//                        .collect(Collectors.toList());
                List<TagInfo> currentClusterTags = tagList
                        .stream().filter((tag) ->
                                        (
//                                        targetTag.getClusters().size() == 0 ||
                                                inCluster(tag, targetTag.getClusters().get(0).getClusterId()))
//                                    && onFloor(tag, currentFloor)
                        )
                        .collect(Collectors.toList());
                System.out.println("currentClusterTags: " + currentClusterTags.toString());
                List<TagInfo> elevators = currentClusterTags
                        .stream().filter((tag) -> filterByTagType(tag, "elevator"))
                        .collect(Collectors.toList());
                System.out.println("elevators: " + elevators.toString());
                TagInfo nearestElevator = elevators.stream()
                        .min(Comparator.comparingDouble(tag -> tag.getCurrentDistance()))
                        .orElse(null);
                System.out.println("nearestElevator: " + (nearestElevator == null ? "null" : nearestElevator.toString()));
                List<TagInfo> stairs = currentClusterTags
                        .stream().filter((tag) -> filterByTagType(tag, "stairs"))
                        .collect(Collectors.toList());
                System.out.println("stairs: " + stairs.toString());
                TagInfo nearestStairs = stairs.stream()
                        .min(Comparator.comparingDouble(tag -> tag.getCurrentDistance()))
                        .orElse(null);
                System.out.println("nearestStairs: " + (nearestStairs == null ? "null" : nearestStairs.toString()));

                TagInfo newTarget = (nearestElevator != null) ? nearestElevator : nearestStairs;
                // TESTING
//                TagInfo newTarget = tagList.stream().filter((tag) -> tag.getDeviceName().equals("test")).collect(Collectors.toList()).get(0);
                String chosenMethod = "none";
                if (nearestElevator != null) {
                    chosenMethod = "elevator";
                } else if (nearestStairs != null) {
                    chosenMethod = "stairs";
                }

                // Ask user if they want to get to right floor
                if (chosenMethod.equals("none")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    String message = "This location is on floor " + destinationFloor + "\n\nEnsure you are on floor " + destinationFloor + " before proceeding";
                    builder.setMessage(message);
                    builder.setCancelable(true);
                    // Set the "Okay" button
                    builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Your code to handle the "Okay" button click
                            // For example, you can dismiss the dialog if needed
                            dialog.dismiss();
                        }
                    });
                    builder.setNegativeButton("Go Back", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            // Navigate back the list they were previously looking at.
                            moveBackNavigation();
                        }
                    });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage("This location is on floor " + destinationFloor + "\n\nDo you want to navigate from floor " + currentFloor + " to floor " + destinationFloor + "?");
                    builder.setCancelable(false);
                    // User selects Yes
                    builder.setPositiveButton("Yes", (DialogInterface.OnClickListener) (dialog, which) -> {
                        inMultiPhaseNavigation = true;
                        // navigate to closest Elevator/Staircase
                        nextTag = targetTag;
                        targetTag = newTarget;
                        TagListFragment.targetBundle.putParcelable("target", targetTag);
                        TagListFragment.targetBundle.putParcelable("nextTarget", nextTag);

                        ListView waypoints = view.findViewById(R.id.waypointList);

                        Log.d("ww", nextTag.getType());
                        waypointList = new ArrayList<>();

                        waypointList.add(getSearchItemObject(targetTag));
                        waypointList.add(getSearchItemObject(nextTag));

                        adapter = new WaypointAdapter(getActivity(), waypointList);
                        waypoints.setAdapter(adapter);

                        init(view);

                        LocationObjectsSingleton.getGPS().removeLocationUpdateListener(locationUpdateListener);
                        LocationObjectsSingleton.getCompass().removeCompassUpdateListener(compassUpdateListener);

                        LocationObjectsSingleton.getGPS().setLocationUpdateListener(locationUpdateListener = () -> getGPSLocationUpdateListener());
                        LocationObjectsSingleton.getCompass().setCompassUpdateListener(compassUpdateListener = () -> getCompassUpdateListener(view));
//                        // TEST
//                        AlertDialog alertDialog = builder.create();
//                        alertDialog.show();

                    });
                    // User selects No
                    builder.setNegativeButton("No", (DialogInterface.OnClickListener) (dialog, which) -> {
//                        // TESTING
//                        destinationReached(view);
                        dialog.cancel();
                    });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
            }
            // CASE 2: If the currentFloor exists and is THE SAME than the destinationFloor
            //          Then navigate the same as usual.
        } else {
            // CASE 3: If the currentFloor cannot be determined send a generic message!
            // Search for elevator or staircase on the destination floor
            List<TagInfo> tagList = TagListSingleton.getTagList();
            System.out.println("tagList: " + tagList.toString());
//            List<TagInfo> currentFloorTags = tagList
//                    .stream().filter((tag) ->
//                                    (
////                                        targetTag.getClusters().size() == 0 ||
//                                            inCluster(tag, targetTag.getClusters().get(0).getClusterId()))
////                                    && onFloor(tag, destinationFloor)
//                    )
//                    .collect(Collectors.toList());
            List<TagInfo> currentClusterTags = tagList
                    .stream().filter((tag) ->
                                    (
//                                        targetTag.getClusters().size() == 0 ||
                                            inCluster(tag, targetTag.getClusters().get(0).getClusterId()))
//                                    && onFloor(tag, destinationFloor)
                    )
                    .collect(Collectors.toList());
            System.out.println("currentClusterTags: " + currentClusterTags.toString());
            List<TagInfo> elevators = currentClusterTags
                    .stream().filter((tag) -> filterByTagType(tag, "elevator"))
                    .collect(Collectors.toList());
            System.out.println("elevators: " + elevators.toString());
            TagInfo nearestElevator = elevators.stream()
                    .min(Comparator.comparingDouble(tag -> tag.getCurrentDistance()))
                    .orElse(null);
            System.out.println("nearestElevator: " + (nearestElevator == null ? "null" : nearestElevator.toString()));
            List<TagInfo> stairs = currentClusterTags
                    .stream().filter((tag) -> filterByTagType(tag, "stairs"))
                    .collect(Collectors.toList());
            System.out.println("stairs: " + stairs.toString());
            TagInfo nearestStairs = stairs.stream()
                    .min(Comparator.comparingDouble(tag -> tag.getCurrentDistance()))
                    .orElse(null);
            System.out.println("nearestStairs: " + (nearestStairs == null ? "null" : nearestStairs.toString()));

            TagInfo newTarget = (nearestElevator != null) ? nearestElevator : nearestStairs;
            // TESTING
//                TagInfo newTarget = tagList.stream().filter((tag) -> tag.getDeviceName().equals("test")).collect(Collectors.toList()).get(0);
//                TagInfo newTarget = tagList.stream().filter((tag) -> tag.getDeviceName().equals("pppopqe")).collect(Collectors.toList()).get(0);
            String chosenMethod = "none";
            if (nearestElevator != null) {
                chosenMethod = "elevator";
            } else if (nearestStairs != null) {
                chosenMethod = "stairs";
            }

// TEST
//                waypoints.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//
//                    @Override
//                    public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
//
//                        adapter.remove(adapter.getTagListFull().get(0));
//                        adapter.notifyDataSetChanged();
//
//                    }
//                });


            // Ask user if they want to get to right floor
            if (chosenMethod.equals("none")) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                String message = "This location is on floor " + destinationFloor + "\n\nEnsure you are on floor " + destinationFloor + " before proceeding";
                builder.setMessage(message);
                builder.setCancelable(true);
                // Set the "Okay" button
                builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Your code to handle the "Okay" button click
                        // For example, you can dismiss the dialog if needed
                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton("Go Back", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        // Navigate back the list they were previously looking at.
                        moveBackNavigation();
                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                String message = "This location is on floor " + destinationFloor + "\n\nDo you wish to navigate to the nearest " + chosenMethod + "?";
                builder.setMessage(message);
                builder.setCancelable(false);
                // User selects Yes
                builder.setPositiveButton("Yes", (DialogInterface.OnClickListener) (dialog, which) -> {
                    inMultiPhaseNavigation = true;
                    // navigate to closest Elevator/Staircase
                    nextTag = targetTag;
                    targetTag = newTarget;
                    TagListFragment.targetBundle.putParcelable("target", targetTag);
                    TagListFragment.targetBundle.putParcelable("nextTarget", nextTag);

                    ListView waypoints = view.findViewById(R.id.waypointList);

                    Log.d("ww", nextTag.getType());
                    waypointList = new ArrayList<>();

                    waypointList.add(getSearchItemObject(targetTag));
                    waypointList.add(getSearchItemObject(nextTag));

                    adapter = new WaypointAdapter(getActivity(), waypointList);
                    waypoints.setAdapter(adapter);

//                    NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment_content_main);
//                    navController.navigate(R.id.NavigationFragment);

                    init(view);
//
                    LocationObjectsSingleton.getGPS().removeLocationUpdateListener(locationUpdateListener);
                    LocationObjectsSingleton.getCompass().removeCompassUpdateListener(compassUpdateListener);

                    LocationObjectsSingleton.getGPS().setLocationUpdateListener(locationUpdateListener = () -> getGPSLocationUpdateListener());
                    LocationObjectsSingleton.getCompass().setCompassUpdateListener(compassUpdateListener = () -> getCompassUpdateListener(view));
//                    waypoints.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View view) {
//                            // ExploreFragment.filterTypeBundle.putString("filterType", "room");
//                            // to get value from bundle
//                            // String myValue = ExploreFragment.filterTypeBundle.get("filterType");
//                        }
//                    });
//                    // TEST
//                    AlertDialog alertDialog = builder.create();
//                    alertDialog.show();
                });
                // User selects No
                builder.setNegativeButton("No", (DialogInterface.OnClickListener) (dialog, which) -> {
                    dialog.cancel();
//                    // TESTING
//                    destinationReached(view);
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        }
    }

    private void getGPSLocationUpdateListener() {
        Log.d("navigationfragment", "In Navigation: GPS UPDATED");

        float[] gpsNavigation = LocationObjectsSingleton.getGPS().calculateDistanceFromCurrent(targetTag.getCoordinates());
        distanceFromTarget = gpsNavigation[0];
        targetBearing = (gpsNavigation[1] + 360) % 360;
        locationReady = true;
    }

    private void getCompassUpdateListener(View view) {
        if (!locationReady) return;

        float currentBearing = LocationObjectsSingleton.getCompass().azimuth;
        float bearingDiff = targetBearing - currentBearing;
        if ((bearingDiff > 0 && bearingDiff < 180) || (bearingDiff > -360 && bearingDiff < -180)) {
            int instructionBearing = (int) (360 + bearingDiff) % 360;
            targetBearingText.setText("Bearing: " + instructionBearing);
            if (instructionBearing < 15) {
                instructionText.setText("Facing the correct direction.");
            } else if (instructionBearing < 60) {
                instructionText.setText("Turn slightly to your right.");
            } else if (instructionBearing < 150) {
                instructionText.setText("Turn to your right.");
            } else {
                instructionText.setText("Turn around");
            }
        } else {
            int instructionBearing = (int) (360 - bearingDiff) % 360;
            targetBearingText.setText("Bearing: " + instructionBearing);
            if (instructionBearing < 15) {
                instructionText.setText("Facing the correct direction.");
            } else if (instructionBearing < 60) {
                instructionText.setText("Turn slightly to your left.");
            } else if (instructionBearing < 150) {
                instructionText.setText("Turn to your left.");
            } else {
                instructionText.setText("Turn around");
            }
        }
        directionArrowImage.setRotation(bearingDiff);

        float actionFrequency = distanceFromTarget*40 + 200;
        if (distanceFromTarget <= 2) {

            actionFrequency = arriveFrequency;
        }
        long currTime = System.currentTimeMillis();
        if (currTime - lastBeepTime > actionFrequency) {
            lastBeepTime = currTime;
            if (toneEnable) {
                if (distanceFromTarget > 2) {
                    beepAudio.seekTo(20);
                    beepAudio.start();
                    playedArriveSound = false;
                } else if (!playedArriveSound) {
                    arrivedAudio.seekTo(35);
                    arrivedAudio.start();
                    playedArriveSound = true;
                    destinationReached(view);
                }
            }
            if (vibratorEnable) {
                if (distanceFromTarget <= 2) {
                    vibrator.vibrate(vibrationEffect);
                } else {
                    view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                }
            }
        }

        float sensorDistance = LocationObjectsSingleton.getScanner().getTagDistanceIfDetected(targetTag.getDeviceName());
        if (sensorDistance != 0) {
            if (sensorDistance < 20) {
                // Stop relying on gps?
                distanceFromTarget = sensorDistance;
            }
        }

        targetDistanceText.setText("Distance: " + formatDistance(distanceFromTarget));
    }

    private void destinationReached(View view) {
        if(nextTag != null) {
            TagListFragment.targetBundle.putParcelable("target", nextTag);
            TagListFragment.targetBundle.putParcelable("nextTarget", null);

            init(view);

            LocationObjectsSingleton.getGPS().removeLocationUpdateListener(locationUpdateListener);
            LocationObjectsSingleton.getCompass().removeCompassUpdateListener(compassUpdateListener);

            LocationObjectsSingleton.getGPS().setLocationUpdateListener(locationUpdateListener = () -> getGPSLocationUpdateListener());
            LocationObjectsSingleton.getCompass().setCompassUpdateListener(compassUpdateListener = () -> getCompassUpdateListener(view));
            // TODO: UI updates to the ListView of LinearLayouts

            adapter.remove(adapter.getTagListFull().get(0));
            adapter.notifyDataSetChanged();
        }

    }
    public SearchListItem getSearchItemObject(TagInfo tagInfo){
        String id = tagInfo.getTagId();
        String out = tagInfo.getName();
        String otherInfo = tagInfo.getType().substring(0, 1).toUpperCase()+ tagInfo.getType().substring(1);
        for(ClusterInfo cluster : tagInfo.getClusters()) {
            otherInfo += ", " + cluster.getName();
        }
        double distance = tagInfo.getCurrentDistance();
        String d;
        if(distance < 1000){
            d = String.format("%.1f", tagInfo.getCurrentDistance())+" m";
        } else {
            d = String.format("%.1f", tagInfo.getCurrentDistance()/1000)+ " km";
        }
        return new SearchListItem(id, out, getIcon(targetTag.getType()), d, otherInfo);
    }

    private int getIcon(String tagType) {
        Log.d("search", tagType);
        if(tagType.matches("room")){
            return R.drawable.room_icon_black;
        } else if (tagType.matches("washroom")) {
            return R.drawable.washroom_black;
        } else if (tagType.matches("food")) {
            return R.drawable.food_icon_black;
        } else if (tagType.matches("elevator")) {
            return R.drawable.elevator_icon_black;
        } else if (tagType.matches("stairs")) {
            return R.drawable.stairs_icon_black;
        } else if (tagType.matches("entrance")) {
            return R.drawable.entrance_icon_black;
        }
        return R.drawable.location_no_type_icon;
    }

    private String formatBearing(double bearing) {
        DecimalFormat df0 = new DecimalFormat("#");
        return df0.format(bearing);
    }

    private String formatDistance(double distance) {
        DecimalFormat df0 = new DecimalFormat("#");
        DecimalFormat df1 = new DecimalFormat("#.0");
        if (distance > 1000) {
            return df1.format(distance/1000.0) + " km";
        }
        return df0.format(distance) + " m";
    }

    private boolean onFloor(TagInfo tag, int floor) {
        boolean result = tag.getFloor() == floor;
        if(result) {
            System.out.println("Tag " + tag.getName() + " is on floor " + floor);
        } else {
            System.out.println("Tag " + tag.getName() + " is NOT on floor " + floor);
        }
        return result;
    }

    private boolean inCluster(TagInfo tag, String clusterId) {
        List<ClusterInfo> clusters = tag.getClusters();
        for(ClusterInfo cluster : clusters) {
            if(clusterId.equals(cluster.getClusterId())) {
                System.out.println("inCluster: " + tag.getName() + " is in cluster " + cluster.getName());
                return true;
            }
        }
        return false;
    }

    private boolean filterByTagType(TagInfo tag, String filterKeyword) {
        // 1. TagType Matching/Filtering
        // Check if the filterKeyword is a tag type:
        //  - If so, then this tag should be included if it
        //      matches the filterKeyword
        for(String type : TagListFragment.TagTypes) {
            // Case-sensitive matching
            if(filterKeyword.equals(type)) {
                return tag.getType().equals(filterKeyword);
            }
        }
        return false;
    }

    private void moveBackNavigation() {
        // Cleanup in onDestroyView...
        NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment_content_main);
        // Navigate back to the previous destination
        navController.navigateUp();
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        beepAudio.release();
        arrivedAudio.release();
        TagListFragment.targetBundle.putParcelable("target", null);
        TagListFragment.targetBundle.putParcelable("nextTarget", null);
        LocationObjectsSingleton.getGPS().removeLocationUpdateListener(locationUpdateListener);
        LocationObjectsSingleton.getCompass().removeCompassUpdateListener(compassUpdateListener);
    }
}
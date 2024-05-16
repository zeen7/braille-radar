package com.example.brailleradar;

import android.content.Context;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.brailleradar.databinding.ExploreFragmentBinding;
import com.example.brailleradar.models.ClusterInfo;
import com.example.brailleradar.models.SearchListItem;
import com.example.brailleradar.models.TagInfo;

import android.util.Log;
import android.widget.TextView;


import java.util.ArrayList;


public class ExploreFragment extends Fragment {
    private Vibrator myVib;
    private String logError;
    private ArrayList<SearchListItem> searchTagList;

    private @NonNull ExploreFragmentBinding binding;
    private boolean radar_on = false;
//    private Scanner s;
//    private GPS g;
//    private Compass c;
//    private Thread scannerThread;

    public static Bundle filterTypeBundle = new Bundle();
    public static Bundle targetBundle = new Bundle();
    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), result -> {}
    );

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = ExploreFragmentBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //fillSearchList();
        //Log.d("search", searchTagList.get(0).toString());



        ImageButton recentButton = (ImageButton) view.findViewById(R.id.recent_button);
        recentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ExploreFragment.filterTypeBundle.putString("filterType", "recent");
                NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment_content_main);
                navController.navigate(R.id.TagListFragment);
                // to get value from bundle
                // String myValue = ExploreFragment.filterTypeBundle.get("filterType");
            }
        });

        ImageButton sensingButton = (ImageButton) view.findViewById(R.id.sensing_button);
        sensingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ExploreFragment.filterTypeBundle.putString("filterType", "sensing");
                NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment_content_main);
                navController.navigate(R.id.TagListFragment);
                // to get value from bundle
                // String myValue = ExploreFragment.filterTypeBundle.get("filterType");
            }
        });

        ImageButton favouriteButton = (ImageButton) view.findViewById(R.id.favourite_button);
        favouriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ExploreFragment.filterTypeBundle.putString("filterType", "favourite");
                NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment_content_main);
                navController.navigate(R.id.TagListFragment);
                // to get value from bundle
                // String myValue = ExploreFragment.filterTypeBundle.get("filterType");
            }
        });

        ImageButton roomButton = (ImageButton) view.findViewById(R.id.room_button);
        roomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ExploreFragment.filterTypeBundle.putString("filterType", "room");
                NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment_content_main);
                navController.navigate(R.id.TagListFragment);

                // to get value from bundle
                // String myValue = ExploreFragment.filterTypeBundle.get("filterType");
            }
        });

        ImageButton washroomButton = (ImageButton) view.findViewById(R.id.washroom_button);
        washroomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ExploreFragment.filterTypeBundle.putString("filterType", "washroom");
                NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment_content_main);
                navController.navigate(R.id.TagListFragment);
                // to get value from bundle
                // String myValue = ExploreFragment.filterTypeBundle.get("filterType");
            }
        });

        ImageButton foodButton = (ImageButton) view.findViewById(R.id.food_button);
        foodButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ExploreFragment.filterTypeBundle.putString("filterType", "food");
                NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment_content_main);
                navController.navigate(R.id.TagListFragment);
                // to get value from bundle
                // String myValue = ExploreFragment.filterTypeBundle.get("filterType");
            }
        });

        ImageButton elevatorButton = (ImageButton) view.findViewById(R.id.elevator_button);
        elevatorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ExploreFragment.filterTypeBundle.putString("filterType", "elevator");
                NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment_content_main);
                navController.navigate(R.id.TagListFragment);
                // to get value from bundle
                // String myValue = ExploreFragment.filterTypeBundle.get("filterType");
            }
        });

        ImageButton stairsButton = (ImageButton) view.findViewById(R.id.stairs_button);
        stairsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ExploreFragment.filterTypeBundle.putString("filterType", "stairs");
                NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment_content_main);
                navController.navigate(R.id.TagListFragment);
                // to get value from bundle
                // String myValue = ExploreFragment.filterTypeBundle.get("filterType");
            }
        });

        ImageButton entranceButton = (ImageButton) view.findViewById(R.id.entrance_button);
        entranceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ExploreFragment.filterTypeBundle.putString("filterType", "entrance");
                NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment_content_main);
                navController.navigate(R.id.TagListFragment);
                // to get value from bundle
                // String myValue = ExploreFragment.filterTypeBundle.get("filterType");
            }
        });






        // SEARCH BAR
        AutoCompleteTextView searchLocationView = (AutoCompleteTextView) view.findViewById(R.id.location_search);

//        LocationObjectsSingleton.getGPS().setLocationUpdateListener(new GPS.LocationUpdateListener() {
//            @Override
//            public void onLocationUpdated(double latitude, double longitude) {
//                ArrayList<String> tagListStrings = new ArrayList<>();
//
//                double limit = 5;
//                int i = 0;
//                for(TagInfo tagInfo : TagListSingleton.getTagList()) {
//                    if(i == limit) {
//                        break;
//                    }
//                    tagListStrings.add(tagInfo.getName() + " (" + tagInfo.getCurrentDistance()
//                            + "m)\n" + tagInfo.getClusterName() + " - " + tagInfo.getType());
//                    i++;
//                }
//
//
//                // display name, cluster name, type, distance
//                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, tagListStrings);
//                searchLocationView.setAdapter(adapter);
//            }
//        });

        // Search autocomplete with 0 characters typed, show all options when search bar is pressed
        searchLocationView.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event){
                searchTagList = new ArrayList<>();

                double limit = 5;
                int i = 0;
                for(TagInfo tagInfo : TagListSingleton.getTagList()) {
//                    if(i == limit) {
//                        break;
//                    }
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

                    searchTagList.add(new SearchListItem(id, out, getIcon(tagInfo.getType()), d, otherInfo));

                    i++;
                }
                Log.d("search", searchTagList.toString());
                // display name, cluster name, type, distance
                AutoCompleteTextView searchLocationView = (AutoCompleteTextView) view.findViewById(R.id.location_search);
                SearchListAdapter adapter = new SearchListAdapter(getActivity(), searchTagList);
                searchLocationView.setAdapter(adapter);
                searchLocationView.showDropDown();
                searchLocationView.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED);
                return false;
            }
        });
        // options go away in search bar when user clicks enter key
        searchLocationView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    Log.e("TAG","Done pressed");
                }
                view.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_SELECTED);
                return false;
            }
        });
        // options go away in search bar when user clicks an option
        searchLocationView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                View view = getActivity().getCurrentFocus();
                System.out.println("arg2: "+arg2);
                System.out.println("arg3: "+arg3);
                if (view != null) {
                    InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }

                SearchListItem item = (SearchListItem) arg0.getItemAtPosition(arg2);
                System.out.println("arg0[" + arg2 + "]: " + item.getTagName() + " (" + item.getTagId() + ")");

                // Enter navigation mode with chosen tag
                String desiredTagId = item.getTagId();
                TagInfo tag = TagListSingleton.getTagList()
                        .stream()
                        .filter(t -> desiredTagId.equals(t.getTagId()))
                        .findFirst()
                        .orElse(null);

                if (tag != null) {
                    moveToNavigationFragment(tag);
                } else {
                    // Handle the case where the tag with the specified ID is not found
                    // This could be an error or you might want to handle it in a specific way
                    Log.e("searchbar", "Item arg0[" + arg2 + "] was not found in the tag list... Cannot navigate to this tag");
                }
            }

        });
    }

    private void moveToNavigationFragment(TagInfo tag) {
        // Store the target Coordinates (or alternatively tag ID + backend call)
        // for the navigation fragment
        TagListFragment.targetBundle.putParcelable("target", tag);

        NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment_content_main);
        // Navigate to the Navigation Fragment with target Tag ID (or coordinates)
        navController.navigate(R.id.NavigationFragment);
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



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
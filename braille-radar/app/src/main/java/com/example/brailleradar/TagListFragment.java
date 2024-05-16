package com.example.brailleradar;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.preference.PreferenceManager;

import com.example.brailleradar.databinding.TagListFragmentBinding;
import com.example.brailleradar.models.ClusterInfo;
import com.example.brailleradar.models.TagInfo;

import org.w3c.dom.Text;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class TagListFragment extends Fragment {

    // Can get these enums from the backend (more robust)
    public static final String[] TagTypes = {"room", "washroom", "food", "elevator", "stairs", "entrance"};

    public List<TagInfo> filteredTagList;

    private @NonNull TagListFragmentBinding binding;

    private String filterKeyword = (String) ExploreFragment.filterTypeBundle.get("filterType");

    public static Bundle targetBundle = new Bundle();

    private boolean firstRun = true;

    private GPS.LocationUpdateListener locationUpdateListener;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        String title = "";
        if (filterKeyword != null && !filterKeyword.isEmpty()) {
            title = filterKeyword.substring(0, 1).toUpperCase() + filterKeyword.substring(1);
        }

        // Set action bar title
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null && activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().setTitle(title);
        }

        binding = TagListFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(updateList(view)) {
            firstRun = false;
        }

        LocationObjectsSingleton.getGPS().setLocationUpdateListener(locationUpdateListener = () -> {
            System.out.println("Tag List Fragment: firstRun = " + firstRun);

            // If we are in the Nearby ("sensing") view, constantly update it:
            filterKeyword = (String) ExploreFragment.filterTypeBundle.get("filterType");

            if(!firstRun && !filterKeyword.equals("sensing")) {
                return;
            }
            firstRun = filterKeyword.equals("sensing");
            // Set the adapter to the ListView on the main thread
            requireActivity().runOnUiThread(() -> updateList(view));
        });
//            // TODO: add row background colour based on the proximity of the sensor/and if in BLE range
//            // TODO: Add a map of distance ranges to colour in a Constants file
    }

    private boolean updateList(View view) {
//        if(MainActivity.cacheLoaded) {
//            // Stop the progress loading indicator
//            ProgressBar progressBar = view.findViewById(R.id.tag_list_loading);
//            progressBar.setVisibility(View.GONE);
//        }

        ListView listView = view.findViewById(R.id.tag_list_view);
        LinearLayout tagListLayout = view.findViewById(R.id.tag_list_layout);

        // Update the elements in the table
        //        TableLayout tl = view.findViewById(R.id.tag_list_table);

        filterKeyword = (String) ExploreFragment.filterTypeBundle.get("filterType");
        ////        // TESTING
        //        String filterKeyword = "Type1";
        System.out.println("filterKeyword: " + filterKeyword);

        if(filterKeyword.matches("recent")){
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(requireActivity());

            Set<String> set = preferences.getStringSet("RecentList", null);
            filteredTagList = new ArrayList<>();
            if (set != null) {
                for (int j = 0; j < TagListSingleton.getTagList().size(); j++) {
                    if (set.contains(TagListSingleton.getTagList().get(j).getTagId())) {
                        filteredTagList.add(TagListSingleton.getTagList().get(j));
                    }
                }
            }
        } else if(filterKeyword.matches("favourite")){
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(requireActivity());

            Set<String> set = preferences.getStringSet("FavouriteList", null);
            filteredTagList = new ArrayList<>();
            if (set != null) {
                for (int j = 0; j < TagListSingleton.getTagList().size(); j++) {
                    if (set.contains(TagListSingleton.getTagList().get(j).getTagId())) {
                        filteredTagList.add(TagListSingleton.getTagList().get(j));
                    }
                }
            }
        } else {
            // Apply the filter to the tag list
            filteredTagList = TagListSingleton.getTagList()
                    .stream()
                    .filter(tag -> includedTag(tag, filterKeyword))
                    .collect(Collectors.toList());
        }
        String filteredString = "";
        for(TagInfo tag : filteredTagList) {
            filteredString += tag.getName() + ", ";
        }
        System.out.println("filteredTagList: " + filteredTagList + " " + filteredString);


        // If the filtered tag list 0, continue trying to update/populate it
        // Or it is the sensing list
        if(filteredTagList.isEmpty()) {
            firstRun = true;

            // Add a comment that there are currently no tags if cache is already loaded
            if(MainActivity.cacheLoaded) {
                // Clear the current layout children - removes progress bar
                tagListLayout.removeAllViews();

                TextView noTagsTextView = new TextView(requireContext());
                noTagsTextView.setText("NO TAGS...");
                noTagsTextView.setPadding(0, getDp(30), 0, 0);
                noTagsTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 36);
//                // Set layout parameters for vertical centering
//                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
//                        LinearLayout.LayoutParams.MATCH_PARENT,
//                        LinearLayout.LayoutParams.MATCH_PARENT
//                );
////                layoutParams.gravity = Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL;
//                noTagsTextView.setLayoutParams(layoutParams);
                noTagsTextView.setGravity(Gravity.CENTER);

                tagListLayout.addView(noTagsTextView, 0); // Add as the first element in the linear layout
                tagListLayout.addView(listView);
            }
        } else {
            // Clear the current layout children - removes progress bar
            tagListLayout.removeAllViews();

            // Add back the list view
            tagListLayout.addView(listView);

            // Set a click listener if needed
            listView.setOnItemClickListener((parent, v, position, id) -> {
                TagInfo selectedTag = filteredTagList.get(position);
                moveToNavigationFragment(selectedTag);
            });

            ArrayAdapter<TagInfo> adapter = new ArrayAdapter<TagInfo>(
                    requireContext(),
                    R.layout.tag_list_item,
                    R.id.nameTextView,
                    filteredTagList
            ) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    // Get the current item from the adapter
                    TagInfo tag = (TagInfo) getItem(position);

                    // Inflate the custom layout for the item
                    if (convertView == null) {
                        convertView = LayoutInflater.from(getContext()).inflate(R.layout.tag_list_item, parent, false);
                    }

                    // Bind data to the views in the custom layout
                    ImageView iconImageView = convertView.findViewById(R.id.iconImageView);
                    TextView nameTextView = convertView.findViewById(R.id.nameTextView);
                    TextView locationTextView = convertView.findViewById(R.id.locationTextView);
                    TextView distanceTextView = convertView.findViewById(R.id.distanceTextView);
                    LinearLayout clustersLayout = convertView.findViewById(R.id.clustersLayout);
                    ToggleButton favButton = convertView.findViewById(R.id.favouriteButton);

                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(requireActivity());
                    Set<String> favouritesSet = preferences.getStringSet("FavouriteList", null);
                    if (favouritesSet == null) {
                        favouritesSet = new HashSet<>();
                    }

                    // If already saved in savedPreferences, set the icon to true
                    if(favouritesSet.contains(tag.getTagId())){
                        System.out.println("Fav: " + tag);
                        favButton.setChecked(true);
                    } else {
                        favButton.setChecked(false);
                    }

                    favButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            System.out.println(position);

                            Log.d("favs",position +" "+ tag.getName() + " " + tag.getTagId());

                            SharedPreferences.Editor editor = preferences.edit();
                            if(favButton.isChecked()){
                                Log.d("favs", "isChecked");
                                FavouritesSingleton.setRecentTagList(tag.getTagId());

                                Set<String> set = new HashSet<String>(FavouritesSingleton.getRecentTagList());

                                editor.putStringSet("FavouriteList", set);
                                editor.commit();

                            } else {
                                Log.d("favs", "notChecked");
                                // TODO: Fix bug upon startup when Favourites list is not working
                                FavouritesSingleton.removeRecentTagList(tag.getTagId());
                                Set<String> set = new HashSet<String>(FavouritesSingleton.getRecentTagList());

                                editor.putStringSet("FavouriteList", set);
                                editor.commit();
                            }
                        }
                    });

                    iconImageView.setImageResource(R.drawable.direction_arrow_thick_white);
                    iconImageView.setBackgroundResource(R.drawable.circle_button);
                    iconImageView.setContentDescription("Navigate to");
                    final int iconMaxHeight = getDp(50);
                    iconImageView.setAdjustViewBounds(true);
                    iconImageView.setMaxHeight(iconMaxHeight);

                    nameTextView.setText(tag.getName());
                    nameTextView.setContentDescription(tag.getName());

                    String location = tag.getLocation();
                    if(location != null) {
                        locationTextView.setVisibility(TextView.VISIBLE);
                        locationTextView.setText("("+ location + ")");
                        locationTextView.setContentDescription("at " + location);
                    }


                    // Clear previous cluster TextViews and add new ones
                    clustersLayout.removeAllViews();
                    String clustersDescription = "In";
                    boolean first = true;
                    for (ClusterInfo cluster : tag.getClusters()) {
                        TextView clusterTextView = new TextView(getContext());
                        clusterTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
                        clusterTextView.setTextColor(Color.DKGRAY);
                        clusterTextView.setMaxHeight(getDp(20));
                        clusterTextView.setText(cluster.getName());
                        clustersDescription += (first ? "" : " and ") + clustersDescription + ", ";
                        if(first) {
                            first = false;
                        }
                        clustersLayout.addView(clusterTextView);
                    }
                    clustersLayout.setContentDescription(clustersDescription);

                    //                // FOR THE SS!!!
                    //                if(tag.getDeviceName().equals("BR_005")) {
                    //                    distanceTextView.setText(formatDistance(9.89));
                    //                }
                    //                else if (tag.getDeviceName().equals("BR_007")) {
                    //                    distanceTextView.setText(formatDistance(5.3));
                    //                }
                    //                else {
                    //                    distanceTextView.setText(formatDistance(tag.getCurrentDistance()));
                    //                    distanceTextView.setContentDescription("that is " + formatDistance(tag.getCurrentDistance()) + " away");
                    //                }
                    distanceTextView.setText(formatDistance(filterKeyword.equals("sensing") ? LocationObjectsSingleton.getScanner().getTagDistanceIfDetected(tag.getDeviceName()) : tag.getCurrentDistance()));
                    distanceTextView.setContentDescription("that is " + formatDistance(tag.getCurrentDistance()) + " away");


                    return convertView;
                }
            };

            listView.setAdapter(adapter);
        }

        return filteredTagList.size() > 0;
    }

    private int getDp(float dp) {
        return (int) (dp * requireContext().getResources().getDisplayMetrics().density + 0.5f);
    }

    private String formatDistance(double distance) {
        DecimalFormat df0 = new DecimalFormat("#");
        DecimalFormat df1 = new DecimalFormat("#.0");
        if (distance > 1000) {
            return df1.format(distance/1000.0) + " km";
        }
        return df0.format(distance) + " m";
    }

    private ImageView getArrowIcon() {
        ImageView imageView = new ImageView(requireContext());
        imageView.setImageResource(R.drawable.direction_arrow);
        return imageView;
    }

    private ImageView getIcon(String type) {
        ImageView imageView = new ImageView(requireContext());

        // Assuming your resource names follow a naming convention like "type_icon"
        int resourceId = getResources().getIdentifier(type.toLowerCase() + "_icon", "drawable", requireContext().getPackageName());

        if(resourceId != 0) {
            imageView.setImageResource(resourceId);
        }

//        //setting image position
//        imageView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
//                LinearLayout.LayoutParams.WRAP_CONTENT));

//        switch(type) {
//            case "room": imageView.setImageResource(R.drawable.room_icon);
//            break;
//            case "washroom": imageView.setImageResource(R.drawable.washroom_icon);
//            break;
//            case "food": imageView.setImageResource(R.drawable.food_icon);
//            break;
//            case "elevator": imageView.setImageResource(R.drawable.elevator_icon);
//            break;
//            case "stairs": imageView.setImageResource(R.drawable.stairs_icon);
//            break;
//            case "entrance": imageView.setImageResource(R.drawable.entrance_icon);
//            break;
//
//        }

        return imageView;
    }

    private boolean inCluster(TagInfo tag, String clusterId) {
        List<ClusterInfo> clusters = tag.getClusters();
        for(ClusterInfo cluster : clusters) {
            if(clusterId.equals(cluster.getClusterId())) {
                return true;
            }
        }
        return false;
    }

    private boolean includedTag(TagInfo tag, String filterKeyword) {
        // 1. TagType Matching/Filtering
        // Check if the filterKeyword is a tag type:
        //  - If so, then this tag should be included if it
        //      matches the filterKeyword
        for(String type : TagTypes) {
            // Case-sensitive matching
            if(filterKeyword.equals(type)) {
                return tag.getType().equals(filterKeyword);
            }
        }

        // 2. Other Matching
        switch(filterKeyword) {
            case "recent": {
                // TODO: Check if the tag is in the recent list
                // !!! Needs access to the LogContext Object from Scanner
                // Make into a Singleton?
                return false;
            }
            case "sensing": {
                // TODO Check if the tag is currently being sensed
                List<String> detectedDeviceNames = LocationObjectsSingleton.getScanner().getAllDetectedTags();
//                // TESTING
//                List<String> detectedDeviceNames = List.of("BR_001");
//                // For the SS!!
//                List<String> detectedDeviceNames = List.of("BR_005", "BR_007");

                // Check if the tag's name is in the currently detected devices
                return detectedDeviceNames.contains(tag.getDeviceName());
            }
            // Otherwise, no filtering should be applied
            default: return true;
        }
    }

    private void moveToNavigationFragment(TagInfo tag) {
        // Store the target Coordinates (or alternatively tag ID + backend call)
        // for the navigation fragment
        TagListFragment.targetBundle.putParcelable("target", tag);

        NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment_content_main);
        // Navigate to the Navigation Fragment with target Tag ID (or coordinates)
        navController.navigate(R.id.NavigationFragment);
    }

    private void setRowBackgroundColor(View view, double distance) {
        // Determine the color based on the value (you can customize this logic)
        int color = getRowColour(distance);

        // Set the background color
        view.setBackgroundColor(color);
    }

    private int getRowColour(double distance) {
        // TODO: Add colours to the constants: models/DistanceColour + /constants/ or /utils/ with array of DistanceColours
        // TODO: Add a colour for within BLE range
        // Example logic: Interpolate color from green to red based on the value
        int green = (int) (255 * (1 - distance));  // Higher value, closer to green
        int red = (int) (255 * distance);          // Higher value, closer to red
        int blue = 0;

        // Ensure the values are in the valid range (0 to 255)
        green = Math.max(0, Math.min(255, green));
        red = Math.max(0, Math.min(255, red));

        // Return the interpolated color
        return Color.rgb(red, green, blue);
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        LocationObjectsSingleton.getGPS().removeLocationUpdateListener(locationUpdateListener);
    }

}

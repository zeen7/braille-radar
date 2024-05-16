package com.example.adminapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.adminapp.api.ClusterService;
import com.example.adminapp.api.OrganizationService;
import com.example.adminapp.api.ServiceCallback;
import com.example.adminapp.api.TagService;
import com.example.adminapp.databinding.TagManagementBinding;
import com.example.adminapp.model.Cluster;
import com.example.adminapp.model.TagInfo;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TagManagementFrag extends Fragment {
    private TagManagementBinding binding;

    private Cluster selectedCluster;

    private final OrganizationService organizationService = new OrganizationService();
    private final ClusterService clusterService = new ClusterService();
    private final TagService tagService = new TagService();

    public static Bundle targetBundle = new Bundle();

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = TagManagementBinding.inflate(inflater, container, false);


        return binding.getRoot();


    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // LOCAL SAVING
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = preferences.edit();

        ListView listView = view.findViewById(R.id.managementTagListView);
        View parentView = view;

        // Set up the Spinner with company names
        Spinner spinnerCluster = view.findViewById(R.id.managementSpinnerCluster);
        String selectedOrganization = preferences.getString("organizationId", null);
        System.out.println("TagManagement: selectedOrganization: " + selectedOrganization);
        organizationService.getAllOrganizationClusters(selectedOrganization, new ServiceCallback() {
            @Override
            public void onSuccess(JsonNode clusters) {
                List<Cluster> clustersArray = new ArrayList<>(clusters.size());
                for(JsonNode cluster : clusters) {
                    clustersArray.add(new Cluster(cluster.get("clusterId").asText(), cluster.get("name").asText()));
                }
                ArrayAdapter<Cluster> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, clustersArray);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCluster.setAdapter(adapter);

                // Set the default to the first element
//                spinnerCluster.setSelection(0);
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e("cluster", "Unable to fetch organization (" + selectedOrganization + ") clusters");
            }
        });
//        String[] companyNamesArray = getResources().getStringArray(R.array.company_names_array);
//        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, companyNamesArray);
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        spinnerOrgName.setAdapter(adapter);

        spinnerCluster.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCluster = (Cluster) parent.getItemAtPosition(position);
//                editor.putString("organizationName", selectedOrganization.getName());
//                editor.putString("organizationId", selectedOrganization.getOrgId());
//                editor.apply();
                System.out.println("selectedCluster: " + selectedCluster.getName() + " (" + selectedCluster.getClusterId() + ")");

                // Show the progress indicator again (+ clear listView)
                ProgressBar loading = getView().findViewById(R.id.managementListLoading);
                if(loading != null) {
                    loading.setVisibility(ProgressBar.VISIBLE);
                }
                listView.setAdapter(null);

                // Populate the list of tags.
                clusterService.getClusterTags(selectedCluster.getClusterId(), new ServiceCallback() {
                    @Override
                    public void onSuccess(JsonNode tags) {
                        System.out.println("tags: " + tags.toString());

                        List<TagInfo> tagList = new ArrayList();
                        if (tags.isArray()) {
                            // Convert array of JsonNodes to an array of TagInfos
                            for (int i = 0; i < tags.size(); i++) {
                                tagList.add(new TagInfo(tags.get(i)));
                            }

                            // Sort the tag list by name in ascending alphabetical order
                            Collections.sort(tagList, new Comparator<TagInfo>() {
                                @Override
                                public int compare(TagInfo tag1, TagInfo tag2) {
                                    return tag1.getName().compareToIgnoreCase(tag2.getName());
                                }
                            });
                        } else {
                            // Handle the case where 'tags' is not an array
                            // i.e. SOMETHING WENT WRONG...
                            System.out.println("Something went wrong fetching the tags from the backend - it is not in the correct array format");
                        }


                        // Clear the current progress loader
                        ProgressBar loading = getView().findViewById(R.id.managementListLoading);
                        if(loading != null) {
                            loading.setVisibility(ProgressBar.GONE);
                        }

                        // Note: This does not work for some reason so I'm going with the button press
//                        // Setup Tag List View
//                        // Set a click listener to move to the edit tag page
//                        listView.setOnItemClickListener((parent, v, position, id) -> {
//                            System.out.println("SELECTED!");
//                            TagInfo selectedTag = tagList.get(position);
//                            System.out.println("selectedTag: " + selectedTag);
////                            moveToTagEditFragment(selectedTag);
//                        });


                        ArrayAdapter<TagInfo> adapter = new ArrayAdapter<TagInfo>(
                                requireContext(),
                                R.layout.tag_list_item,
                                R.id.nameTextView,
                                tagList
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
                                ImageView iconImageView = convertView.findViewById(R.id.tagImageView);
                                TextView nameTextView = convertView.findViewById(R.id.nameTextView);
                                TextView deviceNameTextView = convertView.findViewById(R.id.deviceNameTextView);
//                                LinearLayout clustersLayout = convertView.findViewById(R.id.clustersLayout);
                                TextView locationTextView = convertView.findViewById(R.id.locationTextView);
                                ImageView deleteButtonImageView = convertView.findViewById(R.id.deleteButton);
                                Button editButton = convertView.findViewById(R.id.editButton);

                                // Set a click listener to move to the edit tag page
                                editButton.setOnClickListener((l) -> {
                                    TagInfo selectedTag = tagList.get(position);
                                    System.out.println("selectedTag: " + selectedTag.getName() + "(" + selectedTag.getTagId() + ")");
                                    moveToTagEditFragment(selectedTag, selectedCluster);
                                });

                                deleteButtonImageView.setOnClickListener((l) -> {
                                    TagInfo selectedTag = tagList.get(position);

                                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                    builder.setMessage("Are you sure you want to delete " + selectedTag.getName()
                                            + "(" + selectedTag.getDeviceName() + ")?");
                                    builder.setCancelable(false);
                                    // User selects Yes
                                    builder.setPositiveButton("Yes", (DialogInterface.OnClickListener) (dialog, which) -> {
                                        tagService.deleteTagById(selectedTag.getTagId(), new ServiceCallback() {
                                            @Override
                                            public void onSuccess(JsonNode jsonData) {
                                                // Doesn't enter here for some reason...
                                                System.out.println("Successfully deleted tag " + selectedTag.getName() + " (" + selectedTag.getTagId() + ")");
                                                tagList.remove(position);
                                                notifyDataSetChanged();
                                                Toast.makeText(getActivity(), "Tag deleted successfully!", Toast.LENGTH_SHORT).show();
                                            }

                                            @Override
                                            public void onFailure(Throwable t) {
//                                                Log.e("tagservice", "Failed to delete tag " + selectedTag.getName() + " (" + selectedTag.getTagId() + "): " + t.toString());
                                                Log.d("tagservice", "Attempted to delete tag " + selectedTag.getName() + " (" + selectedTag.getTagId() + "): " + t.toString());
                                                tagList.remove(position);
                                                notifyDataSetChanged();
//                                                Toast.makeText(getActivity(), "Failed to delete tag", Toast.LENGTH_SHORT).show();
                                                Toast.makeText(getActivity(), "Tag deleted successfully!", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    });
                                    // User selects No
                                    builder.setNegativeButton("No", (DialogInterface.OnClickListener) (dialog, which) -> {
                                        dialog.cancel();
                                    });
                                    AlertDialog alertDialog = builder.create();
                                    alertDialog.show();
                                });

                                iconImageView.setImageResource(getIconBlack(tag.getType()));
//                                iconImageView.setBackgroundResource(R.drawable.circle_button);
                                iconImageView.setContentDescription(tag.getType());

                                final int iconMaxHeight = getDp(50);
                                iconImageView.setAdjustViewBounds(true);
                                iconImageView.setMaxHeight(iconMaxHeight);

                                nameTextView.setText(tag.getName());
                                nameTextView.setContentDescription(tag.getName());

                                deviceNameTextView.setText(tag.getDeviceName());
                                deviceNameTextView.setContentDescription(tag.getDeviceName());

                                String location = tag.getLocation();
                                if(location != null) {
                                    locationTextView.setVisibility(TextView.VISIBLE);
                                    locationTextView.setText("("+ location + ")");
                                    locationTextView.setContentDescription("at " + location);
                                }

//                                // Clear previous cluster TextViews and add new ones
//                                clustersLayout.removeAllViews();
//                                String clustersDescription = "In";
//                                boolean first = true;
//                                for (String cluster : tag.getClusterNames()) {
//                                    TextView clusterTextView = new TextView(getContext());
//                                    clusterTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
//                                    clusterTextView.setTextColor(Color.DKGRAY);
//                                    clusterTextView.setMaxHeight(getDp(20));
//                                    clusterTextView.setText(cluster);
//                                    clustersDescription += (first ? "" : " and ") + clustersDescription + ", ";
//                                    if(first) {
//                                        first = false;
//                                    }
//                                    clustersLayout.addView(clusterTextView);
//                                }
//                                clustersLayout.setContentDescription(clustersDescription);

                                return convertView;
                            }
                        };

                        listView.setAdapter(adapter);
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        System.out.println("failed to get tags for cluster: " + selectedCluster.getName() + "(" + selectedCluster.getClusterId() + ")");
                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // This method is called when nothing is selected in the Spinner
            }
        });
    }

    private int getIcon(String type) {
//        ImageView imageView = new ImageView(requireContext());

        // Assuming your resource names follow a naming convention like "type_icon"
        int resourceId = getResources().getIdentifier(type.toLowerCase() + "_icon", "drawable", requireContext().getPackageName());

//        if(resourceId != 0) {
//            imageView.setImageResource(resourceId);
//        }

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

//        return imageView;
        return resourceId;
    }

    private int getIconBlack(String type) {
//        ImageView imageView = new ImageView(requireContext());

        // Assuming your resource names follow a naming convention like "type_icon"
        int resourceId = getResources().getIdentifier(type.toLowerCase() + "_icon_black", "drawable", requireContext().getPackageName());

//        if (resourceId != 0) {
//            imageView.setImageResource(resourceId);
//        }

//        return imageView;
        return resourceId;
    }

    private int getDp(float dp) {
        return (int) (dp * requireContext().getResources().getDisplayMetrics().density + 0.5f);
    }

    private void moveToTagEditFragment(TagInfo tag, Cluster cluster) {
        // Store the target Coordinates (or alternatively tag ID + backend call)
        // for the navigation fragment
        TagManagementFrag.targetBundle.putParcelable("tag", tag);
        TagManagementFrag.targetBundle.putParcelable("cluster", cluster);

        NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment_content_main);
        // Navigate to the Navigation Fragment with target Tag to update
        navController.navigate(R.id.tagEditFrag);
    }

}

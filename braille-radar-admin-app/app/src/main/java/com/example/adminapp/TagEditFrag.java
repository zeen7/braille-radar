package com.example.adminapp;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.location.Location;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.adminapp.api.ClusterService;
import com.example.adminapp.api.OrganizationService;
import com.example.adminapp.api.ServiceCallback;
import com.example.adminapp.api.TagService;
import com.example.adminapp.databinding.TagEditBinding;
import com.example.adminapp.model.Cluster;
import com.example.adminapp.model.Tag;
import com.example.adminapp.model.TagInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.List;

public class TagEditFrag extends Fragment {

    private TagEditBinding binding;
    private String selectedType;
    private TextView result;

    private LocationManager locationManager;
    private LocationListener locationListener;

    Double lati;
    Double longi;

    private final OrganizationService organizationService = new OrganizationService();
    private final ClusterService clusterService = new ClusterService();

    private Cluster selectedCluster = (Cluster) TagManagementFrag.targetBundle.get("cluster");
    private Cluster prevCluster = selectedCluster;

    private TagInfo selectedTag = (TagInfo) TagManagementFrag.targetBundle.get("tag");

    private boolean initTagType = true;
    private boolean initCluster = true;


    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = TagEditBinding.inflate(inflater, container, false);


        return binding.getRoot();


    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // LOCAL SAVING
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = preferences.edit();

        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                // Update your latitude and longitude fields here
                System.out.println("coords: "+ location.getLatitude()+" "+ location.getLongitude());
                lati = location.getLatitude();
                longi = location.getLongitude();
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
            }
        };

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling ActivityCompat#requestPermissions here to request the missing permissions
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, locationListener);

        // Set up the Spinner with tag types
        Spinner spinnerTagType = view.findViewById(R.id.editSpinnerTagType);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.tag_types_array,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTagType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(initTagType) {
                    initTagType = false;

                    // Select the previous type.
                    int targetPosition = findPositionByType(selectedTag.getType(), adapter);
                    selectedType = parent.getItemAtPosition(targetPosition).toString().toLowerCase();

                    // Set the selection to the position of the previous selected type
                    if (targetPosition != AdapterView.INVALID_POSITION) {
                        spinnerTagType.setSelection(targetPosition);
                    }
                } else {
                    selectedType = parent.getItemAtPosition(position).toString().toLowerCase();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // This method is called when nothing is selected in the Spinner

                // Doesn't work here for some reason...
//                // Select the previous type.
//                int position = adapter.getPosition(selectedTag.getType());
//
//                // Set the selection to the position of the previous selected type
//                if (position != AdapterView.INVALID_POSITION) {
//                    spinnerTagType.setSelection(position);
//                }
//
//                System.out.println("position: " + position);
            }
        });
        spinnerTagType.setAdapter(adapter);

        // Set up the Spinner with clusters
        Spinner spinnerCluster = view.findViewById(R.id.editSpinnerCluster);
        String selectedOrganization = preferences.getString("organizationId", null);
        System.out.println("TagCreation: selectedOrganization: " + selectedOrganization);
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
                Log.e("cluster", "Unable to fetch organization clusters");
            }
        });
//        String[] companyNamesArray = getResources().getStringArray(R.array.company_names_array);
//        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, companyNamesArray);
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        spinnerOrgName.setAdapter(adapter);


        spinnerCluster.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(initCluster) {
                    initCluster = false;

                    // Select the previous type.
                    int targetPosition = findPositionByCluster(selectedCluster, (ArrayAdapter<Cluster>) parent.getAdapter());
                    selectedCluster = (Cluster) parent.getItemAtPosition(targetPosition);

                    // Set the selection to the position of the previous selected type
                    if (targetPosition != AdapterView.INVALID_POSITION) {
                        spinnerCluster.setSelection(targetPosition);
                    }
                } else {
                    selectedCluster = (Cluster) parent.getItemAtPosition(position);
                }
                System.out.println("selectedCluster: " + selectedCluster.getName() + " (" + selectedCluster.getClusterId() + ")");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // This method is called when nothing is selected in the Spinner

//                // Select the element that has the same clusterId as the current selectedCluster!
//                // Iterate through the adapter to find the position of the item with matching clusterId
//                SpinnerAdapter adapter = (SpinnerAdapter) parent.getAdapter();
//                for (int i = 0; i < adapter.getCount(); i++) {
//                    Cluster cluster = (Cluster) adapter.getItem(i);
//                    if (cluster != null && cluster.getClusterId().equals(selectedCluster.getClusterId())) {
//                        // Found the matching item, set the selection
//                        spinnerCluster.setSelection(i);
//                        break;
//                    }
//                }
            }
        });

        // Set inputs for form
        binding.editInputDeviceName.setText(selectedTag.getDeviceName());
        binding.editInputTagName.setText(selectedTag.getName());
        binding.editInputLocation.setText(selectedTag.getLocation());
        binding.editInputFloor.setText(String.valueOf(selectedTag.getFloor()));
        binding.editInputLongitude.setText(String.valueOf(selectedTag.getCoordinates().longitude));
        binding.editInputLatitude.setText(String.valueOf(selectedTag.getCoordinates().latitude));

        // result text
        // Find the TextView by its ID
        result = view.findViewById(R.id.editTxtResult);

        binding.editSetCurrentLocationButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                binding.editInputLatitude.setText(String.valueOf(lati));
                binding.editInputLongitude.setText(String.valueOf(longi));
            }
        });

        // submit button
        binding.editBtnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TagService tagService = new TagService();
                System.out.println("coords: "+ lati +" "+ longi);

                Tag tag = new Tag();

                // Get inputs from form
                String deviceName = binding.editInputDeviceName.getText().toString();
                String tagName = binding.editInputTagName.getText().toString();
                String location = binding.editInputLocation.getText().toString();
                String tagType = selectedType;
                String floorString = binding.editInputFloor.getText().toString();
                String longitudeString = binding.editInputLongitude.getText().toString();
                String latitudeString = binding.editInputLatitude.getText().toString();
                // TODO: Get the list clusters and allow the user to add to the Clusters
                // Future TODO: Allow the Admin to manage Organization Clusters (CRUD, create and delete)


                if (deviceName.isEmpty() || tagName.isEmpty() || floorString.isEmpty() || longitudeString.isEmpty() || latitudeString.isEmpty()) {
                    // Show an error message to the user indicating that all fields must be filled
                    result.setText("Please fill all required values");
                    return;
                }

                int floor;
                Float longitude;
                Float latitude;
                try {
                    floor = Integer.parseInt(floorString);
                    longitude = Float.parseFloat(longitudeString);
                    latitude = Float.parseFloat(latitudeString);

                } catch (NumberFormatException e) {
                    // Show an error message to the user indicating that the floor must be a valid integer
                    result.setText("Floor, longitude, and latitude must be numbers.");
                    return;
                }

                tag.setDeviceName(deviceName);
                tag.setName(tagName);
                if(!location.equals("")) {
                    tag.setLocation(location);
                }
                tag.setType(tagType);
                tag.setFloor(floor);
                tag.setLongitude(longitude);
                tag.setLatitude(latitude);
                tagService.updateTag(selectedTag.getTagId(), tag.toJsonNode(), new ServiceCallback() {
                    @Override
                    public void onSuccess(JsonNode jsonData) {
//                String username = jsonData.get("username").toString();
//                System.out.println(username);
                        System.out.println("updateTag success! " + selectedTag.getName() + " (" + selectedTag.getTagId() + ")");
                        String success = "Success!";
                        result.setText(success);
                        jsonData = jsonData.get(0);
                        System.out.println(jsonData + " record(s) updated!");

                        // Add the tag to the chosen cluster
                        ObjectMapper objectMapper = new ObjectMapper();
                        // Create an ObjectNode
                        ObjectNode tagIdsObject = objectMapper.createObjectNode();
                        // Create an ArrayNode for the "tags" field
                        ArrayNode tagsArray = objectMapper.createArrayNode();
                        // Get the tagId from selectedTag
                        String tagId = selectedTag.getTagId();
                        // Add the tagId to the tagsArray
                        tagsArray.add(tagId);
                        // Set the "tags" field in the tagIdsObject
                        tagIdsObject.set("tags", tagsArray);
                        System.out.println("tagIdsObject: " + tagIdsObject.toString());

                        // TODO: Remove the tag from its current cluster if the chosen cluster is different
                        if(!(selectedCluster.getClusterId().equals(prevCluster.getClusterId()) && selectedCluster.getName().equals(prevCluster.getName()))) {
                            clusterService.removeTagsFromClusterByTagIds(prevCluster.getClusterId(), tagIdsObject, new ServiceCallback() {
                                @Override
                                public void onSuccess(JsonNode jsonData) {
                                    System.out.println("Tag: " + tagName + " (" + tagId + ") has been successfully removed from cluster "
                                            + prevCluster.getName() + " (" + prevCluster.getClusterId() + ")");
                                }

                                @Override
                                public void onFailure(Throwable t) {
                                    Log.e("clusterservice", "Failed to remove tag " + tagName + " (" + tagId + ") from cluster "
                                            + prevCluster.getName() + " (" + prevCluster.getClusterId() + ") " + t.toString());
                                }
                            });
                            clusterService.addTagsToClusterByTagIds(selectedCluster.getClusterId(), tagIdsObject, new ServiceCallback() {
                                @Override
                                public void onSuccess(JsonNode jsonData) {
                                    System.out.println("Tag: " + tagName + " (" + tagId + ") has been successfully added to cluster "
                                            + selectedCluster.getName() + " (" + selectedCluster.getClusterId() + ")");
                                }

                                @Override
                                public void onFailure(Throwable t) {
                                    Log.e("clusterservice", "Failed to add tag " + tagName + " (" + tagId + ") to cluster "
                                            + selectedCluster.getName() + " (" + selectedCluster.getClusterId() + ") " + t.toString());
                                }
                            });
                        }

                    }

                    @Override
                    public void onFailure(Throwable t) {
                        System.out.println(t.toString());
                        String error = "Error";
                        result.setText(error);
                        System.out.println("Error: updateTag()");
                    }
                });


//                tagService.getAllTags(new ServiceCallback() {
//                    @Override
//                    public void onSuccess(JsonNode jsonData) {
//                        System.out.println("getAllTags success!");
//                        System.out.println(jsonData.toString());
//                    }
//
//                    @Override
//                    public void onFailure(Throwable t) {
//                        System.out.println(t.toString());
//                        System.out.println("Error: getAllTags()");
//                    }
//                });
            }
        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private int findPositionByType(String targetType, ArrayAdapter<CharSequence> adapter) {
        for (int i = 0; i < adapter.getCount(); i++) {
            if (targetType.equalsIgnoreCase(adapter.getItem(i).toString())) {
                return i;
            }
        }
        return AdapterView.INVALID_POSITION;
    }

    private int findPositionByCluster(Cluster cluster, ArrayAdapter<Cluster> adapter) {
        for (int i = 0; i < adapter.getCount(); i++) {
            if (cluster.getClusterId().equals(adapter.getItem(i).getClusterId())
                    && cluster.getName().equals(adapter.getItem(i).getName())
            ) {
                return i;
            }
        }
        return AdapterView.INVALID_POSITION;
    }

}
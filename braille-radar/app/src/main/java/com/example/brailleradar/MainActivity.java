package com.example.brailleradar;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.PreferenceManager;

import com.example.brailleradar.databinding.ActivityMainBinding;
import com.example.brailleradar.models.TagInfo;
import com.example.brailleradar.services.ServiceCallback;
import com.example.brailleradar.services.TagService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements BottomNavigationView
        .OnNavigationItemSelectedListener{

    BottomNavigationView bottomNavigationView;
    private AppBarConfiguration appBarConfiguration;

    private boolean checkingPermission;

    public static boolean cacheLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate the layout first
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize bottomNavigationView after inflating the layout
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
        bottomNavigationView.setSelectedItemId(R.id.ExploreTab);


        // Keeps screen always on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        Log.d("mainactivity.oncreate", ""+(ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED));
        Log.d("mainactivity.oncreate", ""+(ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED));
        Log.d("mainactivity.oncreate", ""+(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED));

        checkingPermission = false;
        Log.d("mainactivity.oncreate", "Requesting permissions");
        requestPermissions();
        Log.d("mainactivity.oncreate", "Permissions granted");

        LocationObjectsSingleton.init(this, this);
        Scanner s = LocationObjectsSingleton.getScanner();
        GPS g = LocationObjectsSingleton.getGPS();
        Compass c = LocationObjectsSingleton.getCompass();

        Thread scannerThread = new Thread(s);
        scannerThread.start();
        g.startLocationUpdates();
        c.start();

        g.setLocationUpdateListener(new GPS.LocationUpdateListener() {
            @Override
            public void onLocationUpdated() {
                // Now that you have a location, you can make the API call
                performApiCall();

//                // TESTING
//                // Increment the tag usage data
//                TagService tagService = new TagService();
//                String name = "BR_001";
//                tagService.incrementTagUsageDataByDeviceName(name, new ServiceCallback() {
//                    @Override
//                    public void onSuccess(JsonNode tag) {
//                        Log.d("INFO", "Incremented " + name + " tag usage data. todaysPings: " + tag.get("todaysPings").asInt() + ", lastIntervalPings: " + tag.get("lastIntervalPings").asInt() + ", totalPings: " + tag.get("totalPings").asInt());
//                    }
//
//                    @Override
//                    public void onFailure(Throwable t) {
//                        Log.e("INFO", "Failed to increment tag usage data for " + name + ". " + t.toString());
//                    }
//                });
            }
        });

        // Load/Update the Favorites List based on StoredPreferences
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        Set<String> favouritesSet = preferences.getStringSet("FavouriteList", null);
        if(favouritesSet != null) {
            for(String favouriteTagId : favouritesSet) {
                FavouritesSingleton.setRecentTagList(favouriteTagId);
            }
        }


        // On destroy???
//        if (scannerThread != null) {
//            scannerThread.interrupt();
//        }
//        g.stopLocationUpdates();
//        c.stop();
    }

//    @Override
//    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
//        int itemId = item.getItemId();
//
//        Fragment fragment = null;
//
//        if (itemId == R.id.radarTab) {
//            fragment = new RadarFragment();
//        } else if (itemId == R.id.settingsTab) {
//            fragment = new SettingsFragment();
//        } else if (itemId == R.id.filterTab) {
//            fragment = new SignFilterFragment();
//        }
//
//        if (fragment != null) {
//            getSupportFragmentManager().beginTransaction()
//                    .replace(R.id.nav_host_fragment_content_main, fragment)
//                    .commit();
//
//            return true;
//        }
//
//        return false;
//    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);

        if (itemId == R.id.ExploreTab) {
            navController.navigate(R.id.ExploreFragment);
        } else if (itemId == R.id.settingsTab) {
            navController.navigate(R.id.SettingsFragment);
        }

        return true;
    }


    // Go back arrow
    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    private void requestPermissions() {
        // Build.VERSION.SDK_INT >= Build.VERSION_CODES.S (for older versions of android)
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN};
        checkingPermission = false;
        while (ActivityCompat.checkSelfPermission(getApplicationContext(), permissions[0]) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(getApplicationContext(), permissions[1]) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(getApplicationContext(), permissions[2]) != PackageManager.PERMISSION_GRANTED) {
            if (!checkingPermission) {
                checkingPermission = true;
                ActivityCompat.requestPermissions(this, permissions, 0);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int i = 0, len = permissions.length; i < len; i++) {
            String permission = permissions[i];
            if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                if (!shouldShowRequestPermissionRationale(permission)) {
                    finishAndRemoveTask();
//                } else {
//                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
//                    alertDialogBuilder
//                            .setTitle("Second Chance")
//                            .setMessage("Permission required for app to run, please retry? \uD83E\uDD7A")
//                            .setCancelable(false)
//                            .setPositiveButton("RETRY", (dialog, id) -> {
//                                ActivityCompat.requestPermissions(this, new String[]{permission}, 3);
//                                Intent i1 = new Intent(MainActivity.this, MainActivity.class);
//                                i1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
//                                startActivity(i1);
//                            })
//                            .setNegativeButton("EXIT", (dialog, id) -> {
//                                finish();
//                                dialog.cancel();
//                            })
//                            .create()
//                            .show();
                }
            }
        }
        checkingPermission = false;
    }

    public static List<TagInfo> findClosestPoints(GPS g, List<TagInfo> tagList) {
        // Create a copy of the tagList to avoid modifying the original list
        List<TagInfo> sortedPoints = new ArrayList<>(tagList);

        // Sort the copied list based on the distance from the current location
        sortedPoints.sort((tag1, tag2) -> {
            tag1.setCurrentDistance(g.calculateDistanceFromCurrent(tag1.getCoordinates())[0]);
            tag2.setCurrentDistance(g.calculateDistanceFromCurrent(tag2.getCoordinates())[0]);
            return Double.compare(tag1.getCurrentDistance(), tag2.getCurrentDistance());
        });

        // The list is now sorted by closest distance; no need to use subList as we want all elements sorted
        return sortedPoints;
    }

    public void performApiCall() {
        TagService service = new TagService();

        // Example API call: getTagByDeviceName
        service.getAllTagsWithClustersInfo(new ServiceCallback() {
                                               @Override
                                               public void onSuccess(JsonNode tag) {
                                                   cacheLoaded = true;

                                                   if (tag.isObject()) {
                                                       ObjectNode objectNode = (ObjectNode) tag;

                                                       System.out.println("someFunction: " + tag.toString());
                                                       Log.d("tagservice", "ObjectNode: " + tag.toString());


                                                   } else {
                                                       List <TagInfo> tagList = new ArrayList<>();
                                                       Log.d("tagservice", "GPS Updated! Updating TagListSingleton.");
//                       Log.d("tagservice", "otherNode: " + tag.toString());
                                                       String jsonString = tag.toString();
//                       Log.d("tagservice", "otherNode: " + tag.get(0).get("latitude").asText() + ", " + tag.get(0).get("longitude").asText());

                                                       for (int i = 0; i < tag.size(); i++) {
                                                           List aliasList = new ArrayList<>();
                                                           for (int j = 0; j < tag.get(i).get("aliases").size(); j++) {
                                                               aliasList.add(tag.get(i).get("aliases").get(j).asText());
                                                           }
                                                           List clusterNamesList = new ArrayList<>();
                                                           for (int j = 0; j < tag.get(i).get("Clusters").size(); j++) {
                                                               clusterNamesList.add(tag.get(i).get("Clusters").get(j).get("name").asText());
                                                           }
                                                           TagInfo newTag = new TagInfo(tag.get(i));
                                                           tagList.add(newTag);
                                                       }

//                       List<TagInfo> closestPoints = findClosestPoints(g, tagList);
//                       Coordinates currentLocation = g.getCurrentLocation();
//                       Log.d("tagservice1", currentLocation.latitude + " " + currentLocation.longitude);
//                       Log.d("tagservice1", closestPoints.get(0).getName());
                                                       TagListSingleton.setTagList(findClosestPoints(LocationObjectsSingleton.getGPS(), tagList));
                                                   }

                                               }

                                               @Override
                                               public void onFailure(Throwable t) {
                                                   System.out.println(t.toString());
                                               }
                                           }
        );
    }
}
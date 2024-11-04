package com.example.adminapp;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;

import com.example.adminapp.api.OrganizationService;
import com.example.adminapp.api.ServiceCallback;
import com.example.adminapp.databinding.ActivityMainBinding;
import com.example.adminapp.model.Organization;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements BottomNavigationView
        .OnNavigationItemSelectedListener{

    BottomNavigationView bottomNavigationView;
    private AppBarConfiguration appBarConfiguration;
    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), result -> {}
    );

    private ActivityMainBinding binding;
    private boolean checkingPermission;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
        bottomNavigationView.setSelectedItemId(R.id.UsageTab);

        setSupportActionBar(binding.toolbar);

        // Set the initial title
        setTitle(R.string.usage_graphs_fragment_label);

        while(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("mainactivity.oncreate", "ACCESS_FINE_LOCATION: HERE1!");
            requestPermission(Manifest.permission.ACCESS_FINE_LOCATION);
            Log.d("mainactivity.oncreate", "ACCESS_FINE_LOCATION: HERE!");

        }

        // LOCAL SAVING
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();

        String prevOrganizationId = preferences.getString("organizationId", null);
        String prevOrganizationName = preferences.getString("organizationName", null);
        if(prevOrganizationId == null) {
            OrganizationService organizationService = new OrganizationService();
            organizationService.getAllOrganizations(new ServiceCallback() {
                @Override
                public void onSuccess(JsonNode organizations) {
                    List<Organization> organizationsArray = new ArrayList<>(organizations.size());
                    for (JsonNode organization : organizations) {
                        organizationsArray.add(new Organization(organization.get("organizationId").asText(), organization.get("name").asText()));
                        break;
                    }
                    // Select the first organization
                    editor.putString("organizationId", organizationsArray.get(0).getOrgId());
                    editor.putString("organizationName", organizationsArray.get(0).getName());
                }

                @Override
                public void onFailure(Throwable t) {
                    Log.e("organization", "Unable to fetch organizations");
                }
            });
        }
    }



    private void requestPermission(@NonNull String permission) {
        // Build.VERSION.SDK_INT >= Build.VERSION_CODES.S (for older versions of android)
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), permission) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(permission);
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
                }
            }
        }
        checkingPermission = false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        return false;
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);

        if (itemId == R.id.RegistrationTab) {
            navController.navigate(R.id.tagCreationFrag);
            setTitle(R.string.add_data_fragment_label); // Set title dynamically
        } else if (itemId == R.id.settingsTab) {
            navController.navigate(R.id.adminCreationFrag);
            setTitle(R.string.settings_fragment_label); // Set title dynamically
        } else if (itemId == R.id.UsageTab) {
            navController.navigate(R.id.usageGraphFrag);
            setTitle(R.string.usage_graphs_fragment_label); // Set title dynamically
        } else if (itemId == R.id.ManagementTab) {
            navController.navigate(R.id.tagManagementFrag);
            setTitle(R.string.edit_data_fragment_label);
        }

        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        // Handle navigation back manually
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return navController.navigateUp();
    }
}

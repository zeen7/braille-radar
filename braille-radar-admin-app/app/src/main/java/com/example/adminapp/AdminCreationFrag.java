package com.example.adminapp;

import android.content.SharedPreferences;
import android.content.res.Resources;
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

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.adminapp.api.OrganizationService;
import com.example.adminapp.api.ServiceCallback;
import com.example.adminapp.databinding.AdminCreationBinding;
import com.example.adminapp.model.Admin;
import com.example.adminapp.model.Organization;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;


public class AdminCreationFrag extends Fragment {
    private AdminCreationBinding binding;

    private TextView result;
    private OrganizationService organizationService = new OrganizationService();

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = AdminCreationBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // LOCAL SAVING
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = preferences.edit();

        // Set up the Spinner with company names
        Spinner spinnerOrgName = view.findViewById(R.id.spinnerOrgName);
        // TODO: Populate this with the Organization in the backend (create a model to store the name and the organizationId)
        organizationService.getAllOrganizations(new ServiceCallback() {
            @Override
            public void onSuccess(JsonNode organizations) {
                List<Organization> organizationsArray = new ArrayList<>(organizations.size());
                for(JsonNode organization : organizations) {
                    organizationsArray.add(new Organization(organization.get("organizationId").asText(), organization.get("name").asText()));
                }
                ArrayAdapter<Organization> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, organizationsArray);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerOrgName.setAdapter(adapter);

                // Set the default to the first element or the last element.
                String prevOrganizationId = preferences.getString("organizationId", organizationsArray.get(0).getOrgId());
                String prevOrganizationName = preferences.getString("organizationName", organizationsArray.get(0).getName());
                System.out.println("prevOrganizationId: " + prevOrganizationId + ", prevOrganizationName: " + prevOrganizationName);
                int position = 0;
                for(int i = 0; i < organizationsArray.size(); i++) {
                    if(organizationsArray.get(i).getName().equals(prevOrganizationName)
                            && organizationsArray.get(i).getOrgId().equals(prevOrganizationId)) {
                        position = i;
                        break;
                    }
                }
                spinnerOrgName.setSelection(position);
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e("organization", "Unable to fetch organizations");
            }
        });
//        String[] companyNamesArray = getResources().getStringArray(R.array.company_names_array);
//        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, companyNamesArray);
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        spinnerOrgName.setAdapter(adapter);


        spinnerOrgName.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Organization selectedOrganization = (Organization) parent.getItemAtPosition(position);
                editor.putString("organizationName", selectedOrganization.getName());
                editor.putString("organizationId", selectedOrganization.getOrgId());
                editor.apply();
                System.out.println("selectedOrganization: " + selectedOrganization.getName() + " (" + selectedOrganization.getOrgId() + ")");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // This method is called when nothing is selected in the Spinner
            }
        });

        // result text
        super.onViewCreated(view, savedInstanceState);
        // Find the TextView by its ID
        result = view.findViewById(R.id.txtResult);

        // submit values
        binding.btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //AdminService adminService = new AdminService();

                // get inputs from page
                String username = binding.inputAdminName.getText().toString();
//                String organization_name = selectedOrganization.getName();
                String password = binding.inputPassword.getText().toString();



                // hardcoded organization for now, eventually will run getOrganizations() or smth
                // validate existence and also whether there's already an admin for it?


//                String organizationIdString = "a43f8d1b-d2ed-4c6f-84b2-203764b6d635"; // tech corp
                String organizationIdString = preferences.getString("organizationId", null);
                System.out.println(organizationIdString);
//                organizationIdString = getOrganizationIdByCompanyName(organization_name);
//                System.out.println(organizationIdString);
//                ObjectId organizationId = new ObjectId(organizationIdString);


                // Create an Admin object with the retrieved values
                    // getters and setters are generated automatically at runtime
                Admin admin = new Admin();
                admin.setUsername(username);
                admin.setPassword(password);
                admin.setOrganizationId(organizationIdString);
                admin.setEmail("email2@test.com");

//                adminService.createAdmin(admin, new ServiceCallback() {
//                        @Override
//                    public void onSuccess(JsonNode jsonData) {
////                String username = jsonData.get("username").toString();
////                System.out.println(username);
//                        System.out.println("createAdmin() success!");
//                        String success = "Success!";
//                        result.setText(success);
//                        System.out.println(jsonData.toString());
//                    }
//
//                    @Override
//                    public void onFailure(Throwable t) {
//                        System.out.println(t.toString());
//                        String error = "Error";
//                        result.setText(error);
//                        System.out.println("Error: createAdmin()");
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

    // hardcoded workaround for demo
    private String getOrganizationIdByCompanyName(String companyName) {
        Resources res = getResources();
        String[] companyNamesArray = res.getStringArray(R.array.company_names_array);
        String[] companyIdsArray = res.getStringArray(R.array.company_ids_array);

        int index = -1;
        for (int i = 0; i < companyNamesArray.length; i++) {
            if (companyName.equals(companyNamesArray[i])) {
                index = i;
                break;
            }
        }

        if (index != -1) {
            return companyIdsArray[index];
        } else {
            return null; // Return null or handle error for unknown company names
        }
    }


}
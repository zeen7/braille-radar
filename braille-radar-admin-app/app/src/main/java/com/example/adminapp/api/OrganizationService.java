package com.example.adminapp.api;

import com.fasterxml.jackson.databind.JsonNode;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class OrganizationService {
    private ApiService apiService;

    public OrganizationService() {
        // Create the Retrofit instance with Jackson Converter
        Retrofit retrofit = new Retrofit.Builder()
                // This should the the IP address of your computer running the emulator (ipconfig/ifconfig in terminal)
                // Define them in host and port in `local.properties`
//                .baseUrl("http://" + System.getProperty("host") + ":" + System.getProperty("port") + "/")
                .baseUrl("http://15.157.124.12:3000/")
                .addConverterFactory(JacksonConverterFactory.create())
                .build();

        // Create the ApiService directly
        apiService = retrofit.create(ApiService.class);
    }

    public void getAllOrganizations(ServiceCallback callback) {
        Call<JsonNode> call = apiService.getAllOrganizations();
        call.enqueue(new JsonCallback(callback));
    }


    public void getAllOrganizationClusters(String organizationId, ServiceCallback callback) {
        Call<JsonNode> call = apiService.getAllOrganizationClusters(organizationId);
        call.enqueue(new JsonCallback(callback));
    }

}

package com.example.adminapp.api;

import com.fasterxml.jackson.databind.JsonNode;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class UserService {
    private ApiService apiService;

    public UserService() {
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

    public void getUsers(ServiceCallback callback) {
        Call<JsonNode> call = apiService.getUsers();
        call.enqueue(new JsonCallback(callback));
    }
}

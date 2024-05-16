package com.example.brailleradar.services;
import com.fasterxml.jackson.databind.JsonNode;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class TagService {
    private ApiService apiService;

    public TagService() {
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

    public void getAllTags(ServiceCallback callback) {
        Call<JsonNode> call = apiService.getAllTags();
        call.enqueue(new JsonCallback(callback));
    }

    public void getTagByDeviceName(String deviceName, ServiceCallback callback) {
        Call<JsonNode> call = apiService.getTagByDeviceName(deviceName);
        call.enqueue(new JsonCallback(callback));
    }

    public void createTag(JsonNode tag, ServiceCallback callback) {
        Call<JsonNode> call = apiService.createTag(tag);
        call.enqueue(new JsonCallback(callback));
    }

    public void getTagClusters(String tagId, ServiceCallback callback) {
        Call<JsonNode> call = apiService.getTagClusters(tagId);
        call.enqueue(new JsonCallback(callback));
    }

    public void getAllTagsWithClustersInfo(ServiceCallback callback) {
        Call<JsonNode> call = apiService.getAllTagsWithClustersInfo();
        call.enqueue(new JsonCallback(callback));
    }

    public void incrementTagUsageDataByTagId(String tagId, ServiceCallback callback) {
        Call<JsonNode> call = apiService.incrementTagUsageDataByTagId(tagId);
        call.enqueue(new JsonCallback(callback));
    }

    public void incrementTagUsageDataByDeviceName(String deviceName, ServiceCallback callback) {
        Call<JsonNode> call = apiService.incrementTagUsageDataByDeviceName(deviceName);
        call.enqueue(new JsonCallback(callback));
    }
}

package com.example.brailleradar.services;
import com.fasterxml.jackson.databind.JsonNode;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public class ClusterService {
    private ApiService apiService;

    public ClusterService() {
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

    public void getAllClusters(ServiceCallback callback) {
        Call<JsonNode> call = apiService.getAllClusters();
        call.enqueue(new JsonCallback(callback));
    }


    public void getClusterTags(String clusterId, ServiceCallback callback) {
        Call<JsonNode> call = apiService.getClusterTags(clusterId);
        call.enqueue(new JsonCallback(callback));
    }

    /*
        tagIdsObject: { "tags": [<array of tagId strings>] }
     */

    public void addTagsToClusterByTagIds(String clusterId, JsonNode tagIdsObject, ServiceCallback callback) {
        Call<JsonNode> call = apiService.addTagsToClusterByTagIds(clusterId, tagIdsObject);
        call.enqueue(new JsonCallback(callback));
    }

    public void removeTagsFromClusterByTagIds(String clusterId, JsonNode tagIdsObject, ServiceCallback callback) {
        Call<JsonNode> call = apiService.removeTagsFromClusterByTagIds(clusterId, tagIdsObject);
        call.enqueue(new JsonCallback(callback));
    }


}

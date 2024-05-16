package com.example.adminapp.api;

import com.fasterxml.jackson.databind.JsonNode;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ApiService {
    @GET("api/users/")
    Call<JsonNode> getUsers();


    @GET("api/tags/")
    Call<JsonNode> getAllTags();
    @GET("api/tags/clusters")
    Call<JsonNode> getAllTagsWithClustersInfo();

    @GET("api/tags/device/{deviceName}")
    Call<JsonNode> getTagByDeviceName(@Path("deviceName") String deviceName);

    @POST("api/tags/")
    Call<JsonNode> createTag(@Body JsonNode tag);
    @PUT("api/tags/id/{tagId}")
    Call<JsonNode> updateTag(@Path("tagId") String tagId, @Body JsonNode tag);

    @GET("api/tags/id/{tagId}/clusters")
    Call<JsonNode> getTagClusters(@Path("tagId") String tagId);
    @DELETE("api/tags/id/{tagId}")
    Call<JsonNode> deleteTagById(@Path("tagId") String tagId);


    @GET("api/clusters/")
    Call<JsonNode> getAllClusters();

    @GET("api/clusters/id/{clusterId}/tags")
    Call<JsonNode> getClusterTags(@Path("clusterId") String clusterId);

    @GET("api/clusters/id/{clusterId}/tags/clusters")
    Call<JsonNode> getClusterTagsWithClustersInfo(@Path("clusterId") String clusterId);

    /*
        tagIdsObject: { "tags": [<array of tagId strings>] }
     */
    @POST("api/clusters/id/{clusterId}/tags")
    Call<JsonNode> addTagsToClusterByTagIds(@Path("clusterId") String clusterId, @Body JsonNode tagIdsObject);
    @HTTP(method = "DELETE", path = "api/clusters/id/{clusterId}/tags", hasBody = true)
    Call<JsonNode> removeTagsFromClusterByTagIds(@Path("clusterId") String clusterId, @Body JsonNode tagIdsObject);



    @GET("api/organizations")
    Call<JsonNode> getAllOrganizations();
    @GET("api/organizations/id/{organizationId}/clusters")
    Call<JsonNode> getAllOrganizationClusters(@Path("organizationId") String organizationId);


}
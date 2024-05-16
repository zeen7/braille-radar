package com.example.brailleradar.services;

import com.fasterxml.jackson.databind.JsonNode;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class JsonCallback implements Callback<JsonNode> {
    private ServiceCallback callback;

    public JsonCallback(ServiceCallback callback) {
        this.callback = callback;
    }

    @Override
    public void onResponse(Call<JsonNode> call, Response<JsonNode> response) {
        if (response.isSuccessful()) {
            JsonNode jsonData = response.body();
            if (jsonData != null) {
                callback.onSuccess(jsonData);
            } else {
                // Handle null JSON response
                callback.onFailure(new NullPointerException("JSON data is null"));
            }
        } else {
            // Handle API error
            callback.onFailure(new Exception("API request failed with code: " + response.code()));
        }
    }

    @Override
    public void onFailure(Call<JsonNode> call, Throwable t) {
        // Handle network failure
        callback.onFailure(t);
    }
}

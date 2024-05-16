package com.example.brailleradar.services;

import com.fasterxml.jackson.databind.JsonNode;

public interface ServiceCallback {
    void onSuccess(JsonNode jsonData);
    void onFailure(Throwable t);
}
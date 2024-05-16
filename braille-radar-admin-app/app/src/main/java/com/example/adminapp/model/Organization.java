package com.example.adminapp.model;

public class Organization {
    private String organizationId;
    private String name;
    public Organization(String organizationId, String name) {
        this.organizationId = organizationId;
        this.name = name;
    }
    public String getOrgId() {
        return organizationId;
    }
    public String getName() {
        return name;
    }
    public String toString() {
        return name;
    }
}

package com.example.odsfilearchiver.model;

public class DataMart {

    private Integer id;
    private String interfaceId;
    private String type;

    public DataMart() {
    }

    public DataMart(Integer id, String interfaceId, String type) {
        this.id = id;
        this.interfaceId = interfaceId;
        this.type = type;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getInterfaceId() {
        return interfaceId;
    }

    public void setInterfaceId(String interfaceId) {
        this.interfaceId = interfaceId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "Data Mart with ID: " + this.getId() + " - INTERFACE ID: '" + this.getInterfaceId() + "' - TYPE: '" + this.getType() + "'";
    }
}

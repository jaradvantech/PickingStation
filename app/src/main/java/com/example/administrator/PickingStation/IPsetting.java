package com.example.administrator.PickingStation;


public class IPsetting {

    public String name;
    public String address;
    public String port;

    public IPsetting(String name, String address, String port) {
        this.name = name;
        this.address = address;
        this.port = port;
    }

    public String toString() {
        return this.address + " : " + this.port;
    }
}

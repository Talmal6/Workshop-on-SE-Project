package com.SEGroup.DTO;

import com.SEGroup.Domain.User.Address;

public class AddressDTO {
    private String address;
    private String city;
    private String country;
    private String zip;

    public AddressDTO() {
    }

    public AddressDTO(String address, String city, String country, String zip) {
        this.address = address;
        this.city = city;
        this.country = country;
        this.zip = zip;
    }

    public AddressDTO(Address addressDTO) {
        this.address = addressDTO.getAddress();
        this.city = addressDTO.getCity();
        this.country = addressDTO.getCountry();
        this.zip = addressDTO.getZip();
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }
} 
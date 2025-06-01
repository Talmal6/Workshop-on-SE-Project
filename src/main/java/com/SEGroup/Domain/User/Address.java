package com.SEGroup.Domain.User;
import com.SEGroup.DTO.AddressDTO;

import jakarta.persistence.Embeddable;




@Embeddable
public class Address {
    private String address;
    private String city;
    private String country;
    private String zip;

    public Address() {
        // Required by JPA
    }

    public Address(String address, String city, String country, String zip) {
        this.address = address;
        this.city = city;
        this.country = country;
        this.zip = zip;
    }

    public Address(AddressDTO addressDTO) {
        this.address = addressDTO.getAddress();
        this.city = addressDTO.getCity();
        this.country = addressDTO.getCountry();
        this.zip = addressDTO.getZip();
    }

    // Getters
    public String getAddress() {
        return address;
    }

    public String getCity() {
        return city;
    }

    public String getCountry() {
        return country;
    }

    public String getZip() {
        return zip;
    }

    // Setters
    public void setAddress(String address) {
        this.address = address;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    @Override
    public String toString() {
        return address + ", " + city + ", " + country + " (" + zip + ")";
    }
}

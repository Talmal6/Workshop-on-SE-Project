package com.SEGroup.UI;

public record UserSession(String jwt, String email){

    private static UserSession current;
    public static void set(UserSession s){ current = s; }
    public static UserSession get(){ return current; }
}
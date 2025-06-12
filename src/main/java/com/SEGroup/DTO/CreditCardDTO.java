package com.SEGroup.DTO;

import jakarta.persistence.Embeddable;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Embeddable
public class CreditCardDTO {
    // Replace these with secure storage / environment variables in real applications
    private static final String SECRET_KEY = "1234567890abcdef";  // 16-byte key (128-bit)
    private static final String INIT_VECTOR = "abcdef1234567890"; // 16-byte IV

    // Encrypted fields
    private String encryptedCardNumber;
    private String encryptedCvv;

    // Other fields
    private String cardHolder;
    private String expiryDate;
    private String address;
    private String city;
    private String zipCode;
    private String country;
    private String id;

    // ─── Encryption / Decryption ─────────────────────────────────────────

    private String encrypt(String plainText) {
        try {
            IvParameterSpec iv = new IvParameterSpec(INIT_VECTOR.getBytes(StandardCharsets.UTF_8));
            SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");

            cipher.init(Cipher.ENCRYPT_MODE, keySpec, iv);
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed", e);
        }
    }

    private String decrypt(String encryptedText) {
        try {
            IvParameterSpec iv = new IvParameterSpec(INIT_VECTOR.getBytes(StandardCharsets.UTF_8));
            SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");

            cipher.init(Cipher.DECRYPT_MODE, keySpec, iv);
            byte[] original = cipher.doFinal(Base64.getDecoder().decode(encryptedText));
            return new String(original, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed", e);
        }
    }

    public CreditCardDTO(){

    }

    public CreditCardDTO(String cardNumber, String cvv, String cardHolder, String expiryDate,
                          String address, String city, String zipCode, String country, String id) {
        if (cardNumber == null || cvv == null || cardHolder == null || expiryDate == null ||
            address == null || city == null || zipCode == null || country == null || id == null) {
            throw new IllegalArgumentException("All fields must be provided");
        }

        this.encryptedCardNumber = encrypt(cardNumber);
        this.encryptedCvv = encrypt(cvv);
        this.cardHolder = cardHolder;
        this.expiryDate = expiryDate;
        this.address = address;
        this.city = city;
        this.zipCode = zipCode;
        this.country = country;
        this.id = id;
    }

    // ─── Getters and Setters ─────────────────────────────────────────────

    public String getCardNumber() {
        return encryptedCardNumber == null ? null : decrypt(encryptedCardNumber);
    }

    public void setCardNumber(String cardNumber) {
        this.encryptedCardNumber = encrypt(cardNumber);
    }

    public String getCvv() {
        return encryptedCvv == null ? null : decrypt(encryptedCvv);
    }

    public void setCvv(String cvv) {
        this.encryptedCvv = encrypt(cvv);
    }

    public String getCardHolder() {
        return cardHolder;
    }

    public void setCardHolder(String cardHolder) {
        this.cardHolder = cardHolder;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
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

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    // ─── JSON Representation ────────────────────────────────────────────

    @Override
    public String toString() {
        try {
            return '{' +
                    "\"card_number\":\"" + getCardNumber() + "\"," +
                    "\"holder\":\"" + cardHolder + "\"," +
                    "\"month\":\"" + (expiryDate != null ? expiryDate.substring(0, 2) : "") + "\"," +
                    "\"year\":\"" + (expiryDate != null ? expiryDate.substring(3) : "") + "\"," +
                    "\"cvv\":\"" + getCvv() + "\"," +
                    "\"address\":\"" + address + "\"," +
                    "\"city\":\"" + city + "\"," +
                    "\"zipCode\":\"" + zipCode + "\"," +
                    "\"country\":\"" + country + '"' +
                    '}';
        } catch (Exception e) {
            return "{\"error\":\"" + e.getMessage() + "\"}";
        }
    }
}

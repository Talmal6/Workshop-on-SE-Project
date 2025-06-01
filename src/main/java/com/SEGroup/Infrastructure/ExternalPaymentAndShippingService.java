package com.SEGroup.Infrastructure;

import com.SEGroup.DTO.AddressDTO;
import com.SEGroup.Domain.IPaymentGateway;
import com.SEGroup.Domain.IShippingService;
import com.SEGroup.Service.LoggerWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringJoiner;

import static java.lang.Integer.parseInt;

//create a new enum
public class ExternalPaymentAndShippingService implements IPaymentGateway , IShippingService {
    private static final String SERVER_URL = "https://damp-lynna-wsep-1984852e.koyeb.app/";
    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Override
    public Boolean cancelShipping(int shippingId) {
        LoggerWrapper.info("cancelShipping called with shippingId=" + shippingId);
        return null;
    }

    @Override
    public Integer ship(AddressDTO address_detail, String name) {
        LoggerWrapper.info("ship called with address=" + address_detail + ", name=" + name);
        Map<String, String> toSend = new LinkedHashMap<>();
        toSend.put("name", name);
        toSend.put("action_type", Action.SUPPLY.value());
        toSend.put("address", address_detail.toString());
        toSend.put("city", address_detail.getCity());
        toSend.put("country", address_detail.getCountry());
        toSend.put("zip", address_detail.getZip());
        LoggerWrapper.info("sending shipping request to external service: " + toSend);
        String result=sendPost(toSend);
        LoggerWrapper.info("Received response for shipping request: " + result);
        int ok = parseInt(result);
        if (ok < 0){
            RuntimeException e = new RuntimeException("The transaction has failed");
            LoggerWrapper.error("The transaction has failed with response: " + result, e);
            throw e;
        }
        LoggerWrapper.info("Shipping request successful, response: " + result);
        return ok;
    }

    public enum Action {
        HANDSHAKE("handshake"),
        PAY("pay"),
        CANCEL_PAY("cancel_pay"),
        SUPPLY("supply"),
        CANCEL_SUPPLY("cancel_supply");

        private final String value;

        Action(String value) {
            this.value = value;
        }

        public String value() {
            return value;
        }
    }

    @Override
    public void processPayment(String paymentDetails, double amount) {
        LoggerWrapper.info("processPayment called with amount=" + amount + ", rawDetails=" + paymentDetails);
        Map<String, String> toSend = parsePayment(paymentDetails);
        LoggerWrapper.debug("Parsed payment details: " + toSend);
        String result=sendPost(toSend);
        LoggerWrapper.info("Payment gateway response: " + result);
        int ok = parseInt(result);
        if (ok < 0){
            RuntimeException e = new RuntimeException("The transaction has failed");
            LoggerWrapper.error("Payment transaction failed with response: " + result, e);
            throw e;
        }
    }

    @Override
    public boolean validatePayment(String paymentDetails) {
        LoggerWrapper.info("validatePayment called");
        String ok = sendPost(Map.of("action_type", Action.HANDSHAKE.value()));
        LoggerWrapper.debug("Handshake response: " + ok);
        return "OK".equalsIgnoreCase(ok.trim());
    }

    @Override
    public String getPaymentStatus(String transactionId) {
        LoggerWrapper.info("getPaymentStatus called with transactionId=" + transactionId);
        if (transactionId == null || transactionId.isEmpty()) {
            return "failed";
        }
        try {
            int ok = Integer.parseInt(transactionId);
            if (ok < 0) {
                return "failed";
            } else {
                return "success";
            }
        } catch (NumberFormatException e) {
            return "failed";
        }
    }

    public Map<String, String> parsePayment(String rawPayment) {
        LoggerWrapper.debug("Parsing payment JSON: " + rawPayment);
        try {
            // keeps insertion order; change to HashMap if you don't care
            return new ObjectMapper().readValue(
                    rawPayment,
                    new TypeReference<LinkedHashMap<String, String>>() {}
            );
        } catch (Exception e) {
            LoggerWrapper.error("Invalid payment JSON", e);
            throw new IllegalArgumentException("Invalid payment JSON", e);
        }
    }

    private static String sendPost(Map<String, String> formParams) {
        LoggerWrapper.debug("sendPost called with params: " + formParams);
        StringJoiner body = new StringJoiner("&");
        formParams.forEach((k, v) ->
                body.add(URLEncoder.encode(k, StandardCharsets.UTF_8) + "=" +
                        URLEncoder.encode(v, StandardCharsets.UTF_8)));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(SERVER_URL))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .timeout(Duration.ofSeconds(10))
                .build();
        try {
            HttpResponse<String> resp = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            LoggerWrapper.debug("HTTP POST response (status " + resp.statusCode() + "): " + resp.body());
            return resp.body();
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            LoggerWrapper.error("Failed contacting external payment service", e);
            throw new RuntimeException("Failed contacting external payment service", e);
        }
    }
}

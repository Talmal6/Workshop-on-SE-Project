package com.SEGroup.Infrastructure;

import com.SEGroup.DTO.AddressDTO;
import com.SEGroup.Domain.IPaymentGateway;
import com.SEGroup.Domain.IShippingService;
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
        return null;
    }

    @Override
    public Integer ship(AddressDTO address_detail, String name) {
        Map<String, String> toSend = new LinkedHashMap<>();
        toSend.put("name", name);
        toSend.put("action_type", Action.SUPPLY.value());
        toSend.put("address", address_detail.toString());
        toSend.put("city", address_detail.getCity());
        toSend.put("country", address_detail.getCountry());
        toSend.put("zip", address_detail.getZip());
        String result=sendPost(toSend);
        int ok = parseInt(result);
        if (ok < 0){
            throw new RuntimeException("The transaction has failed");
        }
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
        Map<String, String> toSend = parsePayment(paymentDetails);
        System.out.println("YehudaaaaaaaPayment: " + toSend);
        String result=sendPost(toSend);
        System.out.println("YehudaaaaaaaPayment result: " + result);
        int ok = parseInt(result);
        if (ok < 0){
            throw new RuntimeException("The transaction has failed");
        }
    }

    @Override
    public boolean validatePayment(String paymentDetails) {
        String ok = sendPost(Map.of("action_type", Action.HANDSHAKE.value()));
        return "OK".equalsIgnoreCase(ok.trim());
    }

    @Override
    public String getPaymentStatus(String transactionId) {
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
        try {
            // keeps insertion order; change to HashMap if you don't care
            return new ObjectMapper().readValue(
                    rawPayment,
                    new TypeReference<LinkedHashMap<String, String>>() {}
            );
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid payment JSON", e);
        }
    }

    private static String sendPost(Map<String, String> formParams) {
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

        System.out.println("Yehudaa"+request.toString());
        try {
            HttpResponse<String> resp = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            return resp.body();
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Failed contacting external payment service", e);
        }
    }
}

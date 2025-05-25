package com.SEGroup.Infrastructure;

import com.SEGroup.Domain.IPaymentGateway;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.StringJoiner;

import static java.lang.Integer.parseInt;
//create a new enum

public class ExternalPaymentService implements IPaymentGateway {
    private static final String SERVER_URL = "https://damp-lynna-wsep-1984852e.koyeb.app/";
    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

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
        int ok = parseInt(sendPost(toSend));
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
        Map<String, String> paymentDetails = Map.of();
        return paymentDetails;
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

        try {
            HttpResponse<String> resp = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            return resp.body();
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Failed contacting external payment service", e);
        }
    }
}

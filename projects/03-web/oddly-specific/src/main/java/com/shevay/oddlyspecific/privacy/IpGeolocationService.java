package com.shevay.oddlyspecific.privacy;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class IpGeolocationService {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public IpGeolocationService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public static class IpLocationResult {
        private final String city;
        private final String region;
        private final String country;
        private final Double latitude;
        private final Double longitude;
        private final String isp;

        public IpLocationResult(String city, String region, String country, Double latitude, Double longitude, String isp) {
            this.city = city;
            this.region = region;
            this.country = country;
            this.latitude = latitude;
            this.longitude = longitude;
            this.isp = isp;
        }

        public String getCity() { return city; }
        public String getRegion() { return region; }
        public String getCountry() { return country; }
        public Double getLatitude() { return latitude; }
        public Double getLongitude() { return longitude; }
        public String getIsp() { return isp; }
    }

    public IpLocationResult resolveIpLocation(String ip) {
        if (ip == null || ip.isBlank() || isPrivateOrLoopbackIp(ip)) {
            return new IpLocationResult("Localhost Environment", "Local Network", "Internal Loopback", 0.0, 0.0, "Local Development Host");
        }

        try {
            String apiUrl = "http://ip-api.com/json/" + ip + "?fields=status,country,regionName,city,lat,lon,isp";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .timeout(Duration.ofSeconds(2))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode json = objectMapper.readTree(response.body());
                if ("success".equalsIgnoreCase(json.path("status").asText())) {
                    String city = json.path("city").asText("Unknown City");
                    String region = json.path("regionName").asText("Unknown Region");
                    String country = json.path("country").asText("Unknown Country");
                    Double lat = json.hasNonNull("lat") ? json.path("lat").asDouble() : 0.0;
                    Double lon = json.hasNonNull("lon") ? json.path("lon").asDouble() : 0.0;
                    String isp = json.path("isp").asText("Unknown ISP");

                    return new IpLocationResult(city, region, country, lat, lon, isp);
                }
            }
        } catch (Exception e) {
            System.err.println("IP Geolocation lookup skipped/failed for IP " + ip + ": " + e.getMessage());
        }

        return new IpLocationResult("Network Gateway", "Public Network", "External Location", 0.0, 0.0, "Internet Service Provider");
    }

    private boolean isPrivateOrLoopbackIp(String ip) {
        return "127.0.0.1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip) ||
                ip.startsWith("10.") || ip.startsWith("192.168.") || ip.startsWith("172.16.") ||
                ip.startsWith("172.17.") || ip.startsWith("172.18.") || ip.startsWith("172.19.") ||
                ip.startsWith("172.2") || ip.startsWith("172.30.") || ip.startsWith("172.31.");
    }
}

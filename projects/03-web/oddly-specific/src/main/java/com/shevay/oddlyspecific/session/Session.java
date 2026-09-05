package com.shevay.oddlyspecific.session;

public class Session {
    private final String sessionId;
    private final String selectedChallengeId;
    private final String connectionIp;
    private final long createdAt;
    
    private boolean locationGranted;
    private Double latitude;
    private Double longitude;
    private Double accuracy;
    private boolean completed;

    // IP Geolocation fields
    private String ipCity;
    private String ipRegion;
    private String ipCountry;
    private Double ipLatitude;
    private Double ipLongitude;
    private String isp;

    // Client Environment fields
    private String userAgent;
    private String platform;
    private String language;
    private String timezone;
    private String screenResolution;

    public Session(String sessionId, String selectedChallengeId, String connectionIp) {
        this.sessionId = sessionId;
        this.selectedChallengeId = selectedChallengeId;
        this.connectionIp = connectionIp;
        this.createdAt = System.currentTimeMillis();
        this.locationGranted = false;
        this.completed = false;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getSelectedChallengeId() {
        return selectedChallengeId;
    }

    public String getConnectionIp() {
        return connectionIp;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public boolean isLocationGranted() {
        return locationGranted;
    }

    public void setLocationGranted(boolean locationGranted) {
        this.locationGranted = locationGranted;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(Double accuracy) {
        this.accuracy = accuracy;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public String getIpCity() { return ipCity; }
    public void setIpCity(String ipCity) { this.ipCity = ipCity; }

    public String getIpRegion() { return ipRegion; }
    public void setIpRegion(String ipRegion) { this.ipRegion = ipRegion; }

    public String getIpCountry() { return ipCountry; }
    public void setIpCountry(String ipCountry) { this.ipCountry = ipCountry; }

    public Double getIpLatitude() { return ipLatitude; }
    public void setIpLatitude(Double ipLatitude) { this.ipLatitude = ipLatitude; }

    public Double getIpLongitude() { return ipLongitude; }
    public void setIpLongitude(Double ipLongitude) { this.ipLongitude = ipLongitude; }

    public String getIsp() { return isp; }
    public void setIsp(String isp) { this.isp = isp; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }

    public String getScreenResolution() { return screenResolution; }
    public void setScreenResolution(String screenResolution) { this.screenResolution = screenResolution; }
}

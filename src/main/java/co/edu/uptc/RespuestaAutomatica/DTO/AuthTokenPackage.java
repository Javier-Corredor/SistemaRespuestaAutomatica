package co.edu.uptc.RespuestaAutomatica.DTO;

public class AuthTokenPackage {
    private String accessToken;
    private String refreshToken;
    private String authScheme;

    public AuthTokenPackage() {}

    public AuthTokenPackage(String accessToken, String refreshToken, String authScheme) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.authScheme = authScheme;
    }

    public static Builder builder() { return new Builder(); }

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    public String getAuthScheme() { return authScheme; }
    public void setAuthScheme(String authScheme) { this.authScheme = authScheme; }

    public static class Builder {
        private String accessToken;
        private String refreshToken;
        private String authScheme;
        public Builder accessToken(String accessToken) { this.accessToken = accessToken; return this; }
        public Builder refreshToken(String refreshToken) { this.refreshToken = refreshToken; return this; }
        public Builder authScheme(String authScheme) { this.authScheme = authScheme; return this; }
        public AuthTokenPackage build() { return new AuthTokenPackage(accessToken, refreshToken, authScheme); }
    }
}

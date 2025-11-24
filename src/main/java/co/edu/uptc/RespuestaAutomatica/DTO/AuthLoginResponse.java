package co.edu.uptc.RespuestaAutomatica.DTO;

public class AuthLoginResponse {
    private String accessToken;
    private String refreshToken;
    private String authScheme;
    private String username;
    private String email;
    private String role;

    public AuthLoginResponse() {}

    public AuthLoginResponse(String accessToken, String refreshToken, String authScheme, String username, String email, String role) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.authScheme = authScheme;
        this.username = username;
        this.email = email;
        this.role = role;
    }

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    public String getAuthScheme() { return authScheme; }
    public void setAuthScheme(String authScheme) { this.authScheme = authScheme; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
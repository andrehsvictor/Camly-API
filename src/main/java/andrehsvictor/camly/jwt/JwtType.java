package andrehsvictor.camly.jwt;

public enum JwtType {
    ACCESS("access"),
    REFRESH("refresh");

    private final String type;

    JwtType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}

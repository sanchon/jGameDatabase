package xyz.sanchon.jgamedatabase.dto;

/** Resultado de búsqueda en la tienda Steam (para JSON hacia el formulario de deseados). */
public class SteamSearchHitDto {

    private long appId;
    private String name;
    private String tinyImage;

    public SteamSearchHitDto() {
    }

    public SteamSearchHitDto(long appId, String name, String tinyImage) {
        this.appId = appId;
        this.name = name;
        this.tinyImage = tinyImage;
    }

    public long getAppId() {
        return appId;
    }

    public void setAppId(long appId) {
        this.appId = appId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTinyImage() {
        return tinyImage;
    }

    public void setTinyImage(String tinyImage) {
        this.tinyImage = tinyImage;
    }
}

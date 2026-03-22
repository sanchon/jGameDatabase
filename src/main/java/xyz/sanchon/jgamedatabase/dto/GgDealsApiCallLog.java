package xyz.sanchon.jgamedatabase.dto;

/**
 * Registro de una llamada a la API de GG.deals para depuración en pantalla.
 */
public class GgDealsApiCallLog {

    private final String method;
    /** URL completa con la clave sustituida por &lt;oculto&gt; */
    private final String requestUrlMasked;
    private final Integer httpStatus;
    private final String rawResponseBody;
    private final String errorMessage;

    public GgDealsApiCallLog(String method, String requestUrlMasked, Integer httpStatus,
                            String rawResponseBody, String errorMessage) {
        this.method = method;
        this.requestUrlMasked = requestUrlMasked;
        this.httpStatus = httpStatus;
        this.rawResponseBody = rawResponseBody;
        this.errorMessage = errorMessage;
    }

    public String getMethod() {
        return method;
    }

    public String getRequestUrlMasked() {
        return requestUrlMasked;
    }

    public Integer getHttpStatus() {
        return httpStatus;
    }

    public String getRawResponseBody() {
        return rawResponseBody;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}

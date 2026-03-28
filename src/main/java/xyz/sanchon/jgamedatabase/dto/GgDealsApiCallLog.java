package xyz.sanchon.jgamedatabase.dto;

/**
 * Record of a GG.deals API call for on-screen debugging.
 */
public class GgDealsApiCallLog {

    private final String method;
    /** Full URL with the API key replaced by &lt;hidden&gt; */
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

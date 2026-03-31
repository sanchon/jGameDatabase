package xyz.sanchon.jgamedatabase.dto;

import java.io.Serializable;

/** One row from a batch-import CSV, held in the HTTP session queue. */
public class BatchGameEntry implements Serializable {

    private String name;
    private String platformHint;
    private String storeHint;

    public BatchGameEntry(String name, String platformHint, String storeHint) {
        this.name = name;
        this.platformHint = platformHint;
        this.storeHint = storeHint;
    }

    public String getName() {
        return name;
    }

    public String getPlatformHint() {
        return platformHint;
    }

    public String getStoreHint() {
        return storeHint;
    }
}

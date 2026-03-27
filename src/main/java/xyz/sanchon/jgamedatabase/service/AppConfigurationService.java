package xyz.sanchon.jgamedatabase.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import xyz.sanchon.jgamedatabase.model.AppConfiguration;
import xyz.sanchon.jgamedatabase.repository.AppConfigurationRepository;

@Service
public class AppConfigurationService {

    public static final String IGDB_CLIENT_ID = "igdb.client-id";
    public static final String IGDB_CLIENT_SECRET = "igdb.client-secret";
    public static final String GGDEALS_API_KEY = "ggdeals.api-key";
    public static final String GGDEALS_REGION = "ggdeals.region";
    public static final String H2_CONSOLE_ENABLED = "h2.console.enabled";

    @Value("${igdb.client-id:#{null}}")
    private String igdbClientIdProp;

    @Value("${igdb.client-secret:#{null}}")
    private String igdbClientSecretProp;

    @Value("${ggdeals.api-key:#{null}}")
    private String ggDealsApiKeyProp;

    @Value("${ggdeals.region:#{null}}")
    private String ggDealsRegionProp;

    private final AppConfigurationRepository repository;

    public AppConfigurationService(AppConfigurationRepository repository) {
        this.repository = repository;
    }

    @PostConstruct
    public void init() {
        saveIfPresent(IGDB_CLIENT_ID, igdbClientIdProp);
        saveIfPresent(IGDB_CLIENT_SECRET, igdbClientSecretProp);
        saveIfPresent(GGDEALS_API_KEY, ggDealsApiKeyProp);
        saveIfPresent(GGDEALS_REGION, ggDealsRegionProp);
    }

    private void saveIfPresent(String key, String value) {
        if (value != null && !value.isEmpty()) {
            if (repository.findByKey(key).isEmpty()) {
                repository.save(new AppConfiguration(key, value));
            }
        }
    }

    public String getIgdbClientId() {
        return getValue(IGDB_CLIENT_ID);
    }

    public String getIgdbClientSecret() {
        return getValue(IGDB_CLIENT_SECRET);
    }

    public String getGgDealsApiKey() {
        return getValue(GGDEALS_API_KEY);
    }
    
    public String getGgDealsRegion() {
        return getValue(GGDEALS_REGION);
    }
    
    public void setIgdbClientId(String value) {
        setValue(IGDB_CLIENT_ID, value);
    }

    public void setIgdbClientSecret(String value) {
        setValue(IGDB_CLIENT_SECRET, value);
    }

    public void setGgDealsApiKey(String value) {
        setValue(GGDEALS_API_KEY, value);
    }
    
    public void setGgDealsRegion(String value) {
        setValue(GGDEALS_REGION, value);
    }

    public boolean isH2ConsoleEnabled() {
        return "true".equals(getValue(H2_CONSOLE_ENABLED));
    }

    public void setH2ConsoleEnabled(boolean enabled) {
        setValue(H2_CONSOLE_ENABLED, String.valueOf(enabled));
    }

    private String getValue(String key) {
        return repository.findByKey(key).map(AppConfiguration::getValue).orElse(null);
    }

    private void setValue(String key, String value) {
        AppConfiguration config = repository.findByKey(key).orElse(new AppConfiguration(key, value));
        config.setValue(value);
        repository.save(config);
    }
}

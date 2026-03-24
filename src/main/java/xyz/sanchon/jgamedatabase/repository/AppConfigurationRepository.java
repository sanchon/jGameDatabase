package xyz.sanchon.jgamedatabase.repository;

import org.springframework.data.repository.CrudRepository;
import xyz.sanchon.jgamedatabase.model.AppConfiguration;

import java.util.Optional;

public interface AppConfigurationRepository extends CrudRepository<AppConfiguration, String> {
    Optional<AppConfiguration> findByKey(String key);
}

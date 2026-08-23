package com.dmart.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Automatically loads .env file variables into Spring Environment
 * for both application startup and test context executions.
 */
public class DotenvEnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String[] possiblePaths = {
            ".env",
            "backend/.env",
            "../.env",
            System.getProperty("user.dir") + File.separator + ".env",
            System.getProperty("user.dir") + File.separator + "backend" + File.separator + ".env",
            new File(".").getAbsolutePath() + File.separator + ".env",
            new File(".").getAbsolutePath() + File.separator + "backend" + File.separator + ".env"
        };

        for (String pathStr : possiblePaths) {
            File envFile = new File(pathStr);
            if (envFile.exists() && envFile.isFile()) {
                try {
                    Map<String, Object> props = new HashMap<>();
                    List<String> lines = Files.readAllLines(Paths.get(envFile.getAbsolutePath()));
                    for (String line : lines) {
                        line = line.trim();
                        if (line.isEmpty() || line.startsWith("#")) {
                            continue;
                        }
                        int eqIdx = line.indexOf('=');
                        if (eqIdx > 0) {
                            String key = line.substring(0, eqIdx).trim();
                            String value = line.substring(eqIdx + 1).trim();
                            props.put(key, value);
                            if ("DB_PASSWORD".equals(key)) {
                                props.put("spring.datasource.password", value);
                            } else if ("DB_URL".equals(key)) {
                                props.put("spring.datasource.url", value);
                            } else if ("DB_USERNAME".equals(key)) {
                                props.put("spring.datasource.username", value);
                            } else if ("JWT_SECRET".equals(key)) {
                                props.put("jwt.secret", value);
                            } else if ("JWT_EXPIRATION".equals(key)) {
                                props.put("jwt.expiration", value);
                            }
                            System.setProperty(key, value);
                        }
                    }
                    if (!props.isEmpty()) {
                        environment.getPropertySources().addFirst(new MapPropertySource("dotenvProperties", props));
                    }
                    break;
                } catch (IOException ignored) {
                }
            }
        }
    }
}
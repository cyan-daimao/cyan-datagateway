package com.cyan.datagateway.infra.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "starrocks")
public class StarRocksProperties {

    private String jdbcUrl;

    private String username;

    private String password;

    private Integer maxPoolSize = 10;

    private Integer minIdle = 2;

    private Long connectionTimeout = 30000L;

    private Long idleTimeout = 600000L;

    private Long maxLifetime = 1800000L;
}

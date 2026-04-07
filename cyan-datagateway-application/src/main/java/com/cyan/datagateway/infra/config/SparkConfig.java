package com.cyan.datagateway.infra.config;

import org.apache.spark.sql.SparkSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * @author cy.Y
 */
@Component
public class SparkConfig {
    @Value("${spring.application.name}")
    private String appName;

    @Value("${spark.spark-master}")
    private String sparkMaster;

    @Value("${iceberg.uri}")
    private String icebergRestUri;

    @Value("${rustfs.endpoint}")
    private String rustfsEndpoint;

    @Value("${rustfs.accessKey}")
    private String rustfsAccessKey;

    @Value("${rustfs.secretKey}")
    private String rustfsSecretKey;

    @Bean
    public SparkSession getRemoteSparkSession() {
        return SparkSession.builder()
                .appName(appName)
                .master(sparkMaster)
                // Disable Spark UI and metrics to avoid jakarta.servlet.SingleThreadModel conflict with Spring Boot 3.x
                .config("spark.ui.enabled", "false")
                .config("spark.metrics.enabled", "false")
                .config("spark.executor.cores", "1")      // 每个Executor 1核
                .config("spark.cores.max", "2")
                .config("spark.executor.memory", "2g")
                .config("spark.driver.memory", "1g")

                // ===================== Iceberg 核心配置 =====================
                .config("spark.sql.extensions", "org.apache.iceberg.spark.extensions.IcebergSparkSessionExtensions")

                // ===================== 创建 iceberg catalog =====================
                .config("spark.sql.catalog.iceberg", "org.apache.iceberg.spark.SparkCatalog")
                .config("spark.sql.catalog.iceberg.catalog-impl", "org.apache.iceberg.rest.RESTCatalog")
                .config("spark.sql.catalog.iceberg.uri", icebergRestUri)
                .config("spark.sql.catalog.iceberg.io-impl", "org.apache.iceberg.aws.s3.S3FileIO")
                .config("spark.sql.catalog.iceberg.s3.endpoint", rustfsEndpoint)
                .config("spark.sql.catalog.iceberg.s3.path-style-access", "true")
                // AWS SDK v2 正确的凭证配置键名
                .config("spark.sql.catalog.iceberg.s3.access-key-id", rustfsAccessKey)
                .config("spark.sql.catalog.iceberg.s3.secret-access-key", rustfsSecretKey)
                .config("spark.sql.defaultCatalog", "iceberg")

                .getOrCreate();
    }
}

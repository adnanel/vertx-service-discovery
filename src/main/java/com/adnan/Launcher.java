package com.adnan;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.hazelcast.config.Config;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Scanner;

public class Launcher  extends io.vertx.core.Launcher {
    private static final Logger logger = LogManager.getLogger(io.vertx.core.Launcher.class.getCanonicalName());

    public Launcher(String[] args) {
        System.setProperty("io.vertx.ext.web.TemplateEngine.disableCache", "true");
        System.setProperty("fileResolverCachingEnabled", "false");
    }

    public static void main(String[] args) {
        new Launcher(args).dispatch(args);
    }

    @Override
    public void beforeStartingVertx(VertxOptions options) {
        var ebOptions = options.getEventBusOptions();
        ebOptions.setClustered(true);
        var hazelConfig = new Config();
        var port = System.getenv("PORT");
        port = port == null ? "9000" : port;
        hazelConfig.getNetworkConfig().setPort(Integer.parseInt(port));

        ClusterManager mgr = new HazelcastClusterManager(hazelConfig);
        options.setClusterManager(mgr);
    }

    @Override
    public void afterStartingVertx(Vertx vertx) {
        super.afterStartingVertx(vertx);
    }

    @Override
    public void beforeDeployingVerticle(DeploymentOptions deploymentOptions) {
        super.beforeDeployingVerticle(deploymentOptions);

        if (deploymentOptions.getConfig() == null) {
            deploymentOptions.setConfig(new JsonObject());
        }

        deploymentOptions.getConfig().mergeIn(loadConfig());
    }

    protected JsonObject loadConfig() {
        return new JsonObject(loadConfigurationForFile("config.json"));
    }

    protected static String configFileContent(String filePath) {
        File config = new File(filePath);
        if (config.isFile()) {
            try (Scanner scanner = new Scanner(config).useDelimiter("\\A")) {
                return scanner.next();
            } catch (FileNotFoundException e) {
                throw new IllegalStateException(String.format("Config %s loading failed", config.getAbsolutePath()));
            }
        } else {
            throw new IllegalStateException(String.format("Config %s is not file", config.getAbsolutePath()));
        }
    }

    protected static String loadConfigurationForFile(String fileName) {
        return configFileContent(configurationPath(fileName));
    }

    protected static String configurationPath(String fileName) {
        return "conf/" + fileName;
    }
}

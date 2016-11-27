package org.gpc4j.ncaaf;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import org.apache.log4j.Logger;
import org.hibernate.validator.constraints.NotEmpty;


/**
 *
 * @author Lyle T Harris
 */
public class FootballConfiguration extends Configuration {

    @JsonProperty("swagger")
    public SwaggerBundleConfiguration swaggerBundleConfiguration;

    @NotEmpty
    private String version;

    @NotEmpty
    private String redisHost;

    @Min(1024)
    @Max(65535)
    private int redisPort;

    private String redisPass;

    private String basePath;


    public String getVersion() {
        return version;
    }


    public void setVersion(String version) {
        System.err.println("Version: " + version);
        Logger.getRootLogger().info(version);
        this.version = version;
    }


    public String getRedisHost() {
        return redisHost;
    }


    public int getRedisPort() {
        return redisPort;
    }


    public void setRedisPort(int redisPort) {
        this.redisPort = redisPort;
    }


    public void setRedisHost(String redisHost) {
        this.redisHost = redisHost;
    }


    public String getRedisPass() {
        return redisPass;
    }


    public void setRedisPass(String redisPass) {
        this.redisPass = redisPass;
    }


    public String getBasePath() {
        return basePath;
    }


    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }


}

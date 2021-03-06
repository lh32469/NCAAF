package org.gpc4j.ncaaf;

import com.fasterxml.jackson.databind.SerializationFeature;
import io.dropwizard.Application;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.gpc4j.ncaaf.redis.RedisGamesProvider;
import org.gpc4j.ncaaf.resources.AP;
import org.zapodot.hystrix.bundle.HystrixBundle;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.AsyncAppender;
import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.dropwizard.assets.AssetsBundle;
import io.federecio.dropwizard.swagger.SwaggerBundle;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;
import io.swagger.jaxrs.config.BeanConfig;
import io.swagger.jaxrs.listing.ApiListingResource;
import java.util.HashMap;
import java.util.Map;
import org.glassfish.jersey.message.MessageProperties;
import org.glassfish.jersey.server.ServerProperties;
import org.gpc4j.ncaaf.resources.GamesResource;
import org.gpc4j.ncaaf.resources.UpdateSchedule;
import org.gpc4j.ncaaf.writers.GameWriter;
import org.gpc4j.ncaaf.writers.GamesWriter;


/**
 *
 * @author ltharris
 */
public class FootballApplication extends Application<FootballConfiguration> {

    public static void main(String[] args) throws Exception {
        new FootballApplication().run(args);
    }


    @Override
    public void initialize(Bootstrap<FootballConfiguration> bootstrap) {
        bootstrap.getObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        bootstrap.addBundle(HystrixBundle.withDefaultSettings());
        bootstrap.addBundle(new ViewBundle());
        // bootstrap.addBundle(new AssetsBundle("/swagger", "/swagger",
        //          "index.html", "swagger"));
        bootstrap.addBundle(new AssetsBundle("/images", "/images",
                "/", "images"));

        // Swagger
        bootstrap.addBundle(new SwaggerBundle<FootballConfiguration>() {
            @Override
            protected SwaggerBundleConfiguration
                    getSwaggerBundleConfiguration(FootballConfiguration cfg) {
                return cfg.swaggerBundleConfiguration;
            }


        });
    }


    @Override
    public String getName() {
        return "NCAAF Weekly Graphic";
    }


    @Override
    public void run(FootballConfiguration cfg, Environment env)
            throws Exception {

        Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        AsyncAppender appender
                = (AsyncAppender) root.getAppender("async-console-appender");
        appender.setIncludeCallerData(true);

        JerseyEnvironment jersey = env.jersey();
        jersey.register(AP.class);
        jersey.register(UpdateSchedule.class);
        jersey.register(GamesResource.class);
        jersey.register(GamesWriter.class);
        jersey.register(GameWriter.class);
        jersey.property(MessageProperties.XML_FORMAT_OUTPUT, true);

        // <editor-fold defaultstate="collapsed" desc="Swagger Config">
        jersey.register(ApiListingResource.class);
        env.getObjectMapper()
                .setSerializationInclusion(JsonInclude.Include.NON_NULL);
        BeanConfig config = new BeanConfig();
        config.setTitle("NCAAF Weekly Graphic REST Services");
        config.setVersion("1.0.0");
        config.setResourcePackage("org.gpc4j.ncaaf.resources");
        config.setBasePath(cfg.getBasePath());
        config.setScan(true);
        // </editor-fold>

        Map<String, Object> properties = new HashMap<>();
        properties.put(ServerProperties.WADL_FEATURE_DISABLE, false);
        jersey.getResourceConfig().addProperties(properties);

        Binder binder = new Binder(cfg);
        jersey.register(binder);
    }


    class Binder extends AbstractBinder {

        private final FootballConfiguration config;


        public Binder(FootballConfiguration config) {
            this.config = config;
        }


        @Override
        protected void configure() {

            JedisPoolConfig cfg = new JedisPoolConfig();
            cfg.setMaxTotal(25);
            cfg.setMinIdle(10);
            cfg.setTestWhileIdle(true);
            JedisPool pool = new JedisPool(cfg, config.getRedisHost(),
                    config.getRedisPort(), 0, config.getRedisPass(), 10);
//            JedisPool pool = new JedisPool(cfg, config.getRedisHost(),
//                    config.getRedisPort());

            bind(pool);

            bind(new RedisGamesProvider(pool)).to(GamesProvider.class);
            bind(new TeamProvider(pool));
        }


    }

}

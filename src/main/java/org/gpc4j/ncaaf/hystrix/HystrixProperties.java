package org.gpc4j.ncaaf.hystrix;

import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixThreadPoolProperties;


public interface HystrixProperties {

    HystrixCommandGroupKey REDIS_GROUP_KEY
            = HystrixCommandGroupKey.Factory.asKey("Redis");

    HystrixCommandProperties.Setter REDIS_COMMAND_PROPS
            = HystrixCommandProperties.Setter()
            .withExecutionTimeoutInMilliseconds(300000);

    HystrixThreadPoolProperties.Setter REDIS_THREAD_PROPERTIES
            = HystrixThreadPoolProperties.Setter()
            .withQueueSizeRejectionThreshold(10000)
            .withMaxQueueSize(1000);

}

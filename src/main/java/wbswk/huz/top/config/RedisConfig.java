package wbswk.huz.top.config;

import com.alibaba.fastjson.support.spring.FastJsonRedisSerializer;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * @author wbswk
 * @description TODO
 **/
@Configuration
public class RedisConfig {

    // Redis服务器地址
    @Value("${spring.redis.host}")
    private String host;

    // Redis服务器连接端口
    @Value("${spring.redis.port}")
    private Integer port;

    // Redis数据库索引（默认为0）
    @Value("${spring.redis.database}")
    private Integer database;

    // 连接超时时间（毫秒）
    @Value("${spring.redis.timeout}")
    private Integer timeout;

    // 连接池最大连接数（使用负值表示没有限制）
    @Value("${spring.redis.lettuce.pool.max-active}")
    private Integer maxActive;

    // 连接池最大阻塞等待时间（使用负值表示没有限制）
    @Value("${spring.redis.lettuce.pool.max-wait}")
    private Integer maxWait;

    // 连接池中的最大空闲连接
    @Value("${spring.redis.lettuce.pool.max-idle}")
    private Integer maxIdle;

    // 连接池中的最小空闲连接
    @Value("${spring.redis.lettuce.pool.min-idle}")
    private Integer minIdle;

    // 关闭超时时间
    @Value("${spring.redis.lettuce.shutdown-timeout}")
    private Integer shutdown;



   /**
    * @Author wbswk
    * @Description // 配置连接池
    * @Param []
    * @return org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration
   **/
    @Bean
    public LettucePoolingClientConfiguration getPoolConfig(){
        GenericObjectPoolConfig config = new GenericObjectPoolConfig();
        // 配置最大的空闲连接数
        config.setMaxIdle(maxIdle);
        // 配置最小的空闲连接数
        config.setMinIdle(minIdle);
        // 配置最大连接池最大连接数
        config.setMaxTotal(maxActive);
        // 配置最大的阻塞时间
        config.setMaxWaitMillis(maxWait);
        return LettucePoolingClientConfiguration.builder().poolConfig(config)
                .commandTimeout(Duration.ofMillis(timeout))
                .shutdownTimeout(Duration.ofMillis(shutdown))
                .build();
    }

    /**
     * @Author wbswk
     * @Description // 配置缓存连接
     * @Param []
     * @return org.springframework.data.redis.connection.RedisConnectionFactory
    **/
    @Bean
     public RedisConnectionFactory getConnectionFactory(){
        // 单机模式
        RedisStandaloneConfiguration standaloneConfiguration = new RedisStandaloneConfiguration();
        standaloneConfiguration.setHostName(host);
        standaloneConfiguration.setPort(port);
        standaloneConfiguration.setDatabase(database);

        // 哨兵模式
//        RedisSentinelConfiguration sentinelConfiguration = new RedisSentinelConfiguration();
        // 分片集群模式
//        RedisClusterConfiguration redisClusterConfiguration = new RedisClusterConfiguration();
        LettuceConnectionFactory factory = new LettuceConnectionFactory(standaloneConfiguration , getPoolConfig());
//       factory.setShareNativeConnection(true);  // 是否允许多个线程操作共用同一个缓存连接，默认true，false时每个操作都将开辟新的连接
        return factory;
     }

     /**
      * @Author wbswk
      * @Description // 替换默认的RedisTemplate(JDK的序列化)
      * @Param []
      * @return org.springframework.data.redis.core.RedisTemplate<java.lang.String,java.lang.Object>
     **/
     @Bean
     public RedisTemplate<String , Object> redisTemplate(){
        RedisTemplate<String,Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(getConnectionFactory());
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
         //RedisTemplate对象需要指明Key序列化方式，如果声明StringRedisTemplate对象则不需要
         redisTemplate.setKeySerializer(stringRedisSerializer);
         // hash的key也采用String的序列化方式
         redisTemplate.setHashKeySerializer(stringRedisSerializer);

         //value采用fastjson的序列化
         FastJsonRedisSerializer<Object> fastJsonRedisSerializer = new FastJsonRedisSerializer<Object>(Object.class);
         redisTemplate.setValueSerializer(fastJsonRedisSerializer);
         redisTemplate.setHashValueSerializer(fastJsonRedisSerializer);

         redisTemplate.afterPropertiesSet();
         return redisTemplate;
     }


















}

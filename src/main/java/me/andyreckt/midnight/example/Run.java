package me.andyreckt.midnight.example;

import me.andyreckt.midnight.Midnight;
import me.andyreckt.midnight.RedisCredentials;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class Run {

    public static void main(final String[] args) {
        RedisCredentials credentials = new RedisCredentials("localhost", 6379);

        JedisPool pool = new JedisPool(credentials.getHostname(), credentials.getPort());

//        JedisPool pool = new JedisPool(
//                new JedisPoolConfig(),
//                        credentials.getHostname(),
//                        credentials.getPort(),
//                        2000,
//                        credentials.getPassword()
//        );

        Midnight midnight = new Midnight(pool);

        midnight.registerClass(ExampleObject.class);
        midnight.registerClass(ExampleSubscriber.class);

        midnight.sendObject(new ExampleObject("salutations", 36));
    }
}

package me.andyreckt.midnight.example;

import me.andyreckt.midnight.Midnight;
import me.andyreckt.midnight.RedisCredentials;
import redis.clients.jedis.JedisPool;

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

        midnight.registerObject(ExampleObject.class);
        midnight.registerListener(new ExampleSubscriber());

        midnight.sendObject(new ExampleObject("hello", 36));
    }
}

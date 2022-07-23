package me.andyreckt.midnight.example;

import me.andyreckt.midnight.Midnight;
import me.andyreckt.midnight.RedisCredentials;
import redis.clients.jedis.JedisPool;

public class Run {

    public static void main(final String[] args) {
        RedisCredentials credentials = new RedisCredentials("localhost", 6379);

//        JedisPool pool = new JedisPool(credentials.getHostname(), credentials.getPort());

        JedisPool pool = new JedisPool(
                new redis.clients.jedis.JedisPoolConfig(),
                        credentials.getHostname(),
                        credentials.getPort(),
                        2000,
                        credentials.getPassword()
        );

        Midnight midnight = new Midnight(pool);

        midnight.registerObject(ExampleObject.class);
        midnight.registerListener(new ExampleSubscriber());

        // Examples
        midnight.sendObject(new ExampleObject("hello", 36));

        midnight.cache("data", "hello");
        midnight.cache("data", "example", "world");

        String data = (String) midnight.get("data", String.class);
        String data2 = (String) midnight.get("data", "example", String.class);

        midnight.log(data + " " + data2);

    }
}

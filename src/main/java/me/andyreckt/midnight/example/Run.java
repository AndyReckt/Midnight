package me.andyreckt.midnight.example;

import com.google.gson.GsonBuilder;
import me.andyreckt.midnight.Midnight;
import me.andyreckt.midnight.RedisCredentials;
import redis.clients.jedis.JedisPool;

import java.util.HashMap;
import java.util.Map;

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
        midnight.cache("data", "example2", "test");

        String data = (String) midnight.getAsync("data", String.class);
        String data2 = (String) midnight.get("data", "example", String.class);

        Map<String, Object> data3 = midnight.getAll("data", String.class);
        Map<String, String> data4 = new HashMap<>();
        data3.forEach((key, value) -> data4.put(key, (String) value));

        midnight.log(data + " " + data2);
        midnight.log(new GsonBuilder().serializeNulls().setPrettyPrinting().create().toJson(data4));

    }
}

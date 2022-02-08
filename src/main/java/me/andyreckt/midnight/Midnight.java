package me.andyreckt.midnight;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.SneakyThrows;
import me.andyreckt.midnight.annotations.RedisListener;
import me.andyreckt.midnight.annotations.RedisObject;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPubSub;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Midnight {

    private static final Gson GSON = new GsonBuilder().serializeNulls().create();
    private static final String channel = "AraJedisPool";
    private static final String splitRegex = "--;@;--";

    private final Map<Method, Class<?>> subscriberMap = new HashMap<>();
    private final Map<String, Class<?>> objectMap = new HashMap<>();

    private final Executor executor;
    private JedisPubSub pubSub;
    private final JedisPool pool;


    public Midnight(JedisPool pool) {
        System.out.println("[Midnight] >> Initializing");
        executor = Executors.newFixedThreadPool(2);
        this.pool = pool;
        setupPubSub(pool);
        System.out.println("[Midnight] >> Initialized");
    }

    @SneakyThrows
    private void setupPubSub(JedisPool pool) {
        if (this.pubSub != null) return;
        this.pubSub = new JedisPubSub() {
            @Override @SneakyThrows
            public void onMessage(String channel, String message) {
                if (!channel.equalsIgnoreCase(Midnight.channel)) return;

                String[] array = message.split(Midnight.splitRegex);
                String id = array[0];

                if (objectMap.get(id) == null) return;
                Object clazz = GSON.fromJson(array[1], objectMap.get(id));

                if (clazz == null) return;

                for (Map.Entry<Method, Class<?>> entry : subscriberMap.entrySet()) {
                    if (entry.getValue().getAnnotation(RedisObject.class) == null) continue;
                    if (entry.getValue().getAnnotation(RedisObject.class).id().equalsIgnoreCase(id)) {
                        entry.getKey().invoke(null, clazz);
                    }
                }
            }
        };

        executor.execute(() -> {
            final Jedis jedis = pool.getResource();
            jedis.subscribe(pubSub, channel);
        });
    }

    /**
     * Sends a class through redis
     *
     * @param object the instance of the class to send
     */

    @SneakyThrows
    public void sendObject(Object object) {
        executor.execute(() -> {
            if (object.getClass().getAnnotation(RedisObject.class) == null) return;
            Jedis jedis = this.pool.getResource();
            String toSend = GSON.toJson(object);
            jedis.publish(channel, object.getClass().getAnnotation(RedisObject.class).id() + splitRegex + toSend);
        });
    }


    /**
     * Scan a class and registers it as an RedisObject if possible,
     * if not then checks if any method in the class can be registered as a RedisListener
     *
     *
     * @param registeredClass The class to scan/register.
     */
    public void registerClass(Class<?> registeredClass) {
        if (registeredClass.getAnnotation(RedisObject.class) != null) {
            this.objectMap.put(registeredClass.getAnnotation(RedisObject.class).id(), registeredClass);
            System.out.println("[Midnight] >> Registered class " + registeredClass.getSimpleName() + " as an Object");
            return;
        }

        for (Method method : registeredClass.getMethods()) {
            if (method.getAnnotation(RedisListener.class) != null) {
                registerMethod(method);
            }
        }
    }

    /**
     * Registers a method as a RedisListener.
     *
     * @param method The method to register
     */
    @SneakyThrows
    private void registerMethod(Method method) {
        if (method.getParameterTypes().length != 1) throw new Exception("The amount of parameters a RedisListener method should have is one and only one.");

        Class<?> clazz = method.getParameterTypes()[0];

        this.subscriberMap.put(method, clazz);

        System.out.println("[Midnight] >> Registered method" + method.getName() + " in class " + method.getDeclaringClass().getSimpleName() + " as a Listener");
    }

}

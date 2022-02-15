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
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Midnight {

    private static final Gson GSON = new GsonBuilder().create();
    private static final String channel = "AraJedisPool";
    private static final String splitRegex = "--;@;--";

    private final List<LData> dataList = new ArrayList<>();
    private final Map<String, Class<?>> objectMap = new HashMap<>();

    private final Executor executor;
    private final JedisPool pool;
    private JedisPubSub pubSub;


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
            @Override
            @SneakyThrows
            public void onMessage(String channel, String message) {
                if (!channel.equalsIgnoreCase(Midnight.channel)) return;

                String[] array = message.split(Midnight.splitRegex);
                String id = array[0];

                if (objectMap.get(id) == null) return;
                Object clazz = GSON.fromJson(array[1], objectMap.get(id));

                if (clazz == null) return;

                for (LData data : dataList) {
                    if (data.getClazz().getAnnotation(RedisObject.class) == null) continue;
                    if (data.getClazz().getAnnotation(RedisObject.class).id().equalsIgnoreCase(id)) {
                        data.getMethod().invoke(data.getObject(), clazz);
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
     * @param clazz The class to scan/register.
     */
    @Deprecated
    public void registerClass(Class<?> clazz) {
        System.out.println("[Midnight] >> Using Midnight#registerClass() is deprecated, please refrain to use it.");

        if (clazz.getAnnotation(RedisObject.class) != null) {
            this.objectMap.put(clazz.getAnnotation(RedisObject.class).id(), clazz);
            System.out.println("[Midnight] >> Registered class " + clazz.getSimpleName() + " as an Object");
            return;
        }

        for (Method method : clazz.getDeclaredMethods()) {
            if (method.getAnnotation(RedisListener.class) != null) {
                registerMethod(method);
            }
        }
    }

    /**
     * Scans a class and registers it as an RedisObject if possible
     *
     * @param clazz The class to register.
     */
    public void registerObject(Class<?> clazz) {
        if (clazz.getAnnotation(RedisObject.class) == null) return;
        this.objectMap.put(clazz.getAnnotation(RedisObject.class).id(), clazz);
        System.out.println("[Midnight] >> Registered class " + clazz.getSimpleName() + " as an Object");
    }


    /**
     * Scan a class and checks if any method in the class can be registered as a RedisListener
     *
     * @param clazz The class to scan.
     */
    public void registerListener(Object clazz) {
        for (Method method : clazz.getClass().getDeclaredMethods()) {
            if (method.getAnnotation(RedisListener.class) != null) {
                registerMethod(method, clazz);
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
        if (!Modifier.isStatic(method.getModifiers())) throw new Exception("In order to register a method as a listener without creating a new instance of its class, the method must be static.");
        Class<?> clazz = method.getParameterTypes()[0];

        this.dataList.add(new LData(null, method, clazz));

        System.out.println("[Midnight] >> Registered method " + method.getName() + " in class " + method.getDeclaringClass().getSimpleName() + " as a Listener");
    }

    /**
     * Registers a method as a RedisListener.
     *
     * @param method The method to register
     * @param instance The instance of the class
     */
    @SneakyThrows
    private void registerMethod(Method method, Object instance) {
        if (method.getParameterTypes().length != 1) throw new Exception("The amount of parameters a RedisListener method should have is one and only one.");
        Class<?> clazz = method.getParameterTypes()[0];

        this.dataList.add(new LData(instance, method, clazz));

        System.out.println("[Midnight] >> Registered method " + method.getName() + " in class " + method.getDeclaringClass().getSimpleName() + " as a Listener");
    }

}

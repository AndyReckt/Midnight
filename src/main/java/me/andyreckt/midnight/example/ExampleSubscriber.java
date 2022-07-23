package me.andyreckt.midnight.example;

import me.andyreckt.midnight.annotations.RedisListener;

public class ExampleSubscriber {

    @RedisListener // A Listener must be static since we are not using an instance of a class to invoke the method
    public void sub(ExampleObject test) {
        System.out.println(test.getString());
        System.out.println(test.getInteger());
    }


}

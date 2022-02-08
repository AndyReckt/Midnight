package me.andyreckt.midnight.example;


import lombok.AllArgsConstructor;
import lombok.Getter;
import me.andyreckt.midnight.annotations.RedisObject;


@AllArgsConstructor
@Getter
@RedisObject(id = "test")
public class ExampleObject {

    private final String string;
    private final int integer;

}

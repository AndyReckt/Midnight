package me.andyreckt.midnight;

import lombok.Data;
import java.lang.reflect.Method;

@Data
public class LData {

    private final Object object;
    private final Method method;
    private final Class<?> clazz;

}

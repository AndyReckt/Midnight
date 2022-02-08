package me.andyreckt.midnight;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Grape (grape#9876)
 * 10/27/2021 / 9:46 PM
 * Aether / me.grape.aether.shared.credentials
 */
@AllArgsConstructor
@Getter
public class RedisCredentials {

    private final String hostname;
    private final int port;

    private final boolean auth;
    private final String password;


    public RedisCredentials(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
        this.auth = false;
        this.password = "foobar";
    }

}
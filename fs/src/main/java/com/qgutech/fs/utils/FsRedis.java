package com.qgutech.fs.utils;

import redis.clients.jedis.JedisCommands;

public class FsRedis {
    private static JedisCommands commonJedis;

    public static JedisCommands getCommonJedis() {
        return commonJedis;
    }

    public void setCommonJedis(JedisCommands commonJedis) {
        FsRedis.commonJedis = commonJedis;
    }
}

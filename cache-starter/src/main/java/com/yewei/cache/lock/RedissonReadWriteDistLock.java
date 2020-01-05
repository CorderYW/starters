package com.yewei.cache.lock;

import org.redisson.api.RReadWriteLock;

/**
 * Created by robin.wu on 2018/9/26.
 */
public class RedissonReadWriteDistLock implements IReadWriteDistLock {

    private RReadWriteLock readWriteLock;

    public RedissonReadWriteDistLock(RReadWriteLock readWriteLock) {
        this.readWriteLock = readWriteLock;
    }

    @Override
    public IDistLock readLock() {
        return new RedissonDistLock(readWriteLock.readLock());
    }

    @Override
    public IDistLock writeLock() {
        return new RedissonDistLock(readWriteLock.writeLock());
    }
}

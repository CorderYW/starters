package com.yewei.cache.lock;

/**
 * Created by robin.wu on 2018/9/25.
 */
public interface IReadWriteDistLock {
    IDistLock readLock();

    IDistLock writeLock();
}

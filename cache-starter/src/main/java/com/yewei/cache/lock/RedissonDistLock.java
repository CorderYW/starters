package com.yewei.cache.lock;

import org.redisson.RedissonMultiLock;
import org.redisson.api.RLock;

import java.util.concurrent.TimeUnit;

/**
 * Created by robin.wu on 2018/9/20.
 * redisson的分布式锁的包装类
 */
public class RedissonDistLock implements IDistLock{
    private boolean isMultiLock = false;

    private RLock rLock;

    private RedissonMultiLock multiLock;

    public RedissonDistLock(RLock rLock){
        this.rLock = rLock;
    }

    public RedissonDistLock(RedissonMultiLock multiLock) {
        this.multiLock = multiLock;
        this.isMultiLock = true;
    }

    @Override
    public boolean lock() {
        if(isMultiLock && multiLock != null) {
            this.multiLock.lock();
            return false;
        }

        if(rLock != null) {
            this.rLock.lock();
            return false;
        }

        return true;
    }

    @Override
    public boolean lock(long leaseTime, TimeUnit unit) {
        if(isMultiLock && multiLock != null) {
            this.multiLock.lock(leaseTime,unit);
            return false;
        }

        if(rLock != null) {
            this.rLock.lock(leaseTime,unit);
            return false;
        }

        return true;
    }

    @Override
    public boolean tryLock() {
        if(isMultiLock && multiLock != null) {
            return this.multiLock.tryLock();
        }

        if(rLock != null) {
            return this.rLock.tryLock();
        }

        return false;
    }

    @Override
    public boolean tryLock(long waitTime, long leaseTime, TimeUnit unit) throws InterruptedException {
        if(isMultiLock && multiLock != null) {
            return this.multiLock.tryLock(waitTime,leaseTime,unit);
        }

        if(rLock != null) {
            return this.rLock.tryLock(waitTime,leaseTime,unit);
        }

        return false;
    }

    @Override
    public boolean unlock() {
        try {
            if(isMultiLock && multiLock != null) {
                this.multiLock.unlock();
            }

            if(rLock != null) {
                this.rLock.unlock();
            }
            return true;
        }catch (Exception e){
            throw e;
        }
    }
}

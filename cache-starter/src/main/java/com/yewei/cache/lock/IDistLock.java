package com.yewei.cache.lock;

import java.util.concurrent.TimeUnit;

/**
 * Created by robin.wu on 2018/9/20.
 */
public interface IDistLock {
    boolean lock();

    boolean lock(long leaseTime, TimeUnit unit);

    boolean tryLock();

    /**
     *  根据任务执行时长来评估waitTime和leaseTime
     * @param waitTime  尝试加锁时客户端等待时间(集群模式下，等待时间实际上等于 waitTime/lock个数，leaseTime:加锁成功后 的锁时间
     * @param leaseTime 一般不太好指定， 可以不填
     * @param unit
     * @return
     * @throws InterruptedException
     */
    boolean tryLock(long waitTime, long leaseTime, TimeUnit unit) throws InterruptedException;

    boolean unlock();
}

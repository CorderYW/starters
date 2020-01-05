package com.yewei.cache.lock;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import lombok.extern.log4j.Log4j2;
import org.redisson.Redisson;
import org.redisson.RedissonRedLock;
import org.redisson.api.RLock;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.redisson.client.RedisClient;
import org.redisson.client.RedisClientConfig;
import org.redisson.client.RedisConnection;
import org.redisson.client.protocol.RedisCommands;
import org.redisson.cluster.ClusterNodeInfo;
import org.redisson.connection.ConnectionManager;
import org.redisson.misc.RedisURI;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by robin.wu on 2018/9/20.
 * 分布式锁的管理入口（考虑到扩展性，可以提供不同实现的分布式锁,暂时只提供redisson）
 */
@Log4j2
public class DistLockManager {

    @Autowired
    private RedissonClient redisson;

    private static int nodeSize;

    private static Map<String, ClusterNodeInfo> nodeSlotsMap = new HashMap<>();
    private static ConcurrentHashMap<String,Boolean> cacheKeyMap = new ConcurrentHashMap();

    @PostConstruct
    public void init(){
        if(redisson != null) {
            nodeSize = ((Redisson) redisson).getConnectionManager().getEntrySet().size();

            RedisURI uri = ((Redisson) redisson).getConnectionManager().getLastClusterNode();
            if(uri == null) {
                return;
            }

            EventLoopGroup group = new NioEventLoopGroup();

            RedisClientConfig config = new RedisClientConfig();
            config.setAddress(uri.toString())
                    .setPassword(null)
                    .setDatabase(0)
                    .setClientName("myClient")
                    .setGroup(group);

            RedisClient client = RedisClient.create(config);
            RedisConnection conn = client.connect();
            List<ClusterNodeInfo> nodes = conn.sync(RedisCommands.CLUSTER_NODES);

            for(ClusterNodeInfo clusterNodeInfo : nodes) {
                if (clusterNodeInfo.containsFlag(ClusterNodeInfo.Flag.NOADDR) || clusterNodeInfo.containsFlag(ClusterNodeInfo.Flag.HANDSHAKE)) {
                    // skip it
                    continue;
                }
                if(clusterNodeInfo.containsFlag(ClusterNodeInfo.Flag.MASTER)) {
                    nodeSlotsMap.put(clusterNodeInfo.getNodeId(),clusterNodeInfo);
                    log.debug("DistLockManager->init->clusterNodeInfo:{}",clusterNodeInfo);
                }
            }
        }
    }

    /**
     * single lock. not safety
     * @param key
     * @return
     */
    public IDistLock buildDistLock(String key){
        return buildDistLock(key,false);
    }

    /**
     * single lock.
     * @param key
     * @param isFair
     * @return
     */
    public IDistLock buildDistLock(String key,boolean isFair) {
        if(!isFair) {
            return new RedissonDistLock(redisson.getLock(key));
        }else {
            return new RedissonDistLock(redisson.getFairLock(key));
        }
    }

    /**
     * redlock  (分别从不同的实例上获取lock,)
     * @param key  key值需要对应到不同的实例(CRC16(key) mod 16834)
     *              如对应两个实例： lockKeyPrefix_000001, lockKeyPrefix_000002
     * @return
     */
    public IDistLock buildRedDistLock(String key){
        if (key == null || key.length() == 0) {
            throw new IllegalArgumentException("keys are not defined");
        }

        RLock[] rLocks = new RLock[nodeSize];
        for (int i=0; i<nodeSize; i++) {
            rLocks[i] = redisson.getLock(key + i);
        }

        RedissonRedLock redLock = new RedissonRedLock(rLocks);
        return new RedissonDistLock(redLock);
    }


    /**
     * 读写锁
     * @param key
     * @return
     */
    public IReadWriteDistLock buildDistRWLock(String key) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * 判断分布式锁的key是否满足redlock的slot均匀分配策略（key + 序号）
     * @param key
     * @return
     */
    public boolean checkSlotRangeStrategy(String key){
        if(cacheKeyMap.containsKey(key)) {
            return cacheKeyMap.get(key);
        }

        int nodeSize = nodeSlotsMap.size();
        if(nodeSize <=0) {
            return false;
        }
        Set<String> nodeIdSet = new HashSet<>();
        ConnectionManager connectionManager = ((Redisson)redisson).getConnectionManager();
        for(int i=0;i<nodeSize;i++) {
            String tempKey = key + i;

            nodeSlotsMap.keySet().forEach(k->{
                ClusterNodeInfo clusterNodeInfo = nodeSlotsMap.get(k);
                int slot = connectionManager.calcSlot(tempKey);
                clusterNodeInfo.getSlotRanges().forEach(r->{
                    if(r.getStartSlot() <= slot && r.getEndSlot() >= slot) {
                        nodeIdSet.add(k);
                    }
                });
            });
        }

        boolean result = nodeIdSet.size() > nodeSize/2 ? true : false;
        cacheKeyMap.put(key,result);
        return result;
    }


    public String getLockKeyInfo(String key){
        StringBuilder sb = new StringBuilder();
        try {
            for(int i=0; i<nodeSize ; i++) {
                RMap<String,Long> map = redisson.getMap(key + i);
                for(String k: map.keySet()){
                    sb.append(String.format("node%s: {CurrThreadId: [%s], LockKey:[%s], LockName:[%s], LockThreadId:[%s]},",
                            i,Thread.currentThread().getId(),key+i,k,map.get(k)));
                }
            }
            sb.deleteCharAt(sb.length()-1);
        }catch (Exception e){
            log.error("GetLockKeyInfo error",e);
        }
        return sb.toString();
    }
}

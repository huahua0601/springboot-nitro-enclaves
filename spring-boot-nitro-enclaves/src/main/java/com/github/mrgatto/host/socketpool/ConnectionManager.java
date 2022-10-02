package com.github.mrgatto.host.socketpool;

import lombok.extern.slf4j.Slf4j;
import solutions.cloudarchitects.vsockj.VSock;
import solutions.cloudarchitects.vsockj.VSockAddress;

import java.io.IOException;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class ConnectionManager {

    private int cid = 5;
    private int port = 5000;

    private VSockAddress vSockAddress;

    /**
     * total create connection pool
     */
    private AtomicInteger totalCount = new AtomicInteger();

    /**
     * free connection count
     */
    private AtomicInteger freeCount = new AtomicInteger();

    /**
     * lock
     */
    private ReentrantLock lock = new ReentrantLock(true);

    private Condition condition = lock.newCondition();

    /**
     * free connections
     */
    private LinkedList<VSock> freeConnections = new LinkedList<VSock>();

    private ConnectionManager() {

    }

    public ConnectionManager(VSockAddress socketAddress) {
        this.vSockAddress = socketAddress;
    }

    public VSock getConnection() throws Exception {
        lock.lock();
        log.info("Current connection total count:{}", totalCount);
        try {
            VSock connection = null;
            while (true) {
                if (freeCount.get() > 0) {
                    freeCount.decrementAndGet();
                    connection = freeConnections.poll();
                    if ((System.currentTimeMillis() - connection.getLastAccessTime()) > ClientGlobal.g_connection_pool_max_idle_time) {
                        closeConnection(connection);
                        continue;
                    }
                } else if (ClientGlobal.g_connection_pool_max_count_per_entry == 0 || totalCount.get() < ClientGlobal.g_connection_pool_max_count_per_entry) {
                    connection = new VSock(new VSockAddress(this.cid, this.port));
                    totalCount.incrementAndGet();
                } else {
                    try {
                        if (condition.await(ClientGlobal.g_connection_pool_max_wait_time_in_ms, TimeUnit.MILLISECONDS)) {
                            //wait single success
                            continue;
                        }
                        throw new RuntimeException("connect to server " + vSockAddress.getCid() + ":" + vSockAddress.getPort() + " fail, wait_time > " + ClientGlobal.g_connection_pool_max_wait_time_in_ms + "ms");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        throw new RuntimeException("connect to server " + vSockAddress.getCid() + ":" + vSockAddress.getPort() + " fail, emsg:" + e.getMessage());
                    }
                }
                return connection;
            }
        } finally {
            lock.unlock();
        }
    }

    public void releaseConnection(VSock connection) {
        if (connection == null) {
            return;
        }
        lock.lock();
        try {
            connection.setLastAccessTime(System.currentTimeMillis());
            freeConnections.add(connection);
            freeCount.incrementAndGet();
            condition.signal();
        } finally {
            lock.unlock();
        }

    }

    public void closeConnection(VSock connection) {
        try {
            if (connection != null) {
                totalCount.decrementAndGet();
                connection.close();
            }
        } catch (IOException e) {
            System.err.println("close socket[" + vSockAddress.getCid() + ":" + vSockAddress.getPort() + "] error ,emsg:" + e.getMessage());
            e.printStackTrace();
        }
    }

    public void setActiveTestFlag() {
       /* if (freeCount.get() > 0) {
            lock.lock();
            try {
                for (Connection freeConnection : freeConnections) {
                    freeConnection.setNeedActiveTest(true);
                }
            } finally {
                lock.unlock();
            }
        }*/
    }


    @Override
    public String toString() {
        return "ConnectionManager{" +
                "ip:port='" + vSockAddress.getCid() + ":" + vSockAddress.getPort() +
                ", totalCount=" + totalCount +
                ", freeCount=" + freeCount +
                ", freeConnections =" + freeConnections +
                '}';
    }
}

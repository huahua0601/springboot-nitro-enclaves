package com.github.mrgatto.host.socketpool;

import solutions.cloudarchitects.vsockj.VSock;
import solutions.cloudarchitects.vsockj.VSockAddress;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionPool {
    /**
     * key is ip:port, value is ConnectionManager
     */
    private final static ConcurrentHashMap<String, ConnectionManager> CP = new ConcurrentHashMap<String, ConnectionManager>();

    public static VSock getConnection(VSockAddress vSockAddress) throws Exception {
        if (vSockAddress == null) {
            return null;
        }
        String key = getKey(vSockAddress);
        ConnectionManager connectionManager;
        connectionManager = CP.get(key);
        if (connectionManager == null) {
            synchronized (ConnectionPool.class) {
                connectionManager = CP.get(key);
                if (connectionManager == null) {
                    connectionManager = new ConnectionManager(vSockAddress);
                    CP.put(key, connectionManager);
                }
            }
        }
        return connectionManager.getConnection();
    }

    public static void releaseConnection(VSock connection) throws IOException {
        if (connection == null) {
            return;
        }
        String key = getKey(connection.getvSockAddress());
        ConnectionManager connectionManager = CP.get(key);
        if (connectionManager != null) {
            connectionManager.releaseConnection(connection);
        } else {
            connection.close();
        }

    }

    public static void closeConnection(VSock connection) throws IOException {
        if (connection == null) {
            return;
        }
        String key = getKey(connection.getvSockAddress());
        ConnectionManager connectionManager = CP.get(key);
        if (connectionManager != null) {
            connectionManager.closeConnection(connection);
            connectionManager.setActiveTestFlag();
        } else {
            connection.close();
        }
    }

    private static String getKey(VSockAddress vSockAddress) {
        if (vSockAddress == null) {
            return null;
        }
        return String.format("%s:%s", vSockAddress.getCid(), vSockAddress.getPort());
    }

    @Override
    public String toString() {
        if (!CP.isEmpty()) {
            StringBuilder builder = new StringBuilder();
            for (Map.Entry<String, ConnectionManager> managerEntry : CP.entrySet()) {
                builder.append("key:[" + managerEntry.getKey() + " ]-------- entry:" + managerEntry.getValue() + "\n");
            }
            return builder.toString();
        }
        return null;
    }
}

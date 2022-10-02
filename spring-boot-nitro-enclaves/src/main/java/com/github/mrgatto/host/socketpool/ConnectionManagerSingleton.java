package com.github.mrgatto.host.socketpool;

import solutions.cloudarchitects.vsockj.VSockAddress;

public class ConnectionManagerSingleton {

    private volatile static ConnectionManager connectionManager;

    private ConnectionManagerSingleton() {
    }

    public static ConnectionManager getInstance(VSockAddress address) {
        if (connectionManager == null) {
            synchronized (ConnectionManagerSingleton.class) {
                if (connectionManager == null) {
                    connectionManager = new ConnectionManager(address);
                }
            }
        }
        return connectionManager;
    }
}

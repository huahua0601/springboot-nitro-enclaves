package com.github.mrgatto.host.socketpool;

public class ClientGlobal {

    public static boolean g_connection_pool_enabled = true;
    public static int g_connection_pool_max_count_per_entry = 100;
    public static int g_connection_pool_max_idle_time = 3600 * 1000; //millisecond
    public static int g_connection_pool_max_wait_time_in_ms = 1000; //millisecond

}

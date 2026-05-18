package me.tju244.kop.notification.miui;

interface IMiIslandPrivilegedService {
    boolean setXmsfNetworkingEnabled(int uid, boolean enabled) = 1;
    oneway void destroy() = 16777114;
}


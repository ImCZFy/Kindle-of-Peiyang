package me.tju244.kop.notification.miui

import android.content.Context
import android.os.IBinder
import androidx.annotation.Keep
import kotlin.system.exitProcess

@Keep
class MiIslandPrivilegedService(private val context: Context) : IMiIslandPrivilegedService.Stub() {
    override fun setXmsfNetworkingEnabled(uid: Int, enabled: Boolean): Boolean {
        return runCatching {
            val binder = serviceBinder("connectivity") ?: return@runCatching false
            val manager = Class.forName("android.net.IConnectivityManager\$Stub")
                .getMethod("asInterface", IBinder::class.java)
                .invoke(null, binder)
                ?: return@runCatching false
            val chain = 9 // FIREWALL_CHAIN_OEM_DENY_3
            val rule = if (enabled) 0 else 2 // DEFAULT / DENY
            if (!enabled) {
                manager.javaClass
                    .getMethod("setFirewallChainEnabled", Int::class.javaPrimitiveType, Boolean::class.javaPrimitiveType)
                    .invoke(manager, chain, true)
            }
            manager.javaClass
                .getMethod("setUidFirewallRule", Int::class.javaPrimitiveType, Int::class.javaPrimitiveType, Int::class.javaPrimitiveType)
                .invoke(manager, chain, uid, rule)
            true
        }.getOrDefault(false)
    }

    override fun destroy() {
        exitProcess(0)
    }

    private fun serviceBinder(name: String): IBinder? {
        return Class.forName("android.os.ServiceManager")
            .getMethod("getService", String::class.java)
            .invoke(null, name) as? IBinder
    }
}


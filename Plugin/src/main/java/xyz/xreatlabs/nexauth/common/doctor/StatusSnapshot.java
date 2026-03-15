package xyz.xreatlabs.nexauth.common.doctor;

import com.google.gson.JsonObject;
import xyz.xreatlabs.nexauth.api.PlatformHandle;

public record StatusSnapshot(
        String version,
        String platform,
        String failurePolicyMode,
        boolean multiProxyEnabled,
        boolean emailEnabled,
        boolean totpEnabled,
        boolean limboIntegrationAvailable,
        PlatformHandle.ProxyData proxyData,
        JsonObject metrics
) {
}

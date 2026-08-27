package dev.rinchan.skillrespecpill.client;

import dev.rinchan.skillrespecpill.network.PolicySyncPayload;
import java.util.Map;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;

public final class ClientPolicyState {
    private static volatile Map<ResourceLocation, Set<String>> forcedByCategory = Map.of();
    private static volatile boolean cascadeRefundEnabled = true;
    private static volatile boolean ready;

    private ClientPolicyState() {
    }

    public static void beginRefresh() {
        forcedByCategory = Map.of();
        ready = false;
    }

    public static void accept(PolicySyncPayload payload) {
        forcedByCategory = payload.forcedByCategory();
        cascadeRefundEnabled = payload.cascadeRefundEnabled();
        ready = true;
    }

    public static boolean ready() {
        return ready;
    }

    public static boolean isForced(ResourceLocation categoryId, String nodeId) {
        return forcedByCategory.getOrDefault(categoryId, Set.of()).contains(nodeId);
    }

    public static boolean cascadeRefundEnabled() {
        return cascadeRefundEnabled;
    }
}

package dev.rinchan.skillrespecpill.service;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.UUID;

/** Owns the exact synchronous scope in which assignable points increase because of a refund. */
public final class PointMutationContext {
    private static final ThreadLocal<Deque<Scope>> REFUND_SCOPES = new ThreadLocal<>();

    private PointMutationContext() {
    }

    public static void runRefund(UUID playerId, String categoryId, Runnable operation) {
        Deque<Scope> scopes = REFUND_SCOPES.get();
        if (scopes == null) {
            scopes = new ArrayDeque<>();
            REFUND_SCOPES.set(scopes);
        }
        scopes.push(new Scope(playerId, categoryId));
        try {
            operation.run();
        } finally {
            scopes.pop();
            if (scopes.isEmpty()) REFUND_SCOPES.remove();
        }
    }

    public static boolean isRefunding(UUID playerId, String categoryId) {
        Deque<Scope> scopes = REFUND_SCOPES.get();
        return scopes != null && scopes.contains(new Scope(playerId, categoryId));
    }

    private record Scope(UUID playerId, String categoryId) {
    }
}

package com.training.microservices.song.trace;

import org.slf4j.MDC;

import java.util.UUID;

public final class TraceIdContext {

    private TraceIdContext() {
    }

    public static String get() {
        return MDC.get(TraceId.MDC_KEY);
    }

    public static void set(String traceId) {
        if (traceId != null && !traceId.isBlank()) {
            MDC.put(TraceId.MDC_KEY, traceId);
        }
    }

    public static void clear() {
        MDC.remove(TraceId.MDC_KEY);
    }

    public static String generate() {
        return UUID.randomUUID().toString();
    }
}

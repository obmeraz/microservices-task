package com.training.microservices.resource.trace;

public final class TraceId {

    public static final String HEADER = "X-Trace-Id";
    public static final String MDC_KEY = "traceId";

    private TraceId() {
    }
}

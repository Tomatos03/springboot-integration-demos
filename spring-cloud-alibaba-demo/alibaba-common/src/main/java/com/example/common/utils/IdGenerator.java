package com.example.common.utils;

public class IdGenerator {

    private static final long START_TIMESTAMP = 1704067200000L;
    private static final long SEQUENCE_BITS = 12;
    private static final long WORKER_ID_BITS = 5;
    private static final long DATACENTER_ID_BITS = 5;

    private static final long MAX_SEQUENCE = ~(-1L << SEQUENCE_BITS);

    private static long workerId = 1L;
    private static long datacenterId = 1L;
    private static long sequence = 0L;
    private static long lastTimestamp = -1L;

    private IdGenerator() {}

    public static synchronized long nextId() {
        long timestamp = System.currentTimeMillis();

        if (timestamp < lastTimestamp) {
            throw new RuntimeException("时钟回拨，拒绝生成ID");
        }

        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & MAX_SEQUENCE;
            if (sequence == 0) {
                timestamp = waitNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0;
        }

        lastTimestamp = timestamp;

        return ((timestamp - START_TIMESTAMP) << (SEQUENCE_BITS + WORKER_ID_BITS + DATACENTER_ID_BITS))
                | (datacenterId << (SEQUENCE_BITS + WORKER_ID_BITS))
                | (workerId << SEQUENCE_BITS)
                | sequence;
    }

    private static long waitNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }
}
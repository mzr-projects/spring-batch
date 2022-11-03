package com.mt.springbatch.entities;

import java.util.List;
import java.util.Random;

public enum Status {

    NEW,
    NOOB,
    SENIOR,
    OLD;

    private static final List<Status> VALUES = List.of(values());
    private static final int SIZE = VALUES.size();
    private static final Random RANDOM = new Random();

    public static Status randomStatus() {
        return VALUES.get(RANDOM.nextInt(SIZE));
    }
}

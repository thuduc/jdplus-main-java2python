package jdplus.toolkit.desktop.plugin.concurrent;

import nbbrd.design.NonNegative;

@lombok.AllArgsConstructor
public enum ThreadPriority {

    MIN(Thread.MIN_PRIORITY),
    NORMAL(Thread.NORM_PRIORITY),
    MAX(Thread.MAX_PRIORITY);

    private final int value;

    @NonNegative
    public int getPriority() {
        return value;
    }
}

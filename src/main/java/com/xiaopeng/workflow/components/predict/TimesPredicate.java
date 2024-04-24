package com.xiaopeng.workflow.components.predict;

import lombok.extern.slf4j.Slf4j;
import org.jeasy.flows.work.WorkReport;
import org.jeasy.flows.work.WorkReportPredicate;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class TimesPredicate implements WorkReportPredicate {
    private int times;

    private AtomicInteger counter = new AtomicInteger();

    public TimesPredicate(Integer times) {
        this.times = times;
    }

    @Override
    public boolean apply(WorkReport workReport) {
        log.info("TimesPredicate apply: {}", counter.get());
        return counter.incrementAndGet() != times;
    }

    public static TimesPredicate times(int times) {
        return new TimesPredicate(times);
    }
}

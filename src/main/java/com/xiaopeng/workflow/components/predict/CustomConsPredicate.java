package com.xiaopeng.workflow.components.predict;

import lombok.extern.slf4j.Slf4j;
import org.jeasy.flows.work.WorkReport;
import org.jeasy.flows.work.WorkReportPredicate;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 定制 构造方法入参键值对 示例 请看 simple_repeat.json
 */
@Slf4j
public class CustomConsPredicate implements WorkReportPredicate {
    private int times;

    private String componentName;

    private AtomicInteger counter = new AtomicInteger();

    public CustomConsPredicate(CustomEntity customEntity) {
        this.times = customEntity.getTimes() == null ? 0 : customEntity.getTimes();
        this.componentName = customEntity.getComponentName();
    }

    @Override
    public boolean apply(WorkReport workReport) {
        log.info("CustomConsPredicate apply: {} , 组件名称{}", counter.get(), componentName);
        return counter.incrementAndGet() != times;
    }
}

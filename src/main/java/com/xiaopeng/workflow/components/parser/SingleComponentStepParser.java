package com.xiaopeng.workflow.components.parser;

import com.xiaopeng.workflow.components.XPComponentStep;
import lombok.extern.slf4j.Slf4j;
import org.jeasy.flows.workflow.WorkFlow;

import java.util.Map;
import java.util.concurrent.ExecutorService;

@Slf4j
public class SingleComponentStepParser implements ComponentStepParser {
    @Override
    public WorkFlow parse(Map<String, WorkFlow> componentMap, XPComponentStep componentStep, ExecutorService threadPool) {
        String component = componentStep.getComponent();
        log.info("build single component:{}", component);
        WorkFlow workFlow = componentMap.get(component);
        if (workFlow == null) {
            log.error("{} is not found", component);
            throw new RuntimeException("component not found:" + component);
        }
        return workFlow;
    }
}

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
        log.info("build single component:{}", componentStep.getComponent());
        return componentMap.get(componentStep.getComponent());
    }
}

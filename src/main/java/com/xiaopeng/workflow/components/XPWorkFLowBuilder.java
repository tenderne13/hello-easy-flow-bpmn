package com.xiaopeng.workflow.components;

import com.xiaopeng.workflow.components.enums.ComponentType;
import com.xiaopeng.workflow.components.parser.*;
import lombok.extern.slf4j.Slf4j;
import org.jeasy.flows.workflow.WorkFlow;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

@Slf4j
public class XPWorkFLowBuilder {

    public static final Map<String, ComponentStepParser> componentStepParserMap = new HashMap<>();

    static {
        componentStepParserMap.put(ComponentType.SINGLE.getCode(), new SingleComponentStepParser());
        componentStepParserMap.put(ComponentType.PARALLEL.getCode(), new ParallelComponentStepParser());
        componentStepParserMap.put(ComponentType.SEQUENTIAL.getCode(), new SequentialComponentStepParser());
        componentStepParserMap.put(ComponentType.CONDITIONAL.getCode(), new MultConditionalComponentStepParser());
        componentStepParserMap.put(ComponentType.REPEAT.getCode(), new RepeatComponentStepParser());
    }


    public static WorkFlow buildWorkFlow(Map<String, WorkFlow> componentMap, XPComponentStep componentStep, ExecutorService threadPool) {
        String type = componentStep.getType();
        return componentStepParserMap.getOrDefault(type, (compMap, compStep, pool) -> null).parse(componentMap, componentStep, threadPool);
    }
}

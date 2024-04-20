package com.xiaopeng.workflow.components;

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
        componentStepParserMap.put("single", new SingleComponentStepParser());
        componentStepParserMap.put("parallel", new ParallelComponentStepParser());
        componentStepParserMap.put("sequential", new SequentialComponentStepParser());
        componentStepParserMap.put("conditional", new MultConditionalComponentStepParser());
        componentStepParserMap.put("repeat", new RepeatComponentStepParser());
    }


    public static WorkFlow buildWorkFlow(Map<String, WorkFlow> componentMap, XPComponentStep componentStep, ExecutorService threadPool) {
        String type = componentStep.getType();
        return componentStepParserMap.getOrDefault(type, (compMap, compStep, pool) -> null).parse(componentMap, componentStep, threadPool);
    }
}

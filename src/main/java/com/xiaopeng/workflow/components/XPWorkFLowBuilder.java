package com.xiaopeng.workflow.components;

import com.xiaopeng.workflow.components.parser.ComponentStepParser;
import com.xiaopeng.workflow.components.parser.ParallComponentStepParser;
import com.xiaopeng.workflow.components.parser.SeqComponentStepParser;
import com.xiaopeng.workflow.components.parser.SingleComponentStepParser;
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
        componentStepParserMap.put("parallel", new ParallComponentStepParser());
        componentStepParserMap.put("sequential", new SeqComponentStepParser());
    }


    public static WorkFlow buildWorkFlow(Map<String, WorkFlow> componentMap, XPComponentStep componentStep, ExecutorService threadPool) {
        String type = componentStep.getType();
        return componentStepParserMap.getOrDefault(type, (compMap, compStep, pool) -> null).parse(componentMap, componentStep, threadPool);
    }
}

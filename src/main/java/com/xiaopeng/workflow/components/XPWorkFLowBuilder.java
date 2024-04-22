package com.xiaopeng.workflow.components;

import com.xiaopeng.workflow.components.parser.*;
import lombok.extern.slf4j.Slf4j;
import org.jeasy.flows.workflow.WorkFlow;

import java.util.Map;
import java.util.concurrent.ExecutorService;

@Slf4j
public class XPWorkFLowBuilder {

    private static final SingleComponentStepParser singleComponentStepParser = new SingleComponentStepParser();
    private static final ParallelComponentStepParser parallelComponentStepParser = new ParallelComponentStepParser();
    private static final SequentialComponentStepParser sequentialComponentStepParser = new SequentialComponentStepParser();
    private static final MultiConditionalComponentStepParser multiConditionalComponentStepParser = new MultiConditionalComponentStepParser();
    private static final RepeatComponentStepParser repeatComponentStepParser = new RepeatComponentStepParser();

    public static WorkFlow buildWorkFlow(Map<String, WorkFlow> componentMap, XPComponentStep componentStep, ExecutorService threadPool) {
        WorkFlowType type = componentStep.getType();
        if (type == null) {
            return singleComponentStepParser.parse(componentMap, componentStep, threadPool);
        }
        switch (type) {
            case SINGLE:
            default:
                return singleComponentStepParser.parse(componentMap, componentStep, threadPool);
            case PARALLEL:
                return parallelComponentStepParser.parse(componentMap, componentStep, threadPool);
            case SEQUENTIAL:
                return sequentialComponentStepParser.parse(componentMap, componentStep, threadPool);
            case CONDITIONAL:
                return multiConditionalComponentStepParser.parse(componentMap, componentStep, threadPool);
            case REPEAT:
                return repeatComponentStepParser.parse(componentMap, componentStep, threadPool);
        }
    }
}

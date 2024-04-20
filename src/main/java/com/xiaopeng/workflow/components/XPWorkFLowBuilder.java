package com.xiaopeng.workflow.components;

import com.xiaopeng.workflow.components.factory.WorkReportPredicateFactory;
import com.xiaopeng.workflow.components.parser.*;
import lombok.extern.slf4j.Slf4j;
import org.jeasy.flows.work.DefaultWorkReport;
import org.jeasy.flows.work.WorkContext;
import org.jeasy.flows.work.WorkReportPredicate;
import org.jeasy.flows.work.WorkStatus;
import org.jeasy.flows.workflow.WorkFlow;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

@Slf4j
public class XPWorkFLowBuilder {

    public static final Map<String, ComponentStepParser> componentStepParserMap = new HashMap<>();

    static {
        componentStepParserMap.put("single", new SingleComponentStepParser());
        componentStepParserMap.put("parallel", new ParallelComponentStepParser());
        componentStepParserMap.put("sequential", new SequentialComponentStepParser());
        componentStepParserMap.put("conditional", new MultConditionalComponentStepParser());
    }


    public static WorkFlow buildWorkFlow(Map<String, WorkFlow> componentMap, XPComponentStep componentStep, ExecutorService threadPool) {
        String type = componentStep.getType();
        return componentStepParserMap.getOrDefault(type, (compMap, compStep, pool) -> null).parse(componentMap, componentStep, threadPool);
    }


    public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException {

        WorkReportPredicate predicate = WorkReportPredicateFactory.createPredicate("com.xiaopeng.workflow.components.predict.XGPTSwitchPredicate");
        WorkContext workContext = new WorkContext();
        workContext.put("XGPTSwitch", true);
        DefaultWorkReport defaultWorkReport = new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
        System.out.println(Optional.ofNullable(predicate.apply(defaultWorkReport)).orElse(false));
    }
}

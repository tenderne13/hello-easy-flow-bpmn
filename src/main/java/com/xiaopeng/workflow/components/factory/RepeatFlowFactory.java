package com.xiaopeng.workflow.components.factory;

import org.apache.commons.lang3.StringUtils;
import org.jeasy.flows.work.Work;
import org.jeasy.flows.work.WorkReportPredicate;
import org.jeasy.flows.workflow.RepeatFlow;

public class RepeatFlowFactory {

    private static final String DEFAULT_FLOW_NAME = "repeat flow";

    public static RepeatFlow buildRepeatFlow(String flowName, Work workUnit, WorkReportPredicate predicate) {
        if (StringUtils.isEmpty(flowName)) {
            flowName = DEFAULT_FLOW_NAME;
        }
        return RepeatFlow.Builder.aNewRepeatFlow().named(flowName).repeat(workUnit).until(predicate).build();
    }
}

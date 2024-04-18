package com.xiaopeng.workflow.components.factory;

import org.apache.commons.lang3.StringUtils;
import org.jeasy.flows.work.Work;
import org.jeasy.flows.workflow.SequentialFlow;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class SequentialFlowFactory {

    private static final String DEFAULT_FLOW_NAME = "sequential flow";

    public static SequentialFlow buildSequentialFlow(String flowName, List<? extends Work> workFlowList) {
        if (StringUtils.isEmpty(flowName)) {
            flowName = DEFAULT_FLOW_NAME;
        }
        return SequentialFlow.Builder.aNewSequentialFlow().named(flowName).execute(new ArrayList<>(workFlowList)).build();
    }
}

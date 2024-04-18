package com.xiaopeng.workflow.components.factory;

import org.apache.commons.lang3.StringUtils;
import org.jeasy.flows.work.Work;
import org.jeasy.flows.workflow.ParallelFlow;
import org.jeasy.flows.workflow.WorkFlow;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ParallelFlowFactory {

    private static final String DEFAULT_FLOW_NAME = "parallel flow";

    public static ParallelFlow buildParallelFlow(String flowName, List<? extends Work> workFlowList, ExecutorService threadPool) {
        if (StringUtils.isEmpty(flowName)) {
            flowName = DEFAULT_FLOW_NAME;
        }
        Work[] workFlowArray = new Work[workFlowList.size()];
        workFlowList.toArray(workFlowArray);
        return ParallelFlow.Builder.aNewParallelFlow().named(flowName).execute(workFlowArray).with(threadPool).build();
    }
}

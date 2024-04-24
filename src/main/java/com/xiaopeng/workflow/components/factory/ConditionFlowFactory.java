package com.xiaopeng.workflow.components.factory;

import com.xiaopeng.workflow.components.base.MultiConditionalFlow;
import org.apache.commons.lang3.StringUtils;
import org.jeasy.flows.work.NoOpWork;
import org.jeasy.flows.work.Work;
import org.jeasy.flows.work.WorkReportPredicate;
import org.jeasy.flows.workflow.ConditionalFlow;

import java.util.ArrayList;
import java.util.List;

import static com.xiaopeng.workflow.components.base.MultiConditionalFlow.Builder.aNewMultiConditionalFlow;

public class ConditionFlowFactory {

    private static final String DEFAULT_FLOW_NAME = "condition flow";

    public static ConditionalFlow buildConditionalFlow(String flowName, Work initWorkUnit, Work thenWorkUnit, Work otherwiseWorkUnit, WorkReportPredicate workReportPredicate) {
        if (StringUtils.isEmpty(flowName)) {
            flowName = DEFAULT_FLOW_NAME;
        }
        if (initWorkUnit == null) {
            initWorkUnit = new NoOpWork();
        }
        if (thenWorkUnit == null) {
            thenWorkUnit = new NoOpWork();
        }
        if (otherwiseWorkUnit == null) {
            otherwiseWorkUnit = new NoOpWork();
        }
        return ConditionalFlow.Builder.aNewConditionalFlow().named(flowName).execute(initWorkUnit).when(workReportPredicate).then(thenWorkUnit).otherwise(otherwiseWorkUnit).build();
    }

    /**
     * 构建多条件工作流
     *
     * @param predictWorkPairs
     * @param otherWishWork
     * @return
     */
    public static MultiConditionalFlow buildMultiConditionalFlow(List<MultiConditionalFlow.PredictWorkPair> predictWorkPairs, Work otherWishWork) {

        if (predictWorkPairs == null) {
            predictWorkPairs = new ArrayList<>();
        }
        if (otherWishWork == null) {
            otherWishWork = new NoOpWork();
        }
        return aNewMultiConditionalFlow().withThenWorkList(predictWorkPairs).withOtherWishWork(otherWishWork).build();
    }
}

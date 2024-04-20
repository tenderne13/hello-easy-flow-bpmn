package com.xiaopeng.workflow.components.base;

import cn.hutool.core.collection.CollectionUtil;
import com.xiaopeng.workflow.components.XPComponentStep;
import com.xiaopeng.workflow.components.XPConditionStep;
import com.xiaopeng.workflow.components.constants.FlowConstants;
import com.xiaopeng.workflow.components.factory.WorkReportPredicateFactory;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jeasy.flows.work.*;
import org.jeasy.flows.workflow.WorkFlow;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import static com.xiaopeng.workflow.components.XPWorkFLowBuilder.buildWorkFlow;

/**
 * 多条件工作流
 */
@Slf4j
public class MulitConditionalFlow implements WorkFlow {

    private List<PredictWorkPair> thenWorkList = new ArrayList<>();
    private Work otherWishWork = new NoOpWork();

    public MulitConditionalFlow(String name, List<XPConditionStep> conditionSteps, Map<String, WorkFlow> componentMap, ExecutorService threadPool) {
        initConditionFlow(conditionSteps, componentMap, threadPool);
    }

    private void initConditionFlow(List<XPConditionStep> conditionSteps, Map<String, WorkFlow> componentMap, ExecutorService threadPool) {
        if (CollectionUtil.isNotEmpty(conditionSteps)) {
            List<XPConditionStep> thenSteps = conditionSteps.stream().filter(step -> FlowConstants.THEN_STEP.equals(step.getConditionStep())).toList();
            // 多条件IF
            thenWorkList = thenSteps.stream().map(step -> {
                XPComponentStep componentStep = step.getComponentStep();
                String predicateClassName = step.getPredicateClassName();
                WorkFlow workFlow = buildWorkFlow(componentMap, componentStep, threadPool);
                WorkReportPredicate predicate = StringUtils.isNotEmpty(predicateClassName) ? WorkReportPredicateFactory.createPredicate(predicateClassName) : WorkReportPredicate.ALWAYS_FALSE;
                return new PredictWorkPair(predicate, workFlow);
            }).collect(Collectors.toList());

            // 兜底逻辑
            Optional<XPConditionStep> otherStep = conditionSteps.stream().filter(step -> FlowConstants.OTHERWISE_STEP.equals(step.getConditionStep())).findFirst();
            if (otherStep.isPresent()) {
                XPConditionStep xpConditionStep = otherStep.get();
                XPComponentStep componentStep = xpConditionStep.getComponentStep();
                this.otherWishWork = buildWorkFlow(componentMap, componentStep, threadPool);
            }
        }
    }

    @Override
    public WorkReport execute(WorkContext workContext) {
        WorkReport workReport = new DefaultWorkReport(WorkStatus.FAILED, workContext);
        for (PredictWorkPair predictWorkPair : thenWorkList) {
            WorkReportPredicate predicate = predictWorkPair.getPredicate();
            Work work = predictWorkPair.getWork();
            if (predicate.apply(workReport)) {
                return work.execute(workContext);
            }
        }
        if (otherWishWork != null) {
            return otherWishWork.execute(workContext);
        }

        return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
    }


    @Builder
    public static class PredictWorkPair {
        private WorkReportPredicate predicate;
        private Work work;

        public PredictWorkPair(WorkReportPredicate predicate, Work work) {
            this.predicate = predicate;
            this.work = work;
        }

        public WorkReportPredicate getPredicate() {
            return predicate;
        }

        public void setPredicate(WorkReportPredicate predicate) {
            this.predicate = predicate;
        }

        public Work getWork() {
            return work;
        }

        public void setWork(Work work) {
            this.work = work;
        }
    }
}

package com.xiaopeng.workflow.components.parser;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONUtil;
import com.xiaopeng.workflow.components.XPComponentStep;
import com.xiaopeng.workflow.components.XPConditionStep;
import com.xiaopeng.workflow.components.base.MultiConditionalFlow;
import com.xiaopeng.workflow.components.constants.FlowConstants;
import com.xiaopeng.workflow.components.factory.ConditionFlowFactory;
import com.xiaopeng.workflow.components.factory.WorkReportPredicateFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jeasy.flows.work.NoOpWork;
import org.jeasy.flows.work.Work;
import org.jeasy.flows.work.WorkReportPredicate;
import org.jeasy.flows.workflow.WorkFlow;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

import static com.xiaopeng.workflow.components.XPWorkFLowBuilder.buildWorkFlow;

@Slf4j
public class MultiConditionalComponentStepParser implements ComponentStepParser {
    @Override
    public WorkFlow parse(Map<String, WorkFlow> componentMap, XPComponentStep componentStep, ExecutorService threadPool) {
        String stepId = RandomUtil.randomString(8);

        log.info("===================> conditional {} build start <====================", stepId);
        List<XPConditionStep> conditionSteps = componentStep.getConditionSteps();

        // 1, 构建IF THEN 工作流
        List<MultiConditionalFlow.PredictWorkPair> predictWorkPairs = buildPredictWorkPairList(conditionSteps, componentMap, threadPool);
        // 2, 构建OTHERWISE 工作流
        Work otherWishWork = buildOtherWishWork(conditionSteps, componentMap, threadPool);
        MultiConditionalFlow conditionalFlow = ConditionFlowFactory.buildMultiConditionalFlow(predictWorkPairs, otherWishWork);
        log.info("===================> conditional {} flow build success, component info  ==> {} <===", stepId, JSONUtil.toJsonStr(componentStep));
        return conditionalFlow;
    }

    private Work buildOtherWishWork(List<XPConditionStep> conditionSteps, Map<String, WorkFlow> componentMap, ExecutorService threadPool) {
        Optional<XPConditionStep> otherStep = conditionSteps.stream().filter(step -> FlowConstants.OTHERWISE_STEP.equals(step.getConditionStep())).findFirst();
        if (otherStep.isPresent()) {
            XPConditionStep xpConditionStep = otherStep.get();
            XPComponentStep componentStep = xpConditionStep.getComponentStep();
            return buildWorkFlow(componentMap, componentStep, threadPool);
        }
        return new NoOpWork();
    }

    private List<MultiConditionalFlow.PredictWorkPair> buildPredictWorkPairList(List<XPConditionStep> conditionSteps, Map<String, WorkFlow> componentMap, ExecutorService threadPool) {
        return conditionSteps.stream().filter(step -> FlowConstants.THEN_STEP.equals(step.getConditionStep())).map(step -> {
            XPComponentStep componentStep = step.getComponentStep();
            // 构建命中该条件分支时的工作流
            WorkFlow workFlow = buildWorkFlow(componentMap, componentStep, threadPool);
            // 构建条件判断器
            String predicateClassName = step.getPredicateClassName();
            WorkReportPredicate predicate = StringUtils.isNotEmpty(predicateClassName) ? WorkReportPredicateFactory.createPredicate(predicateClassName, step.getPredicateParameterTypes()) : WorkReportPredicate.ALWAYS_FALSE;
            return new MultiConditionalFlow.PredictWorkPair(predicate, workFlow);
        }).toList();
    }
}

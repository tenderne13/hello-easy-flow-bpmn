package com.xiaopeng.workflow.components.parser;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONUtil;
import com.xiaopeng.workflow.components.XPComponentStep;
import com.xiaopeng.workflow.components.constants.FlowConstants;
import com.xiaopeng.workflow.components.factory.ConditionFlowFactory;
import com.xiaopeng.workflow.components.factory.SequentialFlowFactory;
import com.xiaopeng.workflow.components.factory.WorkReportPredicateFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jeasy.flows.work.WorkReportPredicate;
import org.jeasy.flows.workflow.WorkFlow;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

import static com.xiaopeng.workflow.components.XPWorkFLowBuilder.buildWorkFlow;

@Slf4j
public class ConComponentStepParser implements ComponentStepParser {
    @Override
    public WorkFlow parse(Map<String, WorkFlow> componentMap, XPComponentStep componentStep, ExecutorService threadPool) {
        String stepId = RandomUtil.randomString(8);

        log.info("===================> conditional {} build start <====================", stepId);
        // initWorkUnit component 可以为空
        String component = componentStep.getComponent();
        String predicateClassName = componentStep.getPredicateClassName();
        WorkReportPredicate predicate = WorkReportPredicateFactory.createPredicate(predicateClassName);

        List<XPComponentStep> sequentialSteps = componentStep.getSequentialSteps();
        //获取 thenWorkUnit
        Optional<XPComponentStep> thenStep = sequentialSteps.stream().filter(step -> FlowConstants.THEN_STEP.equals(step.getConditionStep())).findFirst();

        //获取otherwiseWorkUnit
        Optional<XPComponentStep> otherWishStep = sequentialSteps.stream().filter(step -> FlowConstants.OTHERWISE_STEP.equals(step.getConditionStep())).findFirst();

        WorkFlow initWorkUnit = null;
        WorkFlow thenWorkFlow = null;
        WorkFlow otherwiseWorkFlow = null;
        if (StringUtils.isNotEmpty(component)) {
            initWorkUnit = componentMap.get(component);
        }
        if (thenStep.isPresent()) {
            thenWorkFlow = buildWorkFlow(componentMap, thenStep.get(), threadPool);
        }
        if (otherWishStep.isPresent()) {
            otherwiseWorkFlow = buildWorkFlow(componentMap, otherWishStep.get(), threadPool);
        }
        log.info("=======> build conditional {} flow success, component info  ==> {} <===", stepId, JSONUtil.toJsonStr(componentStep));
        return ConditionFlowFactory.buildConditionalFlow(componentStep.getName(), initWorkUnit, thenWorkFlow, otherwiseWorkFlow, predicate);
    }
}

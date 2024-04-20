package com.xiaopeng.workflow.components.parser;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONUtil;
import com.xiaopeng.workflow.components.XPComponentStep;
import com.xiaopeng.workflow.components.XPConditionStep;
import com.xiaopeng.workflow.components.base.MulitConditionalFlow;
import com.xiaopeng.workflow.components.constants.FlowConstants;
import com.xiaopeng.workflow.components.factory.ConditionFlowFactory;
import com.xiaopeng.workflow.components.factory.WorkReportPredicateFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jeasy.flows.work.WorkReportPredicate;
import org.jeasy.flows.workflow.WorkFlow;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

import static com.xiaopeng.workflow.components.XPWorkFLowBuilder.buildWorkFlow;

@Slf4j
public class MultConditionalComponentStepParser implements ComponentStepParser {
    @Override
    public WorkFlow parse(Map<String, WorkFlow> componentMap, XPComponentStep componentStep, ExecutorService threadPool) {
        String stepId = RandomUtil.randomString(8);

        log.info("===================> conditional {} build start <====================", stepId);
        List<XPConditionStep> conditionSteps = componentStep.getConditionSteps();
        MulitConditionalFlow mulitConditionalFlow = new MulitConditionalFlow(componentStep.getName(), conditionSteps, componentMap, threadPool);
        log.info("===================> conditional {} flow build success, component info  ==> {} <===", stepId, JSONUtil.toJsonStr(componentStep));
        return mulitConditionalFlow;
    }
}

package com.xiaopeng.workflow.components.parser;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONUtil;
import com.xiaopeng.workflow.components.XPComponentStep;
import com.xiaopeng.workflow.components.XPRepeatStep;
import com.xiaopeng.workflow.components.factory.RepeatFlowFactory;
import com.xiaopeng.workflow.components.factory.WorkReportPredicateFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jeasy.flows.work.WorkReportPredicate;
import org.jeasy.flows.workflow.WorkFlow;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import static com.xiaopeng.workflow.components.XPWorkFLowBuilder.buildWorkFlow;

@Slf4j
public class RepeatComponentStepParser implements ComponentStepParser {
    @Override
    public WorkFlow parse(Map<String, WorkFlow> componentMap, XPComponentStep componentStep, ExecutorService threadPool) {
        String stepId = RandomUtil.randomString(8);

        log.info("===================> repeat {} build start <====================", stepId);
        XPRepeatStep repeatStep = componentStep.getRepeatStep();
        String predicateClassName = repeatStep.getPredicateClassName();
        WorkReportPredicate predicate = StringUtils.isNotEmpty(predicateClassName) ? WorkReportPredicateFactory.createPredicate(predicateClassName) : WorkReportPredicate.ALWAYS_FALSE;
        WorkFlow workFlow = buildWorkFlow(componentMap, repeatStep.getComponentStep(), threadPool);
        log.info("===================> repeat {} flow build success, component info  ==> {} <===", stepId, JSONUtil.toJsonStr(componentStep));
        return RepeatFlowFactory.buildRepeatFlow(componentStep.getName(), workFlow, predicate);
    }
}

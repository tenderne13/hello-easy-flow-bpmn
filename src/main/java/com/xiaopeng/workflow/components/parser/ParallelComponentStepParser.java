package com.xiaopeng.workflow.components.parser;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONUtil;
import com.xiaopeng.workflow.components.XPComponentStep;
import com.xiaopeng.workflow.components.factory.ParallelFlowFactory;
import lombok.extern.slf4j.Slf4j;
import org.jeasy.flows.workflow.WorkFlow;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import static com.xiaopeng.workflow.components.XPWorkFLowBuilder.buildWorkFlow;

@Slf4j
public class ParallelComponentStepParser implements ComponentStepParser {
    @Override
    public WorkFlow parse(Map<String, WorkFlow> componentMap, XPComponentStep componentStep, ExecutorService threadPool) {
        String stepId = RandomUtil.randomString(8);
        log.info("===================> parallel {} build start <====================", stepId);
        List<XPComponentStep> parallelSteps = componentStep.getParallelSteps();
        List<WorkFlow> tmpWorkFlows = new ArrayList<>();
        for (XPComponentStep parallelStep : parallelSteps) {
            tmpWorkFlows.add(buildWorkFlow(componentMap, parallelStep, threadPool));
        }
        log.info("===================> parallel {} flow build success, component info  ==> {} <===", stepId, JSONUtil.toJsonStr(componentStep));
        return ParallelFlowFactory.buildParallelFlow(componentStep.getName(), tmpWorkFlows, threadPool);
    }
}

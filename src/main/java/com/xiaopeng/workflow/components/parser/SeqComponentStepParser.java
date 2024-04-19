package com.xiaopeng.workflow.components.parser;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONUtil;
import com.xiaopeng.workflow.components.XPComponentStep;
import com.xiaopeng.workflow.components.factory.SequentialFlowFactory;
import lombok.extern.slf4j.Slf4j;
import org.jeasy.flows.workflow.WorkFlow;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import static com.xiaopeng.workflow.components.XPWorkFLowBuilder.buildWorkFlow;

@Slf4j
public class SeqComponentStepParser implements ComponentStepParser {
    @Override
    public WorkFlow parse(Map<String, WorkFlow> componentMap, XPComponentStep componentStep, ExecutorService threadPool) {
        String stepId = RandomUtil.randomString(8);

        log.info("===================> sequential {} build start <====================", stepId);
        List<XPComponentStep> sequentialSteps = componentStep.getSequentialSteps();
        List<WorkFlow> tmpSeqWorkFlows = new ArrayList<>();
        for (XPComponentStep parallelStep : sequentialSteps) {
            tmpSeqWorkFlows.add(buildWorkFlow(componentMap, parallelStep, threadPool));
        }
        log.info("=======> build sequential {} flow success, component info  ==> {} <===", stepId, JSONUtil.toJsonStr(componentStep));
        return SequentialFlowFactory.buildSequentialFlow(componentStep.getName(), tmpSeqWorkFlows);
    }
}

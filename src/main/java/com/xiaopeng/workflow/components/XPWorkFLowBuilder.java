package com.xiaopeng.workflow.components;

import cn.hutool.json.JSONUtil;
import com.xiaopeng.workflow.components.factory.ParallelFlowFactory;
import com.xiaopeng.workflow.components.factory.SequentialFlowFactory;
import lombok.extern.slf4j.Slf4j;
import org.jeasy.flows.workflow.WorkFlow;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

@Slf4j
public class XPWorkFLowBuilder {

    public static WorkFlow buildWorkFlow(Map<String, WorkFlow> componentMap, XPComponentStep componentStep, ExecutorService threadPool) {
        String type = componentStep.getType();
        String name = componentStep.getName();
        if ("single".equals(type)) {
            log.info("build single component:{}", componentStep.getComponent());
            return componentMap.get(componentStep.getComponent());
        } else if ("parallel".equals(type)) {
            log.info("===================> parallel build start <====================");
            List<XPComponentStep> parallelSteps = componentStep.getParallelSteps();
            List<WorkFlow> tmpWorkFlows = new ArrayList<>();
            for (XPComponentStep parallelStep : parallelSteps) {
                tmpWorkFlows.add(buildWorkFlow(componentMap, parallelStep, threadPool));
            }
            log.info("=======> build parallel flow success, component info  ==> {} <===", JSONUtil.toJsonStr(componentStep));
            return ParallelFlowFactory.buildParallelFlow(name, tmpWorkFlows, threadPool);
        } else if ("condition".equals(type)) {

        } else if ("sequential".equals(type)) {
            log.info("===================> sequential build start <====================");
            List<XPComponentStep> sequentialSteps = componentStep.getSequentialSteps();
            List<WorkFlow> tmpSeqWorkFlows = new ArrayList<>();
            for (XPComponentStep parallelStep : sequentialSteps) {
                tmpSeqWorkFlows.add(buildWorkFlow(componentMap, parallelStep, threadPool));
            }
            log.info("=======> build sequential flow success, component info  ==> {} <===", JSONUtil.toJsonStr(componentStep));
            return SequentialFlowFactory.buildSequentialFlow(name, tmpSeqWorkFlows);
        } else if ("loop".equals(type)) {

        } else if ("switch".equals(type)) {

        } else if ("subflow".equals(type)) {

        } else if ("retry".equals(type)) {

        } else if ("timeout".equals(type)) {

        }
        return null;
    }
}

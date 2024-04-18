package com.xiaopeng.workflow.components;

import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.jeasy.flows.workflow.WorkFlow;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import static org.jeasy.flows.workflow.ParallelFlow.Builder.aNewParallelFlow;
import static org.jeasy.flows.workflow.SequentialFlow.Builder.aNewSequentialFlow;


@Slf4j
public class XPWorkFLowBuilder {

    public static WorkFlow buildWorkFlow(Map<String, WorkFlow> componentMap, XPComponentStep componentStep, ExecutorService threadPool) {
        String type = componentStep.getType();
        String name = componentStep.getName();
        if ("single".equals(type)) {
            log.info("build single component:{}", componentStep.getComponent());
            return componentMap.get(componentStep.getComponent());
        } else if ("parallel".equals(type)) {
            log.info("build parallel component:{}", JSONUtil.toJsonStr(componentStep));
            List<XPComponentStep> parallelSteps = componentStep.getParallelSteps();
            List<WorkFlow> tmpWorkFlows = new ArrayList<>();
            for (XPComponentStep parallelStep : parallelSteps) {
                tmpWorkFlows.add(buildWorkFlow(componentMap, parallelStep, threadPool));
            }
            WorkFlow[] workFlowArray = new WorkFlow[parallelSteps.size()];
            tmpWorkFlows.toArray(workFlowArray);
            return aNewParallelFlow().named(name).execute(workFlowArray).with(threadPool).build();
        } else if ("condition".equals(type)) {

        } else if ("sequential".equals(type)) {
            log.info("build sequential component:{}", JSONUtil.toJsonStr(componentStep));
            List<XPComponentStep> sequentialSteps = componentStep.getSequentialSteps();
            List<WorkFlow> tmpSeqWorkFlows = new ArrayList<>();
            for (XPComponentStep parallelStep : sequentialSteps) {
                tmpSeqWorkFlows.add(buildWorkFlow(componentMap, parallelStep, threadPool));
            }
            return aNewSequentialFlow().execute(new ArrayList<>(tmpSeqWorkFlows)).build();
        } else if ("loop".equals(type)) {

        } else if ("switch".equals(type)) {

        } else if ("subflow".equals(type)) {

        } else if ("retry".equals(type)) {

        } else if ("timeout".equals(type)) {

        }
        return null;
    }
}

package com.xiaopeng.workflow.converter;

import com.xiaopeng.workflow.components.WorkFlowType;
import com.xiaopeng.workflow.components.XPComponentStep;
import org.camunda.bpm.model.bpmn.Query;
import org.camunda.bpm.model.bpmn.instance.FlowNode;

import java.util.ArrayList;

import static com.xiaopeng.workflow.converter.CamundaBpmnConverter.buildXPComponentStepBeta;

public class ParallelComponentStepConverter implements ComponentStepConverter {
    @Override
    public XPComponentStep convert(FlowNode flowNode) {
        //该flowNode 传入的对象为 StartEvent
        XPComponentStep parallelStep = XPComponentStep.builder().type(WorkFlowType.PARALLEL).parallelSteps(new ArrayList<>()).build();
        //并行节点默认 节点next没有节点
        for (FlowNode node : flowNode.getSucceedingNodes().list()) {
            parallelStep.getParallelSteps().add(buildXPComponentStepBeta(node));
        }
        return parallelStep;
    }
}

package com.xiaopeng.workflow.converter;

import cn.hutool.json.JSONUtil;
import com.xiaopeng.workflow.components.WorkFlowType;
import com.xiaopeng.workflow.components.XPComponentStep;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.model.bpmn.instance.FlowNode;

import java.util.ArrayList;

import static com.xiaopeng.workflow.converter.CamundaBpmnConverter.buildXPComponentStepBeta;
import static com.xiaopeng.workflow.util.FlowNodeUtil.hasNext;

@Slf4j
public class SequentialComponentStepConverter implements ComponentStepConverter {
    @Override
    public XPComponentStep convert(FlowNode flowNode) {
        //flowNode 为 StartEvent
        FlowNode headNode = flowNode.getSucceedingNodes().list().get(0);
        //将 realFlowNodeList 链表展开成list
        XPComponentStep sequentialStep = XPComponentStep.builder().type(WorkFlowType.SEQUENTIAL).sequentialSteps(new ArrayList<>()).build();
        do {
            sequentialStep.getSequentialSteps().add(buildXPComponentStepBeta(headNode));
        } while (hasNext(headNode) && (headNode = headNode.getSucceedingNodes().list().get(0)) != null);
        log.info("sequentialStep : {} build success", JSONUtil.toJsonStr(sequentialStep));
        return sequentialStep;
    }
}

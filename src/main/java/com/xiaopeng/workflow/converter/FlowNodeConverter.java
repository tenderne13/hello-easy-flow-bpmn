package com.xiaopeng.workflow.converter;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowNode;

import java.util.Map;

public interface FlowNodeConverter {
    
    /**
     * Do convert.
     * @param flowNode
     * @param bpmnModel
     * @param flowDef
     * @return
     */
    Map<String, Object> convert(FlowNode flowNode, BpmnModel bpmnModel, Map<String, Object> flowDef);
}

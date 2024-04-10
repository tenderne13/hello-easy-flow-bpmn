package com.xiaopeng.workflow.converter.event;


import com.xiaopeng.workflow.converter.BaseFlowNodeConverter;
import com.xiaopeng.workflow.converter.constant.DefConstants;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowNode;

import java.util.Map;

public class StartEventConverter extends BaseFlowNodeConverter {

    @Override
    public Map<String, Object> convert(FlowNode flowNode, BpmnModel bpmnModel, Map<String, Object> flowDef) {
        Map<String, Object> node = super.convert(flowNode, bpmnModel, flowDef);
        node.put(DefConstants.NODE_PROP_START, true);
        return node;
    }
}

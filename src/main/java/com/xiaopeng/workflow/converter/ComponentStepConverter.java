package com.xiaopeng.workflow.converter;

import com.xiaopeng.workflow.components.XPComponentStep;
import org.camunda.bpm.model.bpmn.instance.FlowNode;

@FunctionalInterface
public interface ComponentStepConverter {
    XPComponentStep convert(FlowNode flowNode);
}

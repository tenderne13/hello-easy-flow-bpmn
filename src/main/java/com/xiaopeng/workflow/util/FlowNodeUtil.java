package com.xiaopeng.workflow.util;

import cn.hutool.core.collection.CollectionUtil;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.model.bpmn.instance.ExtensionElements;
import org.camunda.bpm.model.bpmn.instance.FlowNode;
import org.camunda.bpm.model.bpmn.instance.SequenceFlow;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperties;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperty;

import java.util.List;
import java.util.Optional;

public class FlowNodeUtil {

    public static boolean hasNext(FlowNode flowNode) {
        return flowNode != null && flowNode.getSucceedingNodes().count() > 0;
    }

    public static String getPredicateClassName(SequenceFlow sequenceFlow) {
        return getSingleExtensionPropertie(sequenceFlow.getExtensionElements(), "predicateClassName");
    }

    public static String getSingleExtensionPropertie(ExtensionElements extensionElements, String key) {
        if (extensionElements != null) {
            List<CamundaProperties> list = extensionElements.getElementsQuery().filterByType(CamundaProperties.class).list();
            if (CollectionUtil.isNotEmpty(list)) {
                CamundaProperties camundaProperties = list.get(0);
                Optional<CamundaProperty> propertyOptional = camundaProperties.getCamundaProperties().stream().filter(item -> StringUtils.isNotEmpty(key) && key.equals(item.getCamundaName())).findFirst();
                if (propertyOptional.isPresent()) {
                    CamundaProperty property = propertyOptional.get();
                    return property.getCamundaValue();
                }
            }
        }
        return "";
    }
}

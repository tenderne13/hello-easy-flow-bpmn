package com.xiaopeng.workflow.converter;


import cn.hutool.json.JSONUtil;
import com.xiaopeng.workflow.converter.constant.BpmnXmlConstants;
import com.xiaopeng.workflow.converter.constant.DefConstants;
import com.xiaopeng.workflow.util.ConvertUtil;
import org.activiti.bpmn.model.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BaseFlowNodeConverter implements FlowNodeConverter {

    private static final Logger logger = LoggerFactory.getLogger(BaseFlowNodeConverter.class);

    private static final String CONDITION_TYPE_CREATE_EXP = "createExp";
    private static final String CONDITION_TYPE_CREATE_EXP_PREFIX = CONDITION_TYPE_CREATE_EXP + ":";


    @Override
    public Map<String, Object> convert(FlowNode flowNode, BpmnModel bpmnModel, Map<String, Object> flowDef) {
        Map<String, Object> node = new HashMap<>();
        // 1.填充id
        node.put(DefConstants.COMMON_PROP_ID, flowNode.getId());
        if (StringUtils.isNotEmpty(flowNode.getName())) {
            node.put(DefConstants.COMMON_PROP_NAME, flowNode.getName());
        }


        // 2.设置额外属性
        Map<String, Object> properties = null;
        Map<String, List<ExtensionElement>> extensionElementMap = flowNode.getExtensionElements();
        setExtenProperties(flowNode, extensionElementMap, node);

        // Post
        // self first.
        if (extensionElementMap != null && extensionElementMap.containsKey(BpmnXmlConstants.POST)) {
            ExtensionElement element = extensionElementMap.get(BpmnXmlConstants.POST).get(0);
            String elementText = element.getElementText();
            node.put(DefConstants.NODE_PROP_POST, JSONUtil.toBean(elementText, Map.class));
        } else {
            String defaultFlow = null;
            if (flowNode instanceof Activity) {
                defaultFlow = ((Activity) flowNode).getDefaultFlow();
            } else if (flowNode instanceof Gateway) {
                defaultFlow = ((Gateway) flowNode).getDefaultFlow();
            }
            List<SequenceFlow> sequenceFlowList = flowNode.getOutgoingFlows();
            if (sequenceFlowList.size() == 1) {
                Map<String, Object> post = new HashMap<>();
                node.put(DefConstants.NODE_PROP_POST, post);
                SequenceFlow sequenceFlow = sequenceFlowList.get(0);
                String conditionExp = sequenceFlow.getConditionExpression();
                if (StringUtils.isNotEmpty(conditionExp)) {
                    conditionExp = conditionExp.trim();
                    if (conditionExp.startsWith(CONDITION_TYPE_CREATE_EXP_PREFIX)) {
                        Map<String, Object> condition = new HashMap<String, Object>();
                        condition.put(CONDITION_TYPE_CREATE_EXP, conditionExp.substring(CONDITION_TYPE_CREATE_EXP_PREFIX.length()));
                    } else {
                        post.put(DefConstants.NODE_POST_PROP_WHEN, conditionExp);
                    }
                }
                post.put(DefConstants.NODE_POST_PROP_TO, sequenceFlow.getTargetRef());
            } else if (sequenceFlowList.size() > 1) {
                Map<String, Object> post = new HashMap<>();
                node.put(DefConstants.NODE_PROP_POST, post);
                List<Map<String, Object>> conditionList = new ArrayList<>();
                post.put(DefConstants.NODE_POST_PROP_CONDITIONS, conditionList);
                for (SequenceFlow sequenceFlow : sequenceFlowList) {
                    if (sequenceFlow.getId().equals(defaultFlow)) {
                        post.put(DefConstants.NODE_POST_PROP_DEFAULT_TO, sequenceFlow.getTargetRef());
                    } else {
                        Map<String, Object> condition = new HashMap<>();
                        if (StringUtils.isNotEmpty(sequenceFlow.getConditionExpression())) {
                            condition.put(DefConstants.NODE_POST_PROP_WHEN, sequenceFlow.getConditionExpression());
                        }
                        condition.put(DefConstants.NODE_POST_PROP_TO, sequenceFlow.getTargetRef());
                        conditionList.add(condition);
                    }
                }

                // conditionType
                String conditionType = "inclusive";
                if (extensionElementMap != null && extensionElementMap.containsKey(BpmnXmlConstants.CONDITION_TYPE)) {
                    conditionType = (String) (extensionElementMap.get(BpmnXmlConstants.CONDITION_TYPE)).get(0)
                            .getElementText();
                }
                if (!"exclusive".equals(conditionType)) {
                    post.put(DefConstants.NODE_POST_PROP_CONDITION_TYPE, conditionType);
                }
            }
        }
        return node;
    }

    private static void setExtenProperties(FlowNode flowNode, Map<String, List<ExtensionElement>> extensionElementMap, Map<String, Object> node) {
        Map<String, Object> properties;
        if (extensionElementMap != null && extensionElementMap.containsKey(BpmnXmlConstants.PROPERTIES)) {
            properties = ConvertUtil.getMapValue(node, DefConstants.COMMON_PROP_PROPERTIES);
            ExtensionElement element = extensionElementMap.get(BpmnXmlConstants.PROPERTIES).get(0);
            String elementText = element.getElementText();
            try {
                Map<String, Object> map = JSONUtil.toBean(elementText, Map.class);
                properties.putAll(map);
            } catch (Exception e) {
                throw new RuntimeException("Property JSON parse error, Node:" + flowNode.getId() + ", Property:" + elementText + "." + e.getMessage(), e);
            }
        }

        // Start
        if (extensionElementMap != null && extensionElementMap.containsKey(BpmnXmlConstants.START)) {
            ExtensionElement element = extensionElementMap.get(BpmnXmlConstants.START).get(0);
            String elementText = element.getElementText();
            node.put(DefConstants.NODE_PROP_START, JSONUtil.toBean(elementText, Boolean.class));
        }
        // Pre
        // self first
        if (extensionElementMap != null && extensionElementMap.containsKey(BpmnXmlConstants.PRE)) {
            ExtensionElement element = extensionElementMap.get(BpmnXmlConstants.PRE).get(0);
            String elementText = element.getElementText();
            node.put(DefConstants.NODE_PROP_PRE, JSONUtil.toBean(elementText, Map.class));
        }
        // Action
        // self first
        if (extensionElementMap != null && extensionElementMap.containsKey(BpmnXmlConstants.ACTION)) {
            ExtensionElement element = extensionElementMap.get(BpmnXmlConstants.ACTION).get(0);
            String elementText = element.getElementText();
            node.put(DefConstants.NODE_PROP_ACTION, JSONUtil.toBean(elementText, Map.class));
        }
    }

}

package com.xiaopeng.workflow.converter;

import cn.hutool.json.JSONUtil;
import com.xiaopeng.workflow.components.ParamTypeValuePair;
import com.xiaopeng.workflow.components.WorkFlowType;
import com.xiaopeng.workflow.components.XPComponentStep;
import com.xiaopeng.workflow.components.XPRepeatStep;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.model.bpmn.instance.FlowNode;
import org.camunda.bpm.model.bpmn.instance.StartEvent;

import java.util.ArrayList;
import java.util.List;

import static com.xiaopeng.workflow.converter.CamundaBpmnConverter.buildXPComponentStepBeta;
import static com.xiaopeng.workflow.util.FlowNodeUtil.getSingleExtensionPropertie;

@Slf4j
public class RepeatComponentStepConverter implements ComponentStepConverter {
    @Override
    public XPComponentStep convert(FlowNode subProcess) {
        //flowNode 为 SubProcess
        List<StartEvent> startEventList = subProcess.getChildElementsByType(StartEvent.class).stream().toList();
        if (startEventList.isEmpty()) {
            log.error("SubProcess {} has no start event", subProcess.getName());
            throw new RuntimeException("SubProcess has no start event");
        }
        List<FlowNode> realFlowNodeList = startEventList.get(0).getSucceedingNodes().list();

        String predicateClassName = getSingleExtensionPropertie(subProcess.getExtensionElements(), "predicateClassName");
        if (StringUtils.isEmpty(predicateClassName)) {
            log.warn("predicateClassName is empty");
            throw new RuntimeException("predicateClassName is empty");
        }
        XPComponentStep repeatComponentStep = XPComponentStep.builder().type(WorkFlowType.REPEAT).build();
        XPRepeatStep repeatStep = new XPRepeatStep();
        repeatStep.setPredicateClassName(predicateClassName);
        repeatStep.setPredicateParameterTypes(getPredicateParameterTypes(subProcess));
        repeatStep.setComponentStep(buildXPComponentStepBeta(realFlowNodeList.get(0)));
        repeatComponentStep.setRepeatStep(repeatStep);
        return repeatComponentStep;
    }

    private static List<ParamTypeValuePair> getPredicateParameterTypes(FlowNode flowNode) {
        String predicateParameterTypes = getSingleExtensionPropertie(flowNode.getExtensionElements(), "predicateParameterTypes");
        if (StringUtils.isNotEmpty(predicateParameterTypes)) {
            return JSONUtil.toList(predicateParameterTypes, ParamTypeValuePair.class);
        }
        return new ArrayList<>();
    }
}

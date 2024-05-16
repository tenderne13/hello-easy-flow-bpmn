package com.xiaopeng.workflow.converter;

import cn.hutool.json.JSONUtil;
import com.xiaopeng.workflow.components.ParamTypeValuePair;
import com.xiaopeng.workflow.components.WorkFlowType;
import com.xiaopeng.workflow.components.XPComponentStep;
import com.xiaopeng.workflow.components.XPConditionStep;
import com.xiaopeng.workflow.components.constants.FlowConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.model.bpmn.instance.ExclusiveGateway;
import org.camunda.bpm.model.bpmn.instance.FlowNode;
import org.camunda.bpm.model.bpmn.instance.SequenceFlow;

import java.util.ArrayList;
import java.util.List;

import static com.xiaopeng.workflow.converter.CamundaBpmnConverter.buildXPComponentStepBeta;
import static com.xiaopeng.workflow.util.FlowNodeUtil.getPredicateClassName;
import static com.xiaopeng.workflow.util.FlowNodeUtil.getSingleExtensionPropertie;

@Slf4j
public class ConditionalComponentStepConverter implements ComponentStepConverter {
    @Override
    public XPComponentStep convert(FlowNode flowNode) {
        //flowNode 为 StartEvent
        //排他网关
        FlowNode judgeNode = flowNode.getSucceedingNodes().list().get(0);
        log.info("judgeNode : {}", judgeNode.getId());
        if (!(judgeNode instanceof ExclusiveGateway)) {
            throw new RuntimeException("judgeNode is not Gateway");
        }
        ExclusiveGateway exclusiveGateway = (ExclusiveGateway) judgeNode;
        SequenceFlow defaultBranch = exclusiveGateway.getDefault();

        XPComponentStep conditionStep = XPComponentStep.builder().type(WorkFlowType.CONDITIONAL).build();
        List<XPConditionStep> conditionSteps = new ArrayList<>();
        judgeNode.getOutgoing().forEach(sequenceFlow -> {

            XPConditionStep xpConditionStep = new XPConditionStep();
            String predicateClassName = getPredicateClassName(sequenceFlow);
            xpConditionStep.setPredicateClassName(predicateClassName);
            xpConditionStep.setPredicateParameterTypes(getPredicateParameterTypes(sequenceFlow));
            if (defaultBranch == sequenceFlow) {
                xpConditionStep.setConditionStep(FlowConstants.OTHERWISE_STEP);
            }

            FlowNode targetNode = sequenceFlow.getTarget();
            log.info("targetNode : {}", targetNode.getId());
            XPComponentStep componentStep = buildXPComponentStepBeta(targetNode);
            xpConditionStep.setComponentStep(componentStep);
            conditionSteps.add(xpConditionStep);
        });
        conditionStep.setConditionSteps(conditionSteps);
        return conditionStep;
    }

    private static List<ParamTypeValuePair> getPredicateParameterTypes(SequenceFlow sequenceFlow) {
        String predicateParameterTypes = getSingleExtensionPropertie(sequenceFlow.getExtensionElements(), "predicateParameterTypes");
        if (StringUtils.isNotEmpty(predicateParameterTypes)) {
            return JSONUtil.toList(predicateParameterTypes, ParamTypeValuePair.class);
        }
        return new ArrayList<>();
    }
}

package com.xiaopeng.workflow.converter;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.resource.ClassPathResource;
import cn.hutool.json.JSONUtil;
import com.xiaopeng.workflow.components.WorkFlowType;
import com.xiaopeng.workflow.components.WorkFlowTypeConverter;
import com.xiaopeng.workflow.components.XPComponentStep;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.*;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperties;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * camunda bpmn -> XPComponentStep 转换器
 */
@Slf4j
public class CamundaBpmnConverter {


    public static void main(String[] args) {
        BpmnModelInstance bpmnModelInstance = Bpmn.readModelFromStream(new ClassPathResource("simple_paral.bpmn").getStream());

        Collection<Process> processList = bpmnModelInstance.getModelElementsByType(Process.class);

        for (Process process : processList) {
            System.out.println("process id: " + process.getId());
            System.out.println("process Name: " + process.getName());
            List<StartEvent> startEventList = process.getChildElementsByType(StartEvent.class).stream().toList();
            if (startEventList.isEmpty()) {
                log.error("process {} has no start event", process.getName());
                break;
            }
            XPComponentStep rootXPComponentStep = XPComponentStep.builder().type(WorkFlowType.SEQUENTIAL).name(process.getName()).sequentialSteps(new ArrayList<>()).build();
            List<FlowNode> flowNodes = startEventList.get(0).getSucceedingNodes().list();
            //todo 一个层级中只能有一个startEvent
            buildSequentialXPComponentSteps(flowNodes, rootXPComponentStep);
            log.info(JSONUtil.toJsonStr(rootXPComponentStep));
        }
    }


    /**
     * 最外层Process 为顺序流,主流程处理方法 该方法为一类处理方法
     * 二类处理方法 {buildXPComponentStepBeta} 为递归方法
     *
     * @param flowNodeList
     * @param xpComponentStep
     */
    private static void buildSequentialXPComponentSteps(List<FlowNode> flowNodeList, XPComponentStep xpComponentStep) {
        if (flowNodeList.size() > 1) {
            log.warn(" 主流程存在多个 出口，暂不支持 =====");
            throw new RuntimeException("主流程存在多个 出口，暂不支持");
        }
        FlowNode flowNode = flowNodeList.get(0);
        do {
            log.info("flowNode name : {}", flowNode.getName());
            XPComponentStep childXPComponentStep = buildXPComponentStepBeta(flowNode);
            xpComponentStep.getSequentialSteps().add(childXPComponentStep);
        } while (hasNext(flowNode) && (flowNode = flowNode.getSucceedingNodes().list().get(0)) != null);
    }

    private static boolean hasNext(FlowNode flowNode) {
        return flowNode.getSucceedingNodes().count() > 0;
    }


    private static XPComponentStep buildXPComponentStepBeta(FlowNode flowNode) {
        log.info("当前构建的类别 : {}", flowNode.getElementType().getTypeName());
        if (flowNode instanceof SubProcess) {
            WorkFlowType flowType = getStepType(flowNode);
            log.info("SubProcess flowType : {}", flowType);
            SubProcess subProcess = (SubProcess) flowNode;
            List<StartEvent> startEventList = subProcess.getChildElementsByType(StartEvent.class).stream().toList();
            if (startEventList.isEmpty()) {
                //todo 此处应该抛异常
                log.error("SubProcess {} has no start event", subProcess.getName());
                throw new RuntimeException("SubProcess has no start event");
            }
            List<FlowNode> realFlowNodeList = startEventList.get(0).getSucceedingNodes().list();
            switch (flowType) {
                case SEQUENTIAL:
                    FlowNode headNode = realFlowNodeList.get(0);
                    //将 realFlowNodeList 链表展开成list
                    List<FlowNode> flowNodeList = new ArrayList<>();
                    do {
                        flowNodeList.add(headNode);
                    } while (hasNext(headNode) && (headNode = headNode.getSucceedingNodes().list().get(0)) != null);
                    XPComponentStep sequentialStep = XPComponentStep.builder().type(WorkFlowType.SEQUENTIAL).sequentialSteps(new ArrayList<>()).build();
                    for (FlowNode node : flowNodeList) {
                        sequentialStep.getSequentialSteps().add(buildXPComponentStepBeta(node));
                    }
                    log.info("sequentialStep : {} build success", JSONUtil.toJsonStr(sequentialStep));
                    return sequentialStep;
                case PARALLEL:
                    XPComponentStep parallelStep = XPComponentStep.builder().type(WorkFlowType.PARALLEL).parallelSteps(new ArrayList<>()).build();
                    //并行节点默认 节点next没有节点
                    for (FlowNode node : realFlowNodeList) {
                        parallelStep.getParallelSteps().add(buildXPComponentStepBeta(node));
                    }
                    return parallelStep;
                case CONDITIONAL:
                    break;
                case REPEAT:
                    break;
                default:
                    break;
            }
        } else if (flowNode instanceof Task) {
            log.info("baseCase  ==> singleTask : {}", flowNode.getName());
            return XPComponentStep.builder().component(flowNode.getName()).build();
        } else {
            log.warn("该类别未做实现 ===> {}", flowNode.getElementType());
        }
        return null;
    }

    private static WorkFlowType getStepType(FlowNode flowNode) {
        ExtensionElements extensionElements = flowNode.getExtensionElements();
        if (extensionElements != null) {
            List<CamundaProperties> list = extensionElements.getElementsQuery().filterByType(CamundaProperties.class).list();
            if (CollectionUtil.isNotEmpty(list)) {
                CamundaProperties camundaProperties = list.get(0);
                Optional<CamundaProperty> propertyOptional = camundaProperties.getCamundaProperties().stream().filter(item -> "type".equals(item.getCamundaName())).findFirst();
                if (propertyOptional.isPresent()) {
                    CamundaProperty property = propertyOptional.get();
                    String camundaValue = property.getCamundaValue();
                    WorkFlowTypeConverter converter = new WorkFlowTypeConverter();
                    WorkFlowType workFlowType = converter.convert(camundaValue, WorkFlowType.SEQUENTIAL);
                    return workFlowType;
                }
            }
        }
        return WorkFlowType.SEQUENTIAL;
    }
}

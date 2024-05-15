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
import org.camunda.bpm.model.bpmn.Query;
import org.camunda.bpm.model.bpmn.impl.instance.TaskImpl;
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

    private static XPComponentStep buildXPComponentStep(FlowNode flowNode, XPComponentStep xpComponentStep) {
        WorkFlowType rootType = xpComponentStep.getType();

        log.info("当前构建的类别 : {}", rootType);

        if (flowNode instanceof SubProcess) {


            SubProcess subProcess = (SubProcess) flowNode;
            WorkFlowType flowType = getStepType(subProcess);

            //获取子流程总类型 默认顺序流
            XPComponentStep subProcessXPComponentStep = new XPComponentStep();
            subProcessXPComponentStep.setType(flowType);
            subProcessXPComponentStep.setName(subProcess.getName());

            //todo 获取子流程的节点的类型  可能为 repeat conditional sequential parallel

            List<StartEvent> startEventList = subProcess.getChildElementsByType(StartEvent.class).stream().toList();
            if (startEventList.isEmpty()) {
                //todo 此处应该抛异常
                log.error("SubProcess {} has no start event", subProcess.getName());
            }
            buildXPComponentStep(startEventList.get(0), subProcessXPComponentStep);

            switch (rootType) {
                case SEQUENTIAL:
                    xpComponentStep.getSequentialSteps().add(subProcessXPComponentStep);
                    break;
                case PARALLEL:
                    xpComponentStep.getParallelSteps().add(subProcessXPComponentStep);
                    break;
                case CONDITIONAL:
                    break;
                case REPEAT:
                    break;
                default:
                    break;
            }
        } else if (flowNode instanceof StartEvent) {
            XPComponentStep componentStep = new XPComponentStep();
            StartEvent startEvent = (StartEvent) flowNode;
            List<FlowNode> flowNodes = startEvent.getSucceedingNodes().list();
            if (flowNodes.size() > 1) {
                componentStep.setType(WorkFlowType.PARALLEL);
                List<XPComponentStep> parallelSteps = new ArrayList<>();
                componentStep.setParallelSteps(parallelSteps);
                for (FlowNode node : flowNodes) {
                    buildXPComponentStep(node, componentStep);
                }
            }

            switch (rootType) {
                case SEQUENTIAL:
                    xpComponentStep.getSequentialSteps().add(componentStep);
                    break;
                case PARALLEL:
                    xpComponentStep.getParallelSteps().add(componentStep);
                    break;
                case CONDITIONAL:
                    break;
                case REPEAT:
                    break;
                default:
                    break;
            }


        } else if (flowNode instanceof Task) {
            Task task = (Task) flowNode;
            XPComponentStep taskXPComponentStep = XPComponentStep.builder().type(WorkFlowType.SINGLE).name(task.getName()).build();
            switch (rootType) {
                case SEQUENTIAL:
                    xpComponentStep.getSequentialSteps().add(taskXPComponentStep);
                    break;
                case PARALLEL:
                    xpComponentStep.getParallelSteps().add(taskXPComponentStep);
                    break;
                case CONDITIONAL:
                    break;
                case REPEAT:
                    break;
                default:
                    break;
            }
            List<FlowNode> flowNodes = task.getSucceedingNodes().list();
            if (flowNodes == null || flowNodes.size() == 0) {
                log.info("task {} has no succeeding nodes", task.getName());
            } else {
                for (FlowNode node : flowNodes) {
                    buildXPComponentStep(node, taskXPComponentStep);
                }
            }
        } else {
            log.warn("未做实现");

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


    public static void buildXPParallelComponentStep(FlowNode flowNode, XPComponentStep xpComponentStep) {
        List<XPComponentStep> parallelSteps = xpComponentStep.getParallelSteps();
        if (flowNode instanceof SubProcess) {
            SubProcess subProcess = (SubProcess) flowNode;
            //todo 获取子流程的节点的类型  可能为 repeat conditional sequential parallel
            XPComponentStep rootXPComponentStep = XPComponentStep.builder().type(WorkFlowType.SEQUENTIAL).name(subProcess.getName()).build();
            List<StartEvent> startEventList = subProcess.getChildElementsByType(StartEvent.class).stream().toList();
            if (startEventList.isEmpty()) {
                //todo 此处应该抛异常
                log.error("SubProcess {} has no start event", subProcess.getName());
            }
            //buildXPComponentStep(startEventList, rootXPComponentStep);
        }
    }

    public static void trace(FlowNode flowNode) {

        String nodeType = getNodeType(flowNode);

        System.out.println("nodeType: " + nodeType + ",\n nodeName:" + flowNode.getName());
        Query<FlowNode> succeedingNodes = flowNode.getSucceedingNodes();
        if (succeedingNodes.count() > 0) {
            List<FlowNode> flowNodeList = succeedingNodes.list();
            for (FlowNode node : flowNodeList) {
                trace(node);
            }
        }
    }

    private static String getNodeType(FlowNode flowNode) {
        if (flowNode instanceof ParallelGateway) {
            System.out.println("=== ParallelGateway === > 后继节点个数：" + flowNode.getSucceedingNodes().count());
            //匹配成对的ParallelGateway  出口 顺序流公共焦点
            flowNode.getSucceedingNodes().list().forEach(item -> {
                System.out.println("id :" + item.getId());
            });

            return ParallelGateway.class.getTypeName();
        } else if (flowNode instanceof TaskImpl) {
            return TaskImpl.class.getTypeName();
        } else if (flowNode instanceof StartEvent) {
            return StartEvent.class.getTypeName();
        }
        return "unknow";
    }
}

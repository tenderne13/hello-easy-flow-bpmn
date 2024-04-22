package com.xiaopeng.workflow.converter;

import cn.hutool.core.io.resource.ClassPathResource;
import cn.hutool.json.JSONUtil;
import com.xiaopeng.workflow.components.XPComponentStep;
import com.xiaopeng.workflow.components.enums.ComponentType;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.Query;
import org.camunda.bpm.model.bpmn.impl.instance.TaskImpl;
import org.camunda.bpm.model.bpmn.instance.*;
import org.camunda.bpm.model.bpmn.instance.Process;


import java.util.*;

/**
 * camunda bpmn -> XPComponentStep 转换器
 */
@Slf4j
public class CamundaBpmnConverter {


    public static void main(String[] args) {
        BpmnModelInstance bpmnModelInstance = Bpmn.readModelFromStream(new ClassPathResource("simple_paral.bpmn").getStream());

        Collection<Process> processList = bpmnModelInstance.getModelElementsByType(Process.class);

        String processName = "";
        for (Process process : processList) {
            System.out.println("process id: " + process.getId());
            System.out.println("process Name: " + process.getName());
            List<StartEvent> startEventList = process.getChildElementsByType(StartEvent.class).stream().toList();
            if (startEventList.isEmpty()) {
                log.error("process {} has no start event", process.getName());
                break;
            }
            processName = process.getName();
            XPComponentStep rootXPComponentStep = new XPComponentStep();
            rootXPComponentStep.setType(ComponentType.SEQUENTIAL.getCode());
            rootXPComponentStep.setName(process.getName());
            //todo 一个层级中只能有一个startEvent
            buildXPComponentStep(startEventList.get(0), rootXPComponentStep);
            log.info(JSONUtil.toJsonStr(rootXPComponentStep));
        }
    }


    private static void buildXPComponentStep(FlowNode flowNode, XPComponentStep xpComponentStep) {
        String rootType = xpComponentStep.getType();
        log.info("当前构建的类别 : {}", rootType);

        if (flowNode instanceof SubProcess) {

            SubProcess subProcess = (SubProcess) flowNode;
            //获取子流程总类型 默认顺序流
            String type = "sequential";
            XPComponentStep subProcessXPComponentStep = new XPComponentStep();
            subProcessXPComponentStep.setType(ComponentType.SEQUENTIAL.getCode());
            subProcessXPComponentStep.setName(subProcess.getName());

            //todo 获取子流程的节点的类型  可能为 repeat conditional sequential parallel

            List<StartEvent> startEventList = subProcess.getChildElementsByType(StartEvent.class).stream().toList();
            if (startEventList.isEmpty()) {
                //todo 此处应该抛异常
                log.error("SubProcess {} has no start event", subProcess.getName());
            }
            buildXPComponentStep(startEventList.get(0), subProcessXPComponentStep);
            if (ComponentType.SEQUENTIAL.getCode().equals(rootType)) {
                xpComponentStep.getSequentialSteps().add(subProcessXPComponentStep);
            } else if (ComponentType.PARALLEL.getCode().equals(rootType)) {
                xpComponentStep.getParallelSteps().add(subProcessXPComponentStep);
            } else if (ComponentType.CONDITIONAL.getCode().equals(rootType)) {

            } else if (ComponentType.REPEAT.getCode().equals(rootType)) {

            }
        } else if (flowNode instanceof StartEvent) {
            XPComponentStep componentStep = new XPComponentStep();
            StartEvent startEvent = (StartEvent) flowNode;
            List<FlowNode> flowNodes = startEvent.getSucceedingNodes().list();
            if (flowNodes.size() > 1) {
                componentStep.setType(ComponentType.PARALLEL.getCode());
                List<XPComponentStep> parallelSteps = new ArrayList<>();
                componentStep.setParallelSteps(parallelSteps);
                for (FlowNode node : flowNodes) {
                    buildXPComponentStep(node, componentStep);
                }
            }

            if (ComponentType.SEQUENTIAL.getCode().equals(rootType)) {
                xpComponentStep.getSequentialSteps().add(componentStep);
            } else if (ComponentType.PARALLEL.getCode().equals(rootType)) {
                xpComponentStep.getParallelSteps().add(componentStep);
            } else if (ComponentType.CONDITIONAL.getCode().equals(rootType)) {

            } else if (ComponentType.REPEAT.getCode().equals(rootType)) {

            }


        } else if (flowNode instanceof Task) {
            Task task = (Task) flowNode;
            XPComponentStep taskXPComponentStep = XPComponentStep.builder().type(ComponentType.SINGLE.getCode()).name(task.getName()).build();
            if (ComponentType.SEQUENTIAL.getCode().equals(rootType)) {
                xpComponentStep.getSequentialSteps().add(taskXPComponentStep);
            } else if (ComponentType.PARALLEL.getCode().equals(rootType)) {
                xpComponentStep.getParallelSteps().add(taskXPComponentStep);
            } else if (ComponentType.CONDITIONAL.getCode().equals(rootType)) {

            } else if (ComponentType.REPEAT.getCode().equals(rootType)) {

            }
        } else {
            log.warn("未做实现");

        }
    }

    /**
     * 从流程跟节点开始构建
     * startEvent
     *
     * @param flowNodeList
     * @param xpComponentStep
     */
    private static void buildXPComponentStepV1(List<? extends FlowNode> flowNodeList, XPComponentStep
            xpComponentStep) {
        String rootType = xpComponentStep.getType();
        log.info("当前构建的类别 : {}", rootType);
        switch (rootType) {
            case "sequential":


//                for (FlowNode flowNode : flowNodeList) {
//                    buildXPComponentStep(flowNode, xpComponentStep);
//                }
                break;
            case "parallel":
                XPComponentStep parallelXPComponentStep = XPComponentStep.builder().type(ComponentType.PARALLEL.getCode()).name("parallel").build();
                List<XPComponentStep> parallelSteps = new ArrayList<>();
                parallelXPComponentStep.setParallelSteps(parallelSteps);
                for (FlowNode flowNode : flowNodeList) {
                    buildXPComponentStep(flowNode, parallelXPComponentStep);
                }
                break;
            case "CONDITIONAL":
                break;
            case "REPEAT":
                break;
            case "SINGLE":
                break;
            default:
                log.warn("未做实现");
        }

//        if (flowNode instanceof StartEvent) {
//            StartEvent startEvent = (StartEvent) flowNode;
//            List<FlowNode> flowNodes = startEvent.getSucceedingNodes().list();
//            if (flowNodes.size() > 1) {
//                XPComponentStep parallelXPComponentStep = XPComponentStep.builder().type(ComponentType.PARALLEL.getCode()).name("parallel").build();
//                List<XPComponentStep> parallelSteps = new ArrayList<>();
//                parallelXPComponentStep.setParallelSteps(parallelSteps);
//                for (FlowNode node : flowNodes) {
//                    buildXPComponentStep(node, parallelXPComponentStep);
//                }
//            } else {
//                for (FlowNode node : flowNodes) {
//                    buildXPComponentStep(node, xpComponentStep);
//                }
//            }
//        } else if (flowNode instanceof SubProcess) {
//
//        } else if (flowNode instanceof Task) {
//            String type = xpComponentStep.getType();
//            Task task = (Task) flowNode;
//            XPComponentStep taskXPComponentStep = XPComponentStep.builder().type(ComponentType.SINGLE.getCode()).name(task.getName()).build();
//            if(ComponentType.valueOf(type).equals(ComponentType.PARALLEL)){
//                xpComponentStep.getParallelSteps().add(taskXPComponentStep);
//            }else if(ComponentType.valueOf(type).equals(ComponentType.SEQUENTIAL)){
//                xpComponentStep.getSequentialSteps().add(taskXPComponentStep);
//            }else if(ComponentType.valueOf(type).equals(ComponentType.CONDITIONAL)) {
//
//            }
//        }else{
//            log.warn("未做实现");
//        }
    }

    public static void buildXPParallelComponentStep(FlowNode flowNode, XPComponentStep xpComponentStep) {
        List<XPComponentStep> parallelSteps = xpComponentStep.getParallelSteps();
        if (flowNode instanceof SubProcess) {
            SubProcess subProcess = (SubProcess) flowNode;
            //todo 获取子流程的节点的类型  可能为 repeat conditional sequential parallel
            XPComponentStep rootXPComponentStep = XPComponentStep.builder().type(ComponentType.SEQUENTIAL.getCode()).name(subProcess.getName()).build();
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

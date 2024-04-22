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
import org.camunda.bpm.model.xml.Model;
import org.camunda.bpm.model.xml.ModelInstance;

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
        BpmnModelInstance bpmnModelInstance = Bpmn.readModelFromStream(new ClassPathResource("simple_sequential.bpmn").getStream());

        Collection<StartEvent> startEvents = bpmnModelInstance.getModelElementsByType(StartEvent.class);
        Collection<Process> processList = bpmnModelInstance.getModelElementsByType(Process.class);

        String processName = "";
        for (Process process : processList) {
            System.out.println("process id: " + process.getId());
            System.out.println("process Name: " + process.getName());
            ModelInstance modelInstance = process.getModelInstance();
            Optional<StartEvent> eventOptional = process.getChildElementsByType(StartEvent.class).stream().findFirst();
            if (eventOptional.isEmpty()) {
                log.error("process {} has no start event", process.getName());
                break;
            }
            StartEvent startEvent = eventOptional.get();
            processName = process.getName();
            XPComponentStep rootXPComponentStep = XPComponentStep.builder().type(ComponentType.SEQUENTIAL.getCode()).name(processName).build();
            buildXPComponentStep(startEvent, rootXPComponentStep);
        }

        Definitions definitions = bpmnModelInstance.getDefinitions();
        ModelInstance modelInstance = definitions.getModelInstance();


        XPComponentStep rootXPComponentStep = XPComponentStep.builder().type(ComponentType.SEQUENTIAL.getCode()).name(processName).build();
        //buildXPComponentStep(modelInstance, rootXPComponentStep);
        //从StartEvent 开始构建


        System.out.println(JSONUtil.toJsonStr(rootXPComponentStep));
        for (StartEvent startEvent : startEvents) {
            trace(startEvent);
        }
    }

    /**
     * 从流程跟节点开始构建
     * startEvent
     *
     * @param flowNode
     * @param xpComponentStep
     */
    private static void buildXPComponentStep(FlowNode flowNode, XPComponentStep xpComponentStep) {
        if (flowNode instanceof StartEvent) {
            StartEvent startEvent = (StartEvent) flowNode;
            List<FlowNode> flowNodes = startEvent.getSucceedingNodes().list();
            if (flowNodes.size() > 1) {
                XPComponentStep parallelXPComponentStep = XPComponentStep.builder().type(ComponentType.PARALLEL.getCode()).name("parallel").build();
                List<XPComponentStep> parallelSteps = new ArrayList<>();
                parallelXPComponentStep.setParallelSteps(parallelSteps);
                for (FlowNode node : flowNodes) {
                    buildXPComponentStep(node, parallelXPComponentStep);
                }
            } else {
                for (FlowNode node : flowNodes) {
                    buildXPComponentStep(node, xpComponentStep);
                }
            }
        } else if (flowNode instanceof SubProcess) {

        }
    }

    public static void buildXPParallelComponentStep(FlowNode flowNode, XPComponentStep xpComponentStep) {
        List<XPComponentStep> parallelSteps = xpComponentStep.getParallelSteps();
        if (flowNode instanceof SubProcess) {
            SubProcess subProcess = (SubProcess) flowNode;
            //todo 获取子流程的节点的类型  可能为 repeat conditional sequential parallel
            XPComponentStep rootXPComponentStep = XPComponentStep.builder().type(ComponentType.SEQUENTIAL.getCode()).name(subProcess.getName()).build();
            Optional<StartEvent> eventOptional = subProcess.getChildElementsByType(StartEvent.class).stream().findFirst();
            if (eventOptional.isEmpty()) {
                //todo 此处应该抛异常
                log.error("SubProcess {} has no start event", subProcess.getName());
            }
            StartEvent startEvent = eventOptional.get();
            buildXPComponentStep(startEvent, rootXPComponentStep);
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

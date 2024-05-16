package com.xiaopeng.workflow.converter;

import cn.hutool.json.JSONUtil;
import com.xiaopeng.workflow.components.WorkFlowType;
import com.xiaopeng.workflow.components.WorkFlowTypeConverter;
import com.xiaopeng.workflow.components.XPComponentStep;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.*;
import org.springframework.util.CollectionUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.xiaopeng.workflow.util.FlowNodeUtil.getSingleExtensionPropertie;
import static com.xiaopeng.workflow.util.FlowNodeUtil.hasNext;

/**
 * camunda bpmn -> XPComponentStep 转换器
 */
@Slf4j
public class CamundaBpmnConverter {


    public static final SequentialComponentStepConverter SEQUENTIAL_COMPONENT_STEP_CONVERTER = new SequentialComponentStepConverter();
    public static final ParallelComponentStepConverter PARALLEL_COMPONENT_STEP_CONVERTER = new ParallelComponentStepConverter();
    public static final ConditionalComponentStepConverter CONDITIONAL_COMPONENT_STEP_CONVERTER = new ConditionalComponentStepConverter();
    public static final RepeatComponentStepConverter REPEAT_COMPONENT_STEP_CONVERTER = new RepeatComponentStepConverter();


    /**
     * bpmnXml 转 XPComponentStep
     *
     * @param bpmnXml
     * @return
     */
    public static XPComponentStep convert(String bpmnXml) {
        BpmnModelInstance bpmnModelInstance = Bpmn.readModelFromStream(new ByteArrayInputStream(bpmnXml.getBytes(StandardCharsets.UTF_8)));
        return convert(bpmnModelInstance);
    }

    public static XPComponentStep convert(InputStream inputStream) {
        return convert(Bpmn.readModelFromStream(inputStream));
    }

    public static XPComponentStep convert(BpmnModelInstance bpmnModelInstance) {
        Collection<Process> processList = bpmnModelInstance.getModelElementsByType(Process.class);
        if (CollectionUtils.isEmpty(processList)) {
            log.error("bpmnModelInstance has no process");
            throw new RuntimeException("bpmnModelInstance has no process");
        }
        Process process = processList.iterator().next();
        List<StartEvent> startEventList = process.getChildElementsByType(StartEvent.class).stream().toList();
        if (startEventList.isEmpty()) {
            log.error("process {} has no start event", process.getName());
            throw new RuntimeException("process has no start event");
        }
        //最外层Process 为顺序流
        XPComponentStep rootXPComponentStep = XPComponentStep.builder().type(WorkFlowType.SEQUENTIAL).name(process.getName()).sequentialSteps(new ArrayList<>()).build();
        List<FlowNode> flowNodes = startEventList.get(0).getSucceedingNodes().list();
        //一个层级中只能有一个startEvent
        buildSequentialXPComponentSteps(flowNodes, rootXPComponentStep);
        log.info("final convert result => {}", JSONUtil.toJsonStr(rootXPComponentStep));
        return rootXPComponentStep;
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


    public static XPComponentStep buildXPComponentStepBeta(FlowNode flowNode) {
        log.info("当前构建的类别 : {}", flowNode.getElementType().getTypeName());
        if (flowNode instanceof SubProcess) {
            WorkFlowType flowType = getStepType(flowNode);
            log.info("SubProcess flowType : {}", flowType);
            List<StartEvent> startEventList = flowNode.getChildElementsByType(StartEvent.class).stream().toList();
            if (startEventList.isEmpty()) {
                log.error("SubProcess {} has no start event", flowNode.getName());
                throw new RuntimeException("SubProcess has no start event");
            }
            switch (flowType) {
                case SEQUENTIAL:
                    return SEQUENTIAL_COMPONENT_STEP_CONVERTER.convert(startEventList.get(0));
                case PARALLEL:
                    return PARALLEL_COMPONENT_STEP_CONVERTER.convert(startEventList.get(0));
                case CONDITIONAL:
                    return CONDITIONAL_COMPONENT_STEP_CONVERTER.convert(startEventList.get(0));
                case REPEAT:
                    return REPEAT_COMPONENT_STEP_CONVERTER.convert(flowNode);
                default:
                    log.warn("SubProcess 未做实现 ===> {}", flowType);
                    throw new RuntimeException("SubProcess 未做实现 ===> " + flowType);
            }
        } else if (flowNode instanceof Task) {
            log.info("baseCase  ==> singleTask : {}", flowNode.getName());
            return XPComponentStep.builder().component(flowNode.getName()).build();
        } else {
            log.warn("该类别未做实现 ===> {}", flowNode.getElementType());
            throw new RuntimeException("该类别未做实现 ===> " + flowNode.getElementType());
        }
    }

    private static WorkFlowType getStepType(FlowNode flowNode) {
        ExtensionElements extensionElements = flowNode.getExtensionElements();
        String typeValue = getSingleExtensionPropertie(extensionElements, "type");
        WorkFlowTypeConverter converter = new WorkFlowTypeConverter();
        return converter.convert(typeValue, WorkFlowType.SEQUENTIAL);
    }


}

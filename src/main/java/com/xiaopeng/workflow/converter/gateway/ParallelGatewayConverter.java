package com.xiaopeng.workflow.converter.gateway;

import cn.hutool.json.JSONUtil;
import com.xiaopeng.workflow.converter.BaseFlowNodeConverter;
import com.xiaopeng.workflow.converter.constant.DefConstants;
import com.xiaopeng.workflow.util.ConvertUtil;
import lombok.extern.slf4j.Slf4j;
import org.activiti.bpmn.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Parallel Gateway Converter.
 *
 * @author liyuliang5
 */
@Slf4j
public class ParallelGatewayConverter extends BaseFlowNodeConverter {

    @Override
    public Map<String, Object> convert(FlowNode flowNode, BpmnModel bpmnModel, Map<String, Object> flowDef) {
        ParallelGateway parallelGateway = (ParallelGateway) flowNode;
        Map<String, Object> node = super.convert(flowNode, bpmnModel, flowDef);
        Map<String, Object> post = ConvertUtil.getMapValue(node, DefConstants.NODE_PROP_POST);
        post.put(DefConstants.NODE_POST_PROP_CONDITION_TYPE, "parallel");
        // Set pre nodes.
        List<SequenceFlow> incomingFlows = parallelGateway.getIncomingFlows();
        if (incomingFlows.size() == 0) {
            throw new RuntimeException("Parallel gateway:" + flowNode.getId() + " no incomming flows");
        }

        Map<String, List<ExtensionElement>> extensionElements = flowNode.getExtensionElements();

        List<ExtensionElement> properties = extensionElements.get("properties");
        if(properties != null){
            for (ExtensionElement property : properties) {
                String elementText1 = property.getElementText();
                log.info("elementText1:{}", elementText1);
//            Map<String, List<ExtensionElement>> childElements = property.getChildElements();
//            String elementText = property.getElementText();
//            Map<String, Object> map = JSONUtil.toBean(elementText, Map.class);
//            node.put("properties", map);
            }
        }

        // 大于1个入口 说明是join节点 等于1个入口说明是fork节点
        if (incomingFlows.size() > 1) {
            if (node.get(DefConstants.NODE_PROP_PRE) == null) {
                Map<String, Object> pre = ConvertUtil.getMapValue(node, DefConstants.NODE_PROP_PRE);
                List<String> preNodes = new ArrayList<>();
                flowNode.getIncomingFlows().forEach(incomingFlow -> preNodes.add(incomingFlow.getSourceRef()));
                pre.put("preNodes", preNodes);
            }
            node.put("parallelType", "join");
            //记录网关所有的incoming flows （递归获取) 用于后续判断 fork 与 join是否成对出现
            List<String> incomingFlowIds = new ArrayList<>();

            List<String> visitedIds = new ArrayList<>();
            getAllIncomingFlowIds(bpmnModel, incomingFlows, visitedIds, incomingFlowIds);

            log.info("incomingFlowIds:{}", JSONUtil.toJsonStr(incomingFlowIds));
            // 所有可能到达该并行网关的连线id
            node.put("incomingFlowIds", incomingFlowIds);
        } else {
            List<SequenceFlow> outgoingFlows = parallelGateway.getOutgoingFlows();
            List<String> outgoingFlowIds = outgoingFlows.stream().map(SequenceFlow::getTargetRef).collect(Collectors.toList());
            node.put("outgoingFlowIds", outgoingFlowIds);
            node.put("parallelType", "fork");
        }

        return node;
    }

    private static void getAllIncomingFlowIds(BpmnModel bpmnModel, List<SequenceFlow> incomingFlows, List<String> visitedIds, List<String> incomingFlowIds) {
        for (SequenceFlow incomingFlow : incomingFlows) {
            String incomingFlowId = incomingFlow.getId();
            if (visitedIds.contains(incomingFlowId)) {
                log.info("incomingFlowId:{} has been visited", incomingFlowId);
                continue;
            }
            incomingFlowIds.add(incomingFlowId);
            String sourceRef = incomingFlow.getSourceRef();
            visitedIds.add(sourceRef);
            FlowNode preNode = (FlowNode) bpmnModel.getFlowElement(sourceRef);
            getAllIncomingFlowIds(bpmnModel, preNode.getIncomingFlows(), visitedIds, incomingFlowIds);
        }
    }

    private void recordIncomingFlowIds(ParallelGateway parallelGateway, List<String> incomingFlowIds) {
        List<SequenceFlow> incomingFlows = parallelGateway.getIncomingFlows();
    }

}

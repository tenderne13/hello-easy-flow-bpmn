package com.xiaopeng.workflow.converter.gateway;

import com.xiaopeng.workflow.converter.BaseFlowNodeConverter;
import com.xiaopeng.workflow.converter.constant.DefConstants;
import com.xiaopeng.workflow.util.ConvertUtil;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowNode;
import org.activiti.bpmn.model.ParallelGateway;
import org.activiti.bpmn.model.SequenceFlow;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Parallel Gateway Converter.
 *
 * @author liyuliang5
 */
public class ParallelGatewayConverter extends BaseFlowNodeConverter {

    @Override
    public Map<String, Object> convert(FlowNode flowNode, BpmnModel bpmnModel, Map<String, Object> flowDef) {
        ParallelGateway parallelGateway = (ParallelGateway) flowNode;
        Map<String, Object> node = super.convert(flowNode, bpmnModel, flowDef);
        Map<String, Object> post = ConvertUtil.getMapValue(node, DefConstants.NODE_PROP_POST);
        post.put(DefConstants.NODE_POST_PROP_CONDITION_TYPE, "parallel");
        // Set pre nodes.
        List<SequenceFlow> list = parallelGateway.getIncomingFlows();
        if (list.size() == 0) {
            throw new RuntimeException("Parallel gateway:" + flowNode.getId() + " no incomming flows");
        }
        // 大于1个入口 说明是join节点 等于1个入口说明是fork节点
        if (list.size() > 1) {
            if (node.get(DefConstants.NODE_PROP_PRE) == null) {
                Map<String, Object> pre = ConvertUtil.getMapValue(node, DefConstants.NODE_PROP_PRE);
                List<String> preNodes = new ArrayList<>();
                flowNode.getIncomingFlows().forEach(incomingFlow -> preNodes.add(incomingFlow.getSourceRef()));
                pre.put("preNodes", preNodes);
            }
        }else {

        }
        node.put("type", "并行网关");
        return node;
    }

}

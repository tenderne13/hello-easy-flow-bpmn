package com.xiaopeng.workflow.converter;

import cn.hutool.json.JSONUtil;
import com.xiaopeng.workflow.converter.constant.BpmnXmlConstants;
import com.xiaopeng.workflow.converter.constant.DefConstants;
import com.xiaopeng.workflow.converter.event.StartEventConverter;
import com.xiaopeng.workflow.converter.gateway.ParallelGatewayConverter;
import lombok.extern.slf4j.Slf4j;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.*;
import org.activiti.bpmn.model.Process;
import org.apache.commons.lang3.StringUtils;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * bpmn 转换器
 */
@Slf4j
public class BpmnConverter {

    // 转换器map
    private static Map<Class, FlowNodeConverter> flowNodeConverterMap = new HashMap<>();


    static {
        flowNodeConverterMap.put(StartEvent.class, new StartEventConverter());
        flowNodeConverterMap.put(ParallelGateway.class, new ParallelGatewayConverter());
    }

    public static void main(String[] args) {

    }

    public static String convert(String bpmnXmlData) {
        List<Map<String, Object>> model;
        try {
            model = convert(new ByteArrayInputStream(bpmnXmlData.getBytes("UTF-8")));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return JSONUtil.toJsonStr(model.size() == 1 ? model.get(0) : model);
    }

    public static List<Map<String, Object>> convert(InputStream inputStream) {
        // Get xml data.
        XMLStreamReader reader = null;
        try {
            XMLInputFactory xmlInputFactory = XMLInputFactory.newFactory();
            xmlInputFactory.setProperty("javax.xml.stream.isCoalescing", true);
            reader = xmlInputFactory.createXMLStreamReader(inputStream);
        } catch (XMLStreamException | FactoryConfigurationError e) {
            throw new RuntimeException("BPMN解析异常", e);
        }
        // Convert to java BPMN model leveraging the activiti ability.
        BpmnXMLConverter bpmnXmlConverter = new BpmnXMLConverter();
        BpmnModel bpmnModel = bpmnXmlConverter.convertToBpmnModel(reader);
        // Convert java BPMN model to EasyFlow model.
        List<Map<String, Object>> flowDefList = new ArrayList<>();
        for (Process process : bpmnModel.getProcesses()) {
            Map<String, Object> flowDef = new HashMap<String, Object>();
            convertProcess(process, bpmnModel, flowDef);
            flowDefList.add(flowDef);
        }

        return flowDefList;
    }


    private static void convertProcess(Process process, BpmnModel bpmnModel, Map<String, Object> flowDef) {
        // ID、name and properties
        String processId = process.getId();
        String processName = process.getName();
        flowDef.put(DefConstants.COMMON_PROP_ID, processId);
        if (StringUtils.isNotEmpty(processName)) {
            flowDef.put(DefConstants.COMMON_PROP_NAME, processName);
        }
        Map<String, List<ExtensionElement>> extensionElementMap = process.getExtensionElements();

        // flow pre handler
        if (extensionElementMap != null && extensionElementMap.containsKey(BpmnXmlConstants.PRE)) {
            ExtensionElement element = extensionElementMap.get(BpmnXmlConstants.PRE).get(0);
            String elementText = element.getElementText();
            Object preHandlerDef = JSONUtil.toBean(elementText, Map.class);
            flowDef.put(DefConstants.FLOW_PROP_PRE, preHandlerDef);
        }

        // Process flow element，convert Gateway、Event、Activity to Node.
        List<Map<String, Object>> nodeList = new ArrayList<>();
        flowDef.put(DefConstants.FLOW_PROP_NODES, nodeList);
        for (FlowElement flowElement : process.getFlowElements()) {
            // Flow element
            if (flowElement instanceof FlowNode) {
                FlowNodeConverter nodeConverter = flowNodeConverterMap.get(flowElement.getClass());
                if (nodeConverter == null) {
                    log.info("Unsupported BPMN element:ID" + flowElement.getId() + " TYPE:" + flowElement.getClass().getCanonicalName());
                    continue;
                    //throw new RuntimeException("Unsupported BPMN elmenet:ID" + flowElement.getId() + " TYPE:"
                    //      + flowElement.getClass().getCanonicalName());
                }
                Map<String, Object> node = nodeConverter.convert((FlowNode) flowElement, bpmnModel, flowDef);
                nodeList.add(node);
                // Sequence flow
            } else if (flowElement instanceof SequenceFlow) {
                continue;
            } else if (flowElement instanceof DataStoreReference || flowElement instanceof DataObject) {
                continue;
            } else {
                throw new RuntimeException(
                        "Unsupported BPMN element:ID" + flowElement.getId() + " TYPE:" + flowElement.getClass().getCanonicalName());
            }
        }
        flowNodeListPostProcess(nodeList);
        //最外层 getExtensionElements 属性处理
        proceesPostProcess(flowDef, extensionElementMap);
    }

    /**
     * 流程各个节点后处理
     * 处理并行网关等数据
     *
     * @param nodeList
     */
    private static void flowNodeListPostProcess(List<Map<String, Object>> nodeList) {

    }

    private static void proceesPostProcess(Map<String, Object> flowDef, Map<String, List<ExtensionElement>> extensionElementMap) {
        // flow post handler
        if (extensionElementMap != null && extensionElementMap.containsKey(BpmnXmlConstants.POST)) {
            ExtensionElement element = extensionElementMap.get(BpmnXmlConstants.POST).get(0);
            String elementText = element.getElementText();
            Object postHandlerDef = JSONUtil.toBean(elementText, Map.class);
            flowDef.put(DefConstants.FLOW_PROP_POST, postHandlerDef);
        }

        // properties
        if (extensionElementMap != null && extensionElementMap.containsKey(BpmnXmlConstants.PROPERTIES)) {
            ExtensionElement element = extensionElementMap.get(BpmnXmlConstants.PROPERTIES).get(0);
            String elementText = element.getElementText();
            Map<String, Object> map = JSONUtil.toBean(elementText, Map.class);
            flowDef.put(DefConstants.COMMON_PROP_PROPERTIES, map);
        }
        // listeners
        if (extensionElementMap != null && extensionElementMap.containsKey(BpmnXmlConstants.LISTENERS)) {
            ExtensionElement element = extensionElementMap.get(BpmnXmlConstants.LISTENERS).get(0);
            String elementText = element.getElementText();
            List<Object> list = JSONUtil.toBean(elementText, List.class);
            flowDef.put(DefConstants.FLOW_PROP_LISTENERS, list);
        }
        // filters
        if (extensionElementMap != null && extensionElementMap.containsKey(BpmnXmlConstants.FILTERS)) {
            ExtensionElement element = extensionElementMap.get(BpmnXmlConstants.FILTERS).get(0);
            String elementText = element.getElementText();
            List<Object> list = JSONUtil.toBean(elementText, List.class);
            flowDef.put(DefConstants.FLOW_PROP_FILTERS, list);
        }
        // nodeFilters
        if (extensionElementMap != null && extensionElementMap.containsKey(BpmnXmlConstants.NODE_FILTERS)) {
            ExtensionElement element = extensionElementMap.get(BpmnXmlConstants.NODE_FILTERS).get(0);
            String elementText = element.getElementText();
            List<Object> list = JSONUtil.toBean(elementText, List.class);
            flowDef.put(DefConstants.FLOW_PROP_NODE_FILTERS, list);
        }
        // nodePreHandlerFilters
        if (extensionElementMap != null && extensionElementMap.containsKey(BpmnXmlConstants.NODE_PRE_HANDLER_FILTERS)) {
            ExtensionElement element = extensionElementMap.get(BpmnXmlConstants.NODE_PRE_HANDLER_FILTERS).get(0);
            String elementText = element.getElementText();
            List<Object> list = JSONUtil.toBean(elementText, List.class);
            flowDef.put(DefConstants.FLOW_PROP_NODE_PRE_HANDLER_FILTERS, list);
        }
        // nodeActionFilters
        if (extensionElementMap != null && extensionElementMap.containsKey(BpmnXmlConstants.NODE_ACTION_FILTERS)) {
            ExtensionElement element = extensionElementMap.get(BpmnXmlConstants.NODE_ACTION_FILTERS).get(0);
            String elementText = element.getElementText();
            List<Object> list = JSONUtil.toBean(elementText, List.class);
            flowDef.put(DefConstants.FLOW_PROP_NODE_ACTION_FILTERS, list);
        }
        // nodePostHandlerFilters
        if (extensionElementMap != null && extensionElementMap.containsKey(BpmnXmlConstants.NODE_POST_HANDLER_FILTERS)) {
            ExtensionElement element = extensionElementMap.get(BpmnXmlConstants.NODE_POST_HANDLER_FILTERS).get(0);
            String elementText = element.getElementText();
            List<Object> list = JSONUtil.toBean(elementText, List.class);
            flowDef.put(DefConstants.FLOW_PROP_NODE_POST_HANDLER_FILTERS, list);
        }
        // flowPreHandlerFilters
        if (extensionElementMap != null && extensionElementMap.containsKey(BpmnXmlConstants.FLOW_PRE_HANDLER_FILTERS)) {
            ExtensionElement element = extensionElementMap.get(BpmnXmlConstants.FLOW_PRE_HANDLER_FILTERS).get(0);
            String elementText = element.getElementText();
            List<Object> list = JSONUtil.toBean(elementText, List.class);
            flowDef.put(DefConstants.FLOW_PROP_FLOW_PRE_HANDLER_FILTERS, list);
        }
        // flowPostHandlerFilters
        if (extensionElementMap != null && extensionElementMap.containsKey(BpmnXmlConstants.FLOW_POST_HANDLER_FILTERS)) {
            ExtensionElement element = extensionElementMap.get(BpmnXmlConstants.FLOW_POST_HANDLER_FILTERS).get(0);
            String elementText = element.getElementText();
            List<Object> list = JSONUtil.toBean(elementText, List.class);
            flowDef.put(DefConstants.FLOW_PROP_FLOW_POST_HANDLER_FILTERS, list);
        }
        // runner
        if (extensionElementMap != null && extensionElementMap.containsKey(BpmnXmlConstants.RUNNER)) {
            ExtensionElement element = extensionElementMap.get(BpmnXmlConstants.RUNNER).get(0);
            String elementText = element.getElementText();
            Map<String, Object> runner = JSONUtil.toBean(elementText, Map.class);
            flowDef.put(DefConstants.FLOW_PROP_RUNNER, runner);
        }
        // parseListeners
        if (extensionElementMap != null && extensionElementMap.containsKey(BpmnXmlConstants.PARSE_LISTENERS)) {
            ExtensionElement element = extensionElementMap.get(BpmnXmlConstants.PARSE_LISTENERS).get(0);
            String elementText = element.getElementText();
            List<String> list = JSONUtil.toBean(elementText, List.class);
            flowDef.put(DefConstants.FLOW_PROP_PARSE_LISTENERS, list);
        }
        // logFlag
        if (extensionElementMap != null && extensionElementMap.containsKey(BpmnXmlConstants.LOG_FLAG)) {
            ExtensionElement element = extensionElementMap.get(BpmnXmlConstants.LOG_FLAG).get(0);
            String elementText = element.getElementText();
            if (StringUtils.isNotEmpty(elementText)) {
                flowDef.put(DefConstants.FLOW_PROP_LOG_FLAG, Boolean.valueOf(elementText));
            }
        }
    }
}

package com.xiaopeng.workflow;

import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.jeasy.flows.engine.WorkFlowEngine;
import org.jeasy.flows.work.DefaultWorkReport;
import org.jeasy.flows.work.WorkContext;
import org.jeasy.flows.work.WorkStatus;
import org.jeasy.flows.workflow.WorkFlow;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.xiaopeng.workflow.HelloEasyFlowBpmnApplicationTests.*;
import static org.jeasy.flows.engine.WorkFlowEngineBuilder.aNewWorkFlowEngine;
@SpringBootTest
@Slf4j
public class HelloTests {
    private static final ExecutorService THREAD_POOL = new ThreadPoolExecutor(10, 10, 100, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(2048));
    private static HashMap<String, WorkFlow> componentMap;
    private static WorkFlowEngine engine = aNewWorkFlowEngine().build();

    @BeforeAll
    public static void init() {
        HelloEasyFlowBpmnApplicationTests.init();
    }
    /**
     * 拉齐现有工作流功能 case
     */
    @Test
    public void testConvertXPComp() throws IOException {
        Resource resource = getResource("classpath:flow.json/1.json");
        JSON json = JSONUtil.readJSON(resource.getFile(), StandardCharsets.UTF_8);
        WorkContext workContext = new WorkContext();
        workContext.put("XGPTSwitch", true);
        workContext.put("conditionPath", "COMPONENT_V");
        commonExecute(json, workContext);
    }

    /**
     * 测试条件组件
     */
    @Test
    public void testConditionXPComp() throws IOException {
        Resource resource = getResource("classpath:flow.json/condition.json");
        JSON json = JSONUtil.readJSON(resource.getFile(), StandardCharsets.UTF_8);
        WorkContext workContext = new WorkContext();
        workContext.put("XGPTSwitch", true);
        workContext.put("conditionPath", "COMPONENT_V");
        commonExecute(json, workContext);
    }


    @Test
    public void testSimpleMultiPredicate() throws IOException {
        Resource resource = getResource("classpath:flow.json/3.json");
        JSON json = JSONUtil.readJSON(resource.getFile(), StandardCharsets.UTF_8);
        WorkContext workContext = new WorkContext();
        workContext.put("XGPTSwitch", true);
        workContext.put("conditionPath", "COMPONENT_V");
        commonExecute(json, workContext);
    }

    @Test
    public void testSimpleRepeat() throws IOException {
        Resource resource = getResource("classpath:flow.json/repeat.json");
        JSON json = JSONUtil.readJSON(resource.getFile(), StandardCharsets.UTF_8);
        WorkContext workContext = new WorkContext();
        workContext.put("XGPTSwitch", true);
        workContext.put("conditionPath", "COMPONENT_V");
        commonExecute(json, workContext);
    }


    @Test
    public void testBpmn2Json() throws IOException {
        Resource resource = getResource("classpath:flow.json/bpmn2json.json");
        JSON json = JSONUtil.readJSON(resource.getFile(), StandardCharsets.UTF_8);
        log.info("Model is:{}", json);
        WorkContext workContext = new WorkContext();
        workContext.put("XGPTSwitch", true);
        commonExecute(json, workContext);
    }
}

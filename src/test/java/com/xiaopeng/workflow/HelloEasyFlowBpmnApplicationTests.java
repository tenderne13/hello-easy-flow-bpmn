package com.xiaopeng.workflow;

import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import com.xiaopeng.workflow.components.XPComponentStep;
import com.xiaopeng.workflow.components.XPWorkFLowBuilder;
import lombok.extern.slf4j.Slf4j;
import org.jeasy.flows.engine.WorkFlowEngine;
import org.jeasy.flows.work.DefaultWorkReport;
import org.jeasy.flows.work.WorkContext;
import org.jeasy.flows.work.WorkReport;
import org.jeasy.flows.work.WorkStatus;
import org.jeasy.flows.workflow.WorkFlow;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.jeasy.flows.engine.WorkFlowEngineBuilder.aNewWorkFlowEngine;

@SpringBootTest
@Slf4j
class HelloEasyFlowBpmnApplicationTests {
    private static final ExecutorService THREAD_POOL = new ThreadPoolExecutor(10, 10, 100, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(2048));
    private static HashMap<String, WorkFlow> componentMap;
    private static WorkFlowEngine engine = aNewWorkFlowEngine().build();
    private static final Yaml yaml = new Yaml();

    @BeforeAll
    public static void init() {
        String COMPONENT_S = "COMPONENT_S";
        String COMPONENT_G = "COMPONENT_G";
        String COMPONENT_I = "COMPONENT_I";
        String COMPONENT_V = "COMPONENT_V";
        String COMPONENT_BE = "COMPONENT_BE";
        String COMPONENT_QM = "COMPONENT_QM";
        String COMPONENT_GEN = "COMPONENT_GEN";
        String COMPONENT_TAG = "COMPONENT_TAG";
        String COMPONENT_PRE = "COMPONENT_PRE";
        String COMPONENT_UN = "COMPONENT_UN";

        String COMPONENT_L = "COMPONENT_L";
        String COMPONENT_LQM = "COMPONENT_LQM";
        String COMPONENT_LR = "COMPONENT_LR";
        componentMap = new HashMap<>();
        componentMap.put(COMPONENT_I, workContext -> {
            log.info("COMPONENT_I execute start");
            sleepForAWhile(COMPONENT_I);
            return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
        });
        componentMap.put(COMPONENT_V, workContext -> {
            log.info("COMPONENT_V execute start");
            sleepForAWhile(COMPONENT_V);
            return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
        });

        componentMap.put(COMPONENT_BE, workContext -> {
            log.info(COMPONENT_BE, "{} execute start");
            sleepForAWhile(COMPONENT_BE);
            return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
        });

        componentMap.put(COMPONENT_QM, workContext -> {
            log.info(COMPONENT_QM, "{} execute start");
            sleepForAWhile(COMPONENT_QM);
            return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
        });

        componentMap.put("ENTITY_ENSEMBLE", workContext -> {
            log.info("ENTITY_ENSEMBLE execute start");
            sleepForAWhile("ENTITY_ENSEMBLE");
            return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
        });

        componentMap.put(COMPONENT_GEN, workContext -> {
            log.info(COMPONENT_GEN, "{} execute start");
            sleepForAWhile(COMPONENT_GEN);
            return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
        });

        componentMap.put(COMPONENT_TAG, workContext -> {
            log.info(COMPONENT_TAG, "{} execute start");
            sleepForAWhile(COMPONENT_TAG);
            return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
        });


        componentMap.put(COMPONENT_PRE, workContext -> {
            log.info(COMPONENT_PRE, "{} execute start");
            sleepForAWhile(COMPONENT_PRE);
            return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
        });

        componentMap.put(COMPONENT_UN, workContext -> {
            log.info(COMPONENT_UN, "{} execute start");
            sleepForAWhile(COMPONENT_UN);
            return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
        });

        componentMap.put(COMPONENT_S, workContext -> {
            log.info(COMPONENT_S, "{} execute start");
            sleepForAWhile(COMPONENT_S);
            return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
        });

        componentMap.put(COMPONENT_G, workContext -> {
            log.info("COMPONENT_G execute start");
            sleepForAWhile(COMPONENT_G);
            return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
        });

        componentMap.put(COMPONENT_L, workContext -> {
            log.info(COMPONENT_L, "{} execute start");
            sleepForAWhile(COMPONENT_L);
            return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
        });

        componentMap.put(COMPONENT_LQM, workContext -> {
            log.info(COMPONENT_LQM, "{} execute start");
            sleepForAWhile(COMPONENT_LQM);
            return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
        });

        componentMap.put(COMPONENT_LR, workContext -> {
            log.info(COMPONENT_LR, "{} execute start");
            sleepForAWhile(COMPONENT_LR);
            return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
        });
    }

    static Resource getResource(String path) {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        return resolver.getResource(path);
    }

    static void commonExecute(WorkContext workContext, XPComponentStep xpComponentStep) {
        WorkFlow workFlow = XPWorkFLowBuilder.buildWorkFlow(componentMap, xpComponentStep, THREAD_POOL);
        final WorkReport report = engine.run(workFlow, workContext);
        log.info("report:{}", JSONUtil.toJsonStr(report));
    }

    static XPComponentStep loadConfig(String configPath) throws IOException {
        if (configPath.endsWith(".json")) {
            JSON json = JSONUtil.readJSON(getResource(configPath).getFile(), StandardCharsets.UTF_8);
            return JSONUtil.toBean(json, XPComponentStep.class, false);
        } else {
            return yaml.loadAs(getResource(configPath).getInputStream(), XPComponentStep.class);
        }
    }

    /**
     * 简单顺序执行示例
     */
    @Test
    public void testSimpleSequentialFromJson() throws IOException {
        XPComponentStep xpComponentStep = loadConfig("classpath:flow.json/simple_sequential.json");
        commonExecute(new WorkContext(), xpComponentStep);
    }

    @Test
    public void testSimpleSequentialFromYml() throws IOException {
        XPComponentStep xpComponentStep = loadConfig("classpath:flow.json/simple_sequential.yml");
        commonExecute(new WorkContext(), xpComponentStep);
    }

    @Test
    public void testSimpleParallelJ() throws IOException {
        XPComponentStep xpComponentStep = loadConfig("classpath:flow.json/simple_parallel.json");
        commonExecute(new WorkContext(), xpComponentStep);
    }

    @Test
    public void testSimpleParallelY() throws IOException {
        XPComponentStep xpComponentStep = loadConfig("classpath:flow.json/simple_parallel.yml");
        commonExecute(new WorkContext(), xpComponentStep);
    }

    @Test
    public void testSimpleMultiConditionJ() throws IOException {
        XPComponentStep xpComponentStep = loadConfig("classpath:flow.json/simple_multi_condition.json");
        commonExecute(new WorkContext(), xpComponentStep);
    }

    @Test
    public void testSimpleMultiConditionY() throws IOException {
        XPComponentStep xpComponentStep = loadConfig("classpath:flow.json/simple_multi_condition.yml");
        commonExecute(new WorkContext(), xpComponentStep);
    }

    @Test
    public void testSimpleRepeatCaseJ() throws IOException {
        XPComponentStep xpComponentStep = loadConfig("classpath:flow.json/simple_repeat.json");
        commonExecute(new WorkContext(), xpComponentStep);
    }

    @Test
    public void testSimpleRepeatCaseY() throws IOException {
        XPComponentStep xpComponentStep = loadConfig("classpath:flow.json/simple_repeat.yml");
        commonExecute(new WorkContext(), xpComponentStep);
    }

    /**
     * 复杂 场景示例
     */
    @Test
    public void testComplexFlow() throws IOException {
        XPComponentStep xpComponentStep = loadConfig("classpath:flow.json/complex.json");
        WorkContext workContext = new WorkContext();
        workContext.put("XGPTSwitch", true);
        commonExecute(workContext, xpComponentStep);
    }

    static void sleepForAWhile(String componentName) {
        Random random = new Random();
        int sleepTime = random.nextInt(5000); // Generates a random sleep time between 0 and 5000 milliseconds
        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        log.info("{} execute end ==> cost time:{}ms", componentName, sleepTime);
    }
}

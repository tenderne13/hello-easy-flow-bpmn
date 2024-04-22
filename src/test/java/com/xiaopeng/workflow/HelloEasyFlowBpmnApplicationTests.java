package com.xiaopeng.workflow;

import cn.hutool.json.JSONUtil;
import com.xiaopeng.workflow.components.XPComponentStep;
import com.xiaopeng.workflow.components.XPWorkFLowBuilder;
import com.xiaopeng.workflow.converter.BpmnConverter;
import lombok.extern.slf4j.Slf4j;
import org.jeasy.flows.engine.WorkFlowEngine;
import org.jeasy.flows.work.DefaultWorkReport;
import org.jeasy.flows.work.WorkContext;
import org.jeasy.flows.work.WorkReport;
import org.jeasy.flows.work.WorkStatus;
import org.jeasy.flows.workflow.WorkFlow;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    //@Test
    void contextLoads() {
    }


    //@Test
    public void testConvert() throws IOException {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource resource = resolver.getResource("classpath:flow/converter/paral.bpmn");
        List<Map<String, Object>> convert = BpmnConverter.convert(resource.getInputStream());
        log.info("Model is:" + JSONUtil.toJsonStr(convert));
    }

    /**
     * 拉齐现有工作流功能 case
     */
    //@Test
    public void testConvertXPComp() {

        String jsonStr = "{\"name\":\"工作流\",\"type\":\"sequential\",\"sequentialSteps\":[{\"name\":\"初始化操作\",\"component\":\"COMPONENT_I\"},{\"name\":\"获取词汇表\",\"component\":\"COMPONENT_V\"},{\"name\":\"并行执行\",\"parallelSteps\":[{\"component\":\"COMPONENT_BE\"},{\"component\":\"COMPONENT_QM\"}],\"type\":\"parallel\"},{\"name\":\"实体集成\",\"component\":\"ENTITY_ENSEMBLE\"},{\"name\":\"并行执行全局节点和可见及可说节点\",\"type\":\"parallel\",\"parallelSteps\":[{\"name\":\"全局节点\",\"type\":\"sequential\",\"sequentialSteps\":[{\"name\":\"初始化操作\",\"component\":\"COMPONENT_GEN\"},{\"name\":\"标签集成\",\"component\":\"COMPONENT_TAG\"},{\"name\":\"并行执行预测\",\"type\":\"parallel\",\"parallelSteps\":[{\"component\":\"COMPONENT_PRE\"},{\"component\":\"COMPONENT_UN\"}]}]},{\"name\":\"场景ES\",\"component\":\"COMPONENT_S\"}]},{\"name\":\"全局场景融合\",\"component\":\"COMPONENT_G\"}]}";
        WorkContext workContext = new WorkContext();
        workContext.put("XGPTSwitch", true);
        workContext.put("conditionPath", "COMPONENT_V");
        commonExecute(jsonStr, workContext);
    }

    private static void commonExecute(String jsonStr, WorkContext workContext) {
        log.info("jsonStr:{}", jsonStr);
        XPComponentStep xpComponentStep = JSONUtil.toBean(jsonStr, XPComponentStep.class);
        HashMap<String, WorkFlow> componentMap = buildComponentMap();
        WorkFlow workFlow = XPWorkFLowBuilder.buildWorkFlow(componentMap, xpComponentStep, THREAD_POOL);
        WorkFlowEngine engine = aNewWorkFlowEngine().build();
        final WorkReport report = engine.run(workFlow, workContext);
        log.info("report:{}", JSONUtil.toJsonStr(report));
    }


    /**
     * 测试条件组件
     */
    //@Test
    public void testConditionXPComp() {
        String jsonStr = "{\"name\":\"工作流\",\"type\":\"sequential\",\"sequentialSteps\":[{\"name\":\"初始化操作\",\"component\":\"COMPONENT_I\"},{\"name\":\"e2e and llm flow\",\"type\":\"parallel\",\"parallelSteps\":[{\"name\":\"e2e-flow\",\"type\":\"sequential\",\"sequentialSteps\":[{\"name\":\"获取词汇表\",\"component\":\"COMPONENT_V\"},{\"name\":\"并行执行\",\"type\":\"parallel\",\"parallelSteps\":[{\"component\":\"COMPONENT_BE\"},{\"component\":\"COMPONENT_QM\"}]},{\"name\":\"实体集成\",\"component\":\"ENTITY_ENSEMBLE\"},{\"name\":\"并行执行全局节点和可见及可说节点\",\"type\":\"parallel\",\"parallelSteps\":[{\"name\":\"全局节点\",\"type\":\"sequential\",\"sequentialSteps\":[{\"name\":\"初始化操作\",\"component\":\"COMPONENT_GEN\"},{\"name\":\"标签集成\",\"component\":\"COMPONENT_TAG\"},{\"name\":\"并行执行预测\",\"type\":\"parallel\",\"parallelSteps\":[{\"component\":\"COMPONENT_PRE\"},{\"component\":\"COMPONENT_UN\"}]}]},{\"name\":\"场景ES\",\"component\":\"COMPONENT_S\"}]}]},{\"name\":\"llm 链路\",\"type\":\"conditional\",\"conditionSteps\":[{\"predicateClassName\":\"com.xiaopeng.workflow.components.predict.XGPTSwitchPredicate\",\"componentStep\":{\"type\":\"sequential\",\"name\":\"thenWorkFlow\",\"sequentialSteps\":[{\"name\":\"thenWorkFlow\",\"type\":\"sequential\",\"conditionStep\":1,\"sequentialSteps\":[{\"name\":\"llmParael\",\"type\":\"parallel\",\"parallelSteps\":[{\"name\":\"LLM\",\"component\":\"LLM\"},{\"name\":\"COMPONENT_LQM\",\"component\":\"COMPONENT_LQM\"}]},{\"name\":\"COMPONENT_LR\",\"component\":\"COMPONENT_LR\"}]}]}}]}]},{\"name\":\"全局场景融合\",\"component\":\"COMPONENT_G\"}]}";
        WorkContext workContext = new WorkContext();
        workContext.put("XGPTSwitch", true);
        workContext.put("conditionPath", "COMPONENT_V");
        commonExecute(jsonStr, workContext);
    }

    //@Test
    public void testSimpleMulitPredicate() {
        String jsonStr = "{\"name\":\"工作流\",\"type\":\"sequential\",\"sequentialSteps\":[{\"name\":\"初始化操作\",\"component\":\"COMPONENT_I\"},{\"name\":\"多条件流\",\"type\":\"conditional\",\"conditionSteps\":[{\"predicateClassName\":\"com.xiaopeng.workflow.components.predict.MulitPredicate.IF_COMPONENT_V_CASE\",\"componentStep\":{\"name\":\"获取词汇表\",\"component\":\"COMPONENT_V\"}},{\"predicateClassName\":\"com.xiaopeng.workflow.components.predict.MulitPredicate.IF_COMPONENT_BE_CASE\",\"componentStep\":{\"name\":\"COMPONENT_BE\",\"component\":\"COMPONENT_BE\"}},{\"predicateClassName\":\"com.xiaopeng.workflow.components.predict.MulitPredicate.IF_COMPONENT_QM_CASE\",\"componentStep\":{\"name\":\"COMPONENT_QM\",\"component\":\"COMPONENT_QM\"}}]},{\"name\":\"全局场景融合\",\"component\":\"COMPONENT_G\"}]}";
        WorkContext workContext = new WorkContext();
        workContext.put("XGPTSwitch", true);
        workContext.put("conditionPath", "COMPONENT_V");
        commonExecute(jsonStr, workContext);
    }

    //@Test
    public void testSimpleRepeat() {
        String jsonStr = "{\"name\":\"工作流\",\"type\":\"sequential\",\"sequentialSteps\":[{\"name\":\"初始化操作\",\"component\":\"COMPONENT_I\"},{\"name\":\"多条件流\",\"type\":\"repeat\",\"repeatStep\":{\"predicateClassName\":\"com.xiaopeng.workflow.components.predict.MulitPredicate.IF_COMPONENT_V_CASE\",\"componentStep\":{\"name\":\"获取词汇表\",\"component\":\"COMPONENT_V\"}}},{\"name\":\"全局场景融合\",\"component\":\"COMPONENT_G\"}]}";
        WorkContext workContext = new WorkContext();
        workContext.put("XGPTSwitch", true);
        workContext.put("conditionPath", "COMPONENT_V");
        commonExecute(jsonStr, workContext);
    }

    /********************************************************************************************以下为简单示例******************************************************************************************************************************/

    /**
     * 简单顺序执行示例
     */
    @Test
    public void testSimpleSequential() {
        String jsonStr = "{\"name\":\"顺序流示例\",\"type\":\"sequential\",\"sequentialSteps\":[{\"name\":\"组件I\",\"component\":\"COMPONENT_I\"},{\"name\":\"组件V\",\"component\":\"COMPONENT_V\"},{\"name\":\"组件G\",\"component\":\"COMPONENT_G\"}]}";
        WorkContext workContext = new WorkContext();
        commonExecute(jsonStr, workContext);
    }

    @Test
    public void testSimpleParallel() {
        String jsonStr = "{\"name\":\"并行流示例\",\"type\":\"parallel\",\"parallelSteps\":[{\"name\":\"组件I\",\"component\":\"COMPONENT_I\"},{\"name\":\"组件V\",\"component\":\"COMPONENT_V\"},{\"name\":\"组件G\",\"component\":\"COMPONENT_G\"}]}";
        WorkContext workContext = new WorkContext();
        commonExecute(jsonStr, workContext);
    }

    @Test
    public void testSimpleMulitCondition() {
        String jsonStr = "{\"name\":\"条件判断工作流示例\",\"type\":\"sequential\",\"sequentialSteps\":[{\"name\":\"COMPONENT_I\",\"component\":\"COMPONENT_I\"},{\"name\":\"多条件流\",\"type\":\"conditional\",\"conditionSteps\":[{\"predicateClassName\":\"com.xiaopeng.workflow.components.predict.MulitPredicate.IF_COMPONENT_V_CASE\",\"componentStep\":{\"name\":\"COMPONENT_V\",\"component\":\"COMPONENT_V\"}},{\"predicateClassName\":\"com.xiaopeng.workflow.components.predict.MulitPredicate.IF_COMPONENT_BE_CASE\",\"componentStep\":{\"name\":\"COMPONENT_BE\",\"component\":\"COMPONENT_BE\"}},{\"predicateClassName\":\"com.xiaopeng.workflow.components.predict.MulitPredicate.IF_COMPONENT_QM_CASE\",\"componentStep\":{\"name\":\"COMPONENT_QM\",\"component\":\"COMPONENT_QM\"}},{\"conditionStep\":2,\"componentStep\":{\"name\":\"COMPONENT_L\",\"component\":\"COMPONENT_L\"}}]},{\"name\":\"COMPONENT_G\",\"component\":\"COMPONENT_G\"}]}";
        WorkContext workContext = new WorkContext();
        //workContext.put("conditionPath", "COMPONENT_V");
        commonExecute(jsonStr, workContext);
    }


    @Test
    public void testSimpleRepeatCase() {
        String jsonStr = "{\"name\":\"repeat工作流示例\",\"type\":\"sequential\",\"sequentialSteps\":[{\"name\":\"重复3次\",\"type\":\"repeat\",\"repeatStep\":{\"predicateClassName\":\"com.xiaopeng.workflow.components.predict.MulitPredicate.REPEAT_PREDICATE\",\"componentStep\":{\"name\":\"COMPONENT_I\",\"component\":\"COMPONENT_I\"}}}]}";
        WorkContext workContext = new WorkContext();
        commonExecute(jsonStr, workContext);
    }

    /**
     * 复杂 场景示例
     */
    @Test
    public void testComplexFlow() {
        String jsonStr = "{\"name\":\"复杂工作流示例\",\"type\":\"sequential\",\"sequentialSteps\":[{\"name\":\"COMPONENT_I\",\"component\":\"COMPONENT_I\"},{\"name\":\"e2e and COMPONENT_L flow\",\"type\":\"parallel\",\"parallelSteps\":[{\"name\":\"e2e-flow\",\"type\":\"sequential\",\"sequentialSteps\":[{\"name\":\"COMPONENT_V\",\"component\":\"COMPONENT_V\"},{\"name\":\"并行执行\",\"type\":\"parallel\",\"parallelSteps\":[{\"component\":\"COMPONENT_BE\"},{\"component\":\"COMPONENT_QM\"}]},{\"name\":\"实体集成\",\"component\":\"ENTITY_ENSEMBLE\"},{\"name\":\"并行执行 全局节点 & COMPONENT_S 节点\",\"type\":\"parallel\",\"parallelSteps\":[{\"name\":\"全局节点\",\"type\":\"sequential\",\"sequentialSteps\":[{\"name\":\"初始化操作\",\"component\":\"COMPONENT_GEN\"},{\"name\":\"标签集成\",\"component\":\"COMPONENT_TAG\"},{\"name\":\"并行执行预测\",\"type\":\"parallel\",\"parallelSteps\":[{\"component\":\"COMPONENT_PRE\"},{\"component\":\"COMPONENT_UN\"}]}]},{\"name\":\"COMPONENT_S\",\"component\":\"COMPONENT_S\"}]}]},{\"name\":\"L链路\",\"type\":\"conditional\",\"conditionSteps\":[{\"predicateClassName\":\"com.xiaopeng.workflow.components.predict.XGPTSwitchPredicate\",\"componentStep\":{\"type\":\"sequential\",\"name\":\"thenWorkFlow\",\"sequentialSteps\":[{\"name\":\"thenWorkFlow\",\"type\":\"sequential\",\"conditionStep\":1,\"sequentialSteps\":[{\"name\":\"COMPONENT_LParael\",\"type\":\"parallel\",\"parallelSteps\":[{\"name\":\"COMPONENT_L\",\"component\":\"COMPONENT_L\"},{\"name\":\"COMPONENT_L_COMPONENT_QM\",\"component\":\"COMPONENT_LQM\"}]},{\"name\":\"COMPONENT_LR\",\"component\":\"COMPONENT_LR\"}]}]}}]}]},{\"name\":\"COMPONENT_G\",\"component\":\"COMPONENT_G\"}]}";
        WorkContext workContext = new WorkContext();
        workContext.put("XGPTSwitch", true);
        commonExecute(jsonStr, workContext);
    }


    private static HashMap<String, WorkFlow> buildComponentMap() {
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
        HashMap<String, WorkFlow> componentMap = new HashMap<>();
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
            log.info(COMPONENT_BE + " execute start");
            sleepForAWhile(COMPONENT_BE);
            return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
        });

        componentMap.put(COMPONENT_QM, workContext -> {
            log.info(COMPONENT_QM + " execute start");
            sleepForAWhile(COMPONENT_QM);
            return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
        });

        componentMap.put("ENTITY_ENSEMBLE", workContext -> {
            log.info("ENTITY_ENSEMBLE execute start");
            sleepForAWhile("ENTITY_ENSEMBLE");
            return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
        });


        componentMap.put(COMPONENT_GEN, workContext -> {
            log.info(COMPONENT_GEN + " execute start");
            sleepForAWhile(COMPONENT_GEN);
            return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
        });


        componentMap.put(COMPONENT_TAG, workContext -> {
            log.info(COMPONENT_TAG + " execute start");
            sleepForAWhile(COMPONENT_TAG);
            return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
        });


        componentMap.put(COMPONENT_PRE, workContext -> {
            log.info(COMPONENT_PRE + " execute start");
            sleepForAWhile(COMPONENT_PRE);
            return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
        });

        componentMap.put(COMPONENT_UN, workContext -> {
            log.info(COMPONENT_UN + " execute start");
            sleepForAWhile(COMPONENT_UN);
            return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
        });

        componentMap.put(COMPONENT_S, workContext -> {
            log.info(COMPONENT_S + " execute start");
            sleepForAWhile(COMPONENT_S);
            return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
        });

        componentMap.put(COMPONENT_G, workContext -> {
            log.info("COMPONENT_G execute start");
            sleepForAWhile(COMPONENT_G);
            return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
        });


        componentMap.put(COMPONENT_L, workContext -> {
            log.info(COMPONENT_L + " execute start");
            sleepForAWhile(COMPONENT_L);
            return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
        });

        componentMap.put(COMPONENT_LQM, workContext -> {
            log.info(COMPONENT_LQM + " execute start");
            sleepForAWhile(COMPONENT_LQM);
            return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
        });

        componentMap.put(COMPONENT_LR, workContext -> {
            log.info(COMPONENT_LR + " execute start");
            sleepForAWhile(COMPONENT_LR);
            return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
        });
        return componentMap;
    }

    private static void sleepForAWhile(String componentName) {
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

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

    @Test
    void contextLoads() {
    }


    @Test
    public void testConvert() throws IOException {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource resource = resolver.getResource("classpath:flow/converter/paral.bpmn");
        List<Map<String, Object>> convert = BpmnConverter.convert(resource.getInputStream());
        log.info("Model is:" + JSONUtil.toJsonStr(convert));
    }

    /**
     * 拉齐现有工作流功能 case
     */
    @Test
    public void testConvertXPComp() {

        String jsonStr = "{\"name\":\"工作流\",\"type\":\"sequential\",\"sequentialSteps\":[{\"name\":\"初始化操作\",\"component\":\"INIT_ENV\"},{\"name\":\"获取词汇表\",\"component\":\"RETRIEVE_VOCAB\"},{\"name\":\"并行执行\",\"parallelSteps\":[{\"component\":\"BERT_CRF_ENTITY_EXTRACTOR\"},{\"component\":\"TEMPLATE_QUERY_MATCHER\"}],\"type\":\"parallel\"},{\"name\":\"实体集成\",\"component\":\"ENTITY_ENSEMBLE\"},{\"name\":\"并行执行全局节点和可见及可说节点\",\"type\":\"parallel\",\"parallelSteps\":[{\"name\":\"全局节点\",\"type\":\"sequential\",\"sequentialSteps\":[{\"name\":\"初始化操作\",\"component\":\"GENERAL_RULE_TAGGER\"},{\"name\":\"标签集成\",\"component\":\"TAG_ENSEMBLE\"},{\"name\":\"并行执行预测\",\"type\":\"parallel\",\"parallelSteps\":[{\"component\":\"TEMPLATE_ACTION_PREDICTION\"},{\"component\":\"UNILM_ACTION_PREDICTION\"}]}]},{\"name\":\"场景ES\",\"component\":\"SCENE_ES\"}]},{\"name\":\"全局场景融合\",\"component\":\"GLOBAL_SCENE_FUSION\"}]}";
        commonExecute(jsonStr);
    }

    private static void commonExecute(String jsonStr) {
        XPComponentStep xpComponentStep = JSONUtil.toBean(jsonStr, XPComponentStep.class);
        HashMap<String, WorkFlow> componentMap = buildComponentMap();
        WorkFlow workFlow = XPWorkFLowBuilder.buildWorkFlow(componentMap, xpComponentStep, THREAD_POOL);

        WorkFlowEngine engine = aNewWorkFlowEngine().build();
        WorkContext workContext = new WorkContext();
        workContext.put("XGPTSwitch", true);
        workContext.put("conditionPath", "TEMPLATE_QUERY_MATCHER");
        final WorkReport report = engine.run(workFlow, workContext);
        log.info("report:{}", report);
    }


    /**
     * 测试条件组件
     */
    @Test
    public void testConditionXPComp() {
        String jsonStr = "{\"name\":\"工作流\",\"type\":\"sequential\",\"sequentialSteps\":[{\"name\":\"初始化操作\",\"component\":\"INIT_ENV\"},{\"name\":\"e2e and llm flow\",\"type\":\"parallel\",\"parallelSteps\":[{\"name\":\"e2e-flow\",\"type\":\"sequential\",\"sequentialSteps\":[{\"name\":\"获取词汇表\",\"component\":\"RETRIEVE_VOCAB\"},{\"name\":\"并行执行\",\"type\":\"parallel\",\"parallelSteps\":[{\"component\":\"BERT_CRF_ENTITY_EXTRACTOR\"},{\"component\":\"TEMPLATE_QUERY_MATCHER\"}]},{\"name\":\"实体集成\",\"component\":\"ENTITY_ENSEMBLE\"},{\"name\":\"并行执行全局节点和可见及可说节点\",\"type\":\"parallel\",\"parallelSteps\":[{\"name\":\"全局节点\",\"type\":\"sequential\",\"sequentialSteps\":[{\"name\":\"初始化操作\",\"component\":\"GENERAL_RULE_TAGGER\"},{\"name\":\"标签集成\",\"component\":\"TAG_ENSEMBLE\"},{\"name\":\"并行执行预测\",\"type\":\"parallel\",\"parallelSteps\":[{\"component\":\"TEMPLATE_ACTION_PREDICTION\"},{\"component\":\"UNILM_ACTION_PREDICTION\"}]}]},{\"name\":\"场景ES\",\"component\":\"SCENE_ES\"}]}]},{\"name\":\"llm 链路\",\"type\":\"conditional\",\"conditionSteps\":[{\"predicateClassName\":\"com.xiaopeng.workflow.components.predict.XGPTSwitchPredicate\",\"componentStep\":{\"type\":\"sequential\",\"name\":\"thenWorkFlow\",\"sequentialSteps\":[{\"name\":\"thenWorkFlow\",\"type\":\"sequential\",\"conditionStep\":1,\"sequentialSteps\":[{\"name\":\"llmParael\",\"type\":\"parallel\",\"parallelSteps\":[{\"name\":\"LLM\",\"component\":\"LLM\"},{\"name\":\"LLM_TEMPLATE_QUERY_MATCHER\",\"component\":\"LLM_TEMPLATE_QUERY_MATCHER\"}]},{\"name\":\"LLMPOST_RULE\",\"component\":\"LLMPOST_RULE\"}]}]}}]}]},{\"name\":\"全局场景融合\",\"component\":\"GLOBAL_SCENE_FUSION\"}]}";
        commonExecute(jsonStr);
    }

    @Test
    public void testSimpleMulitPredicate() {
        String jsonStr = "{\"name\":\"工作流\",\"type\":\"sequential\",\"sequentialSteps\":[{\"name\":\"初始化操作\",\"component\":\"INIT_ENV\"},{\"name\":\"多条件流\",\"type\":\"conditional\",\"conditionSteps\":[{\"predicateClassName\":\"com.xiaopeng.workflow.components.predict.MulitPredicate.IF_RETRIEVE_VOCAB_CASE\",\"componentStep\":{\"name\":\"获取词汇表\",\"component\":\"RETRIEVE_VOCAB\"}},{\"predicateClassName\":\"com.xiaopeng.workflow.components.predict.MulitPredicate.IF_BERT_CRF_ENTITY_EXTRACTOR_CASE\",\"componentStep\":{\"name\":\"BERT_CRF_ENTITY_EXTRACTOR\",\"component\":\"BERT_CRF_ENTITY_EXTRACTOR\"}},{\"predicateClassName\":\"com.xiaopeng.workflow.components.predict.MulitPredicate.IF_TEMPLATE_QUERY_MATCHER_CASE\",\"componentStep\":{\"name\":\"TEMPLATE_QUERY_MATCHER\",\"component\":\"TEMPLATE_QUERY_MATCHER\"}}]},{\"name\":\"全局场景融合\",\"component\":\"GLOBAL_SCENE_FUSION\"}]}";
        commonExecute(jsonStr);
    }


    private static HashMap<String, WorkFlow> buildComponentMap() {
        String sceneEs = "SCENE_ES";
        String globalSceneFusion = "GLOBAL_SCENE_FUSION";
        String initEnv = "INIT_ENV";
        String retrieveVocab = "RETRIEVE_VOCAB";
        String bertCrfEntityExtractor = "BERT_CRF_ENTITY_EXTRACTOR";
        String templateQueryMatcher = "TEMPLATE_QUERY_MATCHER";
        String generalRuleTagger = "GENERAL_RULE_TAGGER";
        String tagEnsemble = "TAG_ENSEMBLE";
        String templateActionPrediction = "TEMPLATE_ACTION_PREDICTION";
        String unilmActionPrediction = "UNILM_ACTION_PREDICTION";

        String LLM = "LLM";
        String LLM_TEMPLATE_QUERY_MATCHER = "LLM_TEMPLATE_QUERY_MATCHER";
        String LLMPOST_RULE = "LLMPOST_RULE";
        HashMap<String, WorkFlow> componentMap = new HashMap<>();
        componentMap.put(initEnv, workContext -> {
            log.info("INIT_ENV execute start");
            sleepForAWhile(initEnv);
            return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
        });
        componentMap.put(retrieveVocab, workContext -> {
            log.info("RETRIEVE_VOCAB execute start");
            sleepForAWhile(retrieveVocab);
            return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
        });

        componentMap.put(bertCrfEntityExtractor, workContext -> {
            log.info(bertCrfEntityExtractor + " execute start");
            sleepForAWhile(bertCrfEntityExtractor);
            return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
        });

        componentMap.put(templateQueryMatcher, workContext -> {
            log.info(templateQueryMatcher + " execute start");
            sleepForAWhile(templateQueryMatcher);
            return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
        });

        componentMap.put("ENTITY_ENSEMBLE", workContext -> {
            log.info("ENTITY_ENSEMBLE execute start");
            sleepForAWhile("ENTITY_ENSEMBLE");
            return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
        });


        componentMap.put(generalRuleTagger, workContext -> {
            log.info(generalRuleTagger + " execute start");
            sleepForAWhile(generalRuleTagger);
            return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
        });


        componentMap.put(tagEnsemble, workContext -> {
            log.info(tagEnsemble + " execute start");
            sleepForAWhile(tagEnsemble);
            return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
        });


        componentMap.put(templateActionPrediction, workContext -> {
            log.info(templateActionPrediction + " execute start");
            sleepForAWhile(templateActionPrediction);
            return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
        });

        componentMap.put(unilmActionPrediction, workContext -> {
            log.info(unilmActionPrediction + " execute start");
            sleepForAWhile(unilmActionPrediction);
            return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
        });

        componentMap.put(sceneEs, workContext -> {
            log.info(sceneEs + " execute start");
            sleepForAWhile(sceneEs);
            return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
        });

        componentMap.put(globalSceneFusion, workContext -> {
            log.info("globalSceneFusion execute start");
            sleepForAWhile(globalSceneFusion);
            return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
        });


        componentMap.put(LLM, workContext -> {
            log.info(LLM + " execute start");
            sleepForAWhile(LLM);
            return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
        });

        componentMap.put(LLM_TEMPLATE_QUERY_MATCHER, workContext -> {
            log.info(LLM_TEMPLATE_QUERY_MATCHER + " execute start");
            sleepForAWhile(LLM_TEMPLATE_QUERY_MATCHER);
            return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
        });

        componentMap.put(LLMPOST_RULE, workContext -> {
            log.info(LLMPOST_RULE + " execute start");
            sleepForAWhile(LLMPOST_RULE);
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

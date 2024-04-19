
# 1.simple workflow

```mermaid
flowchart TD
    INIT_ENV --> RETRIEVE_VOCAB
    RETRIEVE_VOCAB --> BERT_CRF_ENTITY_EXTRACTOR
    RETRIEVE_VOCAB --> TEMPLATE_QUERY_MATCHER
    
    BERT_CRF_ENTITY_EXTRACTOR --> ENTITY_ENSEMBLE
    TEMPLATE_QUERY_MATCHER --> ENTITY_ENSEMBLE
    
    
    subgraph "全局"
        GENERAL_RULE_TAGGER --> TAG_ENSEMBLE
        TAG_ENSEMBLE --> TEMPLATE_ACTION_PREDICTION
        TAG_ENSEMBLE --> UNILM_ACTION_PREDICTION
    end

    ENTITY_ENSEMBLE --> 全局
    ENTITY_ENSEMBLE --> SCENE_ES

    全局 --> GLOBAL_SCENE_FUSION
    SCENE_ES --> GLOBAL_SCENE_FUSION
```

## template json

```json
{
  "name": "工作流",
  "sequentialSteps": [
    {
      "name": "初始化操作",
      "component": "INIT_ENV"
    },
    {
      "name": "获取词汇表",
      "component": "RETRIEVE_VOCAB"
    },
    {
      "name": "并行执行",
      "parallelSteps": [
        {
          "component": "BERT_CRF_ENTITY_EXTRACTOR"
        },
        {
          "component": "TEMPLATE_QUERY_MATCHER"
        }
      ]
    },
    {
      "name": "实体集成",
      "component": "ENTITY_ENSEMBLE"
    },
    {
      "name": "并行执行全局节点和可见及可说节点",
      "parallelSteps": [
        {
          "name": "全局节点",
          "sequentialSteps": [
            {
              "name": "初始化操作",
              "component": "GENERAL_RULE_TAGGER"
            },
            {
              "name": "标签集成",
              "component": "TAG_ENSEMBLE"
            },
            {
              "name": "并行执行预测",
              "parallelSteps": [
                {
                  "component": "TEMPLATE_ACTION_PREDICTION"
                },
                {
                  "component": "UNILM_ACTION_PREDICTION"
                }
              ]
            }
          ]
        },
        {
          "name": "场景ES",
          "component": "SCENE_ES"
        }
      ]
    },
    {
      "name": "全局场景融合",
      "component": "GLOBAL_SCENE_FUSION"
    }
  ]
}
```

## UT
``` shell
mvn clean test -D test=com.xiaopeng.workflow.HelloEasyFlowBpmnApplicationTests#testConvertXPComp -e

11:37:38.315 [main] INFO   - ===================> sequential ULY9n94D build start <====================
11:37:38.318 [main] INFO   - build single component:INIT_ENV
11:37:38.318 [main] INFO   - build single component:RETRIEVE_VOCAB
11:37:38.318 [main] INFO   - ===================> parallel dA7jGnb4 build start <====================
11:37:38.318 [main] INFO   - build single component:BERT_CRF_ENTITY_EXTRACTOR
11:37:38.319 [main] INFO   - build single component:TEMPLATE_QUERY_MATCHER
11:37:38.331 [main] INFO   - =======> build parallel dA7jGnb4 flow success, component info  ==> {"name":"并行执行","parallelSteps":[{"component":"BERT_CRF_ENTITY_EXTRACTOR","type":"single"},{"component":"TEMPLATE_QUERY_MATCHER","type":"single"}],"type":"parallel"} <===
11:37:38.357 [main] INFO   - build single component:ENTITY_ENSEMBLE
11:37:38.357 [main] INFO   - ===================> parallel wPyDLLfE build start <====================
11:37:38.357 [main] INFO   - ===================> sequential oZa3MR3x build start <====================
11:37:38.358 [main] INFO   - build single component:GENERAL_RULE_TAGGER
11:37:38.358 [main] INFO   - build single component:TAG_ENSEMBLE
11:37:38.358 [main] INFO   - ===================> parallel GJLQBFkH build start <====================
11:37:38.358 [main] INFO   - build single component:TEMPLATE_ACTION_PREDICTION
11:37:38.358 [main] INFO   - build single component:UNILM_ACTION_PREDICTION
11:37:38.359 [main] INFO   - =======> build parallel GJLQBFkH flow success, component info  ==> {"name":"并行执行预测","parallelSteps":[{"component":"TEMPLATE_ACTION_PREDICTION","type":"single"},{"component":"UNILM_ACTION_PREDICTION","type":"single"}],"type":"parallel"} <===
11:37:38.362 [main] INFO   - =======> build sequential oZa3MR3x flow success, component info  ==> {"name":"全局节点","sequentialSteps":[{"name":"初始化操作","component":"GENERAL_RULE_TAGGER","type":"single"},{"name":"标签集成","component":"TAG_ENSEMBLE","type":"single"},{"name":"并行执行预测","parallelSteps":[{"component":"TEMPLATE_ACTION_PREDICTION","type":"single"},{"component":"UNILM_ACTION_PREDICTION","type":"single"}],"type":"parallel"}],"type":"sequential"} <===
11:37:38.367 [main] INFO   - build single component:SCENE_ES
11:37:38.369 [main] INFO   - =======> build parallel wPyDLLfE flow success, component info  ==> {"name":"并行执行全局节点和可见及可说节点","parallelSteps":[{"name":"全局节点","sequentialSteps":[{"name":"初始化操作","component":"GENERAL_RULE_TAGGER","type":"single"},{"name":"标签集成","component":"TAG_ENSEMBLE","type":"single"},{"name":"并行执行预测","parallelSteps":[{"component":"TEMPLATE_ACTION_PREDICTION","type":"single"},{"component":"UNILM_ACTION_PREDICTION","type":"single"}],"type":"parallel"}],"type":"sequential"},{"name":"场景ES","component":"SCENE_ES","type":"single"}],"type":"parallel"} <===
11:37:38.370 [main] INFO   - build single component:GLOBAL_SCENE_FUSION
11:37:38.374 [main] INFO   - =======> build sequential ULY9n94D flow success, component info  ==> {"name":"工作流","sequentialSteps":[{"name":"初始化操作","component":"INIT_ENV","type":"single"},{"name":"获取词汇表","component":"RETRIEVE_VOCAB","type":"single"},{"name":"并行执行","parallelSteps":[{"component":"BERT_CRF_ENTITY_EXTRACTOR","type":"single"},{"component":"TEMPLATE_QUERY_MATCHER","type":"single"}],"type":"parallel"},{"name":"实体集成","component":"ENTITY_ENSEMBLE","type":"single"},{"name":"并行执行全局节点和可见及可说节点","parallelSteps":[{"name":"全局节点","sequentialSteps":[{"name":"初始化操作","component":"GENERAL_RULE_TAGGER","type":"single"},{"name":"标签集成","component":"TAG_ENSEMBLE","type":"single"},{"name":"并行执行预测","parallelSteps":[{"component":"TEMPLATE_ACTION_PREDICTION","type":"single"},{"component":"UNILM_ACTION_PREDICTION","type":"single"}],"type":"parallel"}],"type":"sequential"},{"name":"场景ES","component":"SCENE_ES","type":"single"}],"type":"parallel"},{"name":"全局场景融合","component":"GLOBAL_SCENE_FUSION","type":"single"}],"type":"sequential"} <===
11:37:38.376 [main] INFO   - Running workflow ''工作流''
11:37:38.376 [main] INFO   - INIT_ENV execute start
11:37:42.516 [main] INFO   - INIT_ENV execute end ==> cost time:4134ms
11:37:42.516 [main] INFO   - RETRIEVE_VOCAB execute start
11:37:47.387 [main] INFO   - RETRIEVE_VOCAB execute end ==> cost time:4867ms
11:37:47.390 [pool-1-thread-1] INFO   - BERT_CRF_ENTITY_EXTRACTOR execute start
11:37:47.390 [pool-1-thread-2] INFO   - TEMPLATE_QUERY_MATCHER execute start
11:37:49.884 [pool-1-thread-1] INFO   - BERT_CRF_ENTITY_EXTRACTOR execute end ==> cost time:2490ms
11:37:52.385 [pool-1-thread-2] INFO   - TEMPLATE_QUERY_MATCHER execute end ==> cost time:4991ms
11:37:52.385 [main] INFO   - ENTITY_ENSEMBLE execute start
11:37:56.389 [main] INFO   - ENTITY_ENSEMBLE execute end ==> cost time:4000ms
11:37:56.390 [pool-1-thread-3] INFO   - GENERAL_RULE_TAGGER execute start
11:37:56.390 [pool-1-thread-4] INFO   - SCENE_ES execute start
11:37:56.446 [pool-1-thread-3] INFO   - GENERAL_RULE_TAGGER execute end ==> cost time:51ms
11:37:56.446 [pool-1-thread-3] INFO   - TAG_ENSEMBLE execute start
11:37:59.189 [pool-1-thread-4] INFO   - SCENE_ES execute end ==> cost time:2796ms
11:38:00.987 [pool-1-thread-3] INFO   - TAG_ENSEMBLE execute end ==> cost time:4536ms
11:38:00.987 [pool-1-thread-5] INFO   - TEMPLATE_ACTION_PREDICTION execute start
11:38:00.987 [pool-1-thread-6] INFO   - UNILM_ACTION_PREDICTION execute start
11:38:01.663 [pool-1-thread-6] INFO   - UNILM_ACTION_PREDICTION execute end ==> cost time:673ms
11:38:05.296 [pool-1-thread-5] INFO   - TEMPLATE_ACTION_PREDICTION execute end ==> cost time:4304ms
11:38:05.296 [main] INFO   - globalSceneFusion execute start
11:38:08.398 [main] INFO   - GLOBAL_SCENE_FUSION execute end ==> cost time:3098ms
11:38:08.398 [main] INFO   - report:DefaultWorkReport {status=COMPLETED, context=context={}}, error=''}



```
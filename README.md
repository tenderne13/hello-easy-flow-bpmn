# XPComponentStep 类文档说明

## 概述

`XPComponentStep` 类是 `com.xiaopeng.workflow.components` 包的一部分。它代表工作流中的一个步骤，可以是以下几种类型：单一(
single)、顺序(sequential)、并行(parallel)、条件(conditional)、重复(repeat)。

## 类型解析

### 单一(single)

单一类型的步骤表示在工作流中只执行一个操作。在 `XPComponentStep` 中，如果 `type` 字段为 "single"，则表示此步骤是单一操作。
该类型为工作流中最原子操作，不包含其他操作。

```mermaid
graph LR
    Start(开始) --> SingleAction[执行单一操作]
    SingleAction --> End(结束)
```

### 顺序(sequential)

顺序类型的步骤表示在工作流中按照一定的顺序执行一系列的操作。在 `XPComponentStep` 中，如果 `type` 字段为 "sequential"
，则 `sequentialSteps` 字段会包含一个 `XPComponentStep` 对象的列表，表示顺序执行的步骤。
以下示例中第一步、第二步、第N步操作都可能为其他类型的步骤。

```mermaid
graph LR
    Start(开始) --> Step1[执行第一步操作]
    Step1 --> Step2[执行第二步操作]
    Step2 --> StepN[执行第N步操作]
    StepN --> End(结束)
```

### 并行(parallel)

并行类型的步骤表示在工作流中同时执行多个操作。在 `XPComponentStep` 中，如果 `type` 字段为 "parallel"，则 `parallelSteps`
字段会包含一个 `XPComponentStep` 对象的列表，表示并行执行的步骤。

```mermaid
graph LR
    Start(开始) --> Step1[执行第一步操作]
    Start --> Step2[执行第二步操作]
    Start --> StepN[执行第N步操作]
    Step1 --> End(结束)
    Step2 --> End
    StepN --> End
```

### 条件(conditional)

条件类型的步骤表示在工作流中根据一定的条件选择执行不同的操作。在 `XPComponentStep` 中，如果 `type` 字段为 "conditional"
，则 `conditionSteps` 字段会包含一个 `XPConditionStep` 对象的列表，表示条件执行的步骤。
因原始 `org.jeasy.flows.workflow.ConditionalFlow`
未提供多条件实现，现重新定义多条件条件流，详情请看`com.xiaopeng.workflow.components.base.MulitConditionalFlow`。

```mermaid
graph TB
    Start(开始) --> PredicateCheck1{检查第一个条件}
    PredicateCheck1 -- 条件满足 --> Work1[执行第一个工作]
    Work1 --> End(结束)
    PredicateCheck1 -- 条件不满足 --> PredicateCheck2{检查第二个条件}
    PredicateCheck2 -- 条件满足 --> Work2[执行第二个工作]
    Work2 --> End
    PredicateCheck2 -- 条件不满足 --> PredicateCheckN{检查第N个条件}
    PredicateCheckN -- 条件满足 --> WorkN[执行第N个工作]
    WorkN --> End
    PredicateCheckN -- 条件不满足 --> OtherwiseWork[执行兜底工作]
    OtherwiseWork --> End
```

### 重复(repeat)

重复类型的步骤表示在工作流中重复执行一个操作。类似 do while形式，该类型，先执行工作单元，再判断是否继续repeat.
在 `XPComponentStep` 中，如果 `type` 字段为 "repeat"，则 `repeatStep`
字段会包含一个 `XPRepeatStep` 对象，表示重复执行的步骤。

```mermaid
graph LR
    Start(开始) --> Action(执行工作单元)
    Action -- 是否符合重复条件 --> Repeat
    Repeat -- 是 --> Action
    Repeat -- 否 --> End(结束)
```

# XPWorkFLowBuilder 类文档

## 概述

`XPWorkFLowBuilder` 类是 `com.xiaopeng.workflow.components` 包的一部分。它用于构建工作流，根据 `XPComponentStep`
的类型，选择不同的解析器进行解析。
XPWorkFLowBuilder 类为流程解析的总入口类，它会根据传入的 `XPComponentStep`
对象，选择不同的解析器进行解析。具体解析请看`com.xiaopeng.workflow.components.parser` 包下的相关实现。

# 使用示例

## 1. 顺序流

首先，我们需要创建一个简单的顺序流工作流。在这个例子中，我们将创建一个包含三个步骤的顺序流：初始化操作，获取词汇表，全局场景融合。每个步骤都由一个组件来执行。

以下是JSON字符串表示的工作流：

```json
{
  "name": "工作流",
  "type": "sequential",
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
      "name": "全局场景融合",
      "component": "GLOBAL_SCENE_FUSION"
    }
  ]
}
```

对应流程图如下：

```mermaid
graph LR
    A[初始化操作] --> B[获取词汇表]
    B --> C[全局场景融合]
```

这个流程图表示了工作流的执行顺序：首先执行初始化操作，然后获取词汇表，最后进行全局场景融合。

## UT

```shell
mvn clean test -D test=com.xiaopeng.workflow.HelloEasyFlowBpmnApplicationTests.testSimpleSequential -e

11:30:07.158 [main] INFO   - jsonStr:{"name":"工作流","type":"sequential","sequentialSteps":[{"name":"初始化操作","component":"INIT_ENV"},{"name":"获取词汇表","component":"RETRIEVE_VOCAB"},{"name":"全局场景融合","component":"GLOBAL_SCENE_FUSION"}]}
11:30:07.226 [main] INFO   - ===================> sequential 5Kgs4zJS build start <====================
11:30:07.226 [main] INFO   - build single component:INIT_ENV
11:30:07.226 [main] INFO   - build single component:RETRIEVE_VOCAB
11:30:07.227 [main] INFO   - build single component:GLOBAL_SCENE_FUSION
11:30:07.231 [main] INFO   - ===================> sequential 5Kgs4zJS flow build success, component info  ==> {"name":"工作流","type":"sequential","sequentialSteps":[{"name":"初始化操作","component":"INIT_ENV","type":"single"},{"name":"获取词汇表","component":"RETRIEVE_VOCAB","type":"single"},{"name":"全局场景融合","component":"GLOBAL_SCENE_FUSION","type":"single"}]} <===
11:30:07.243 [main] INFO   - Running workflow ''工作流''
11:30:07.243 [main] INFO   - INIT_ENV execute start
11:30:12.112 [main] INFO   - INIT_ENV execute end ==> cost time:4864ms
11:30:12.113 [main] INFO   - RETRIEVE_VOCAB execute start
11:30:16.621 [main] INFO   - RETRIEVE_VOCAB execute end ==> cost time:4503ms
11:30:16.621 [main] INFO   - globalSceneFusion execute start
11:30:18.534 [main] INFO   - GLOBAL_SCENE_FUSION execute end ==> cost time:1908ms
11:30:18.535 [main] INFO   - report:{"status":"COMPLETED","workContext":{}}
```

## 2. 简单并行流

首先，我们需要创建一个简单的并行流工作流。在这个例子中，我们将创建一个包含三个步骤的并行流：初始化操作，获取词汇表，全局场景融合。每个步骤都由一个组件来执行，但是这些步骤会并行执行。

以下是JSON字符串表示的工作流：

```json
{
  "name": "工作流",
  "type": "parallel",
  "parallelSteps": [
    {
      "name": "初始化操作",
      "component": "INIT_ENV"
    },
    {
      "name": "获取词汇表",
      "component": "RETRIEVE_VOCAB"
    },
    {
      "name": "全局场景融合",
      "component": "GLOBAL_SCENE_FUSION"
    }
  ]
}
```

对应流程图如下：

```mermaid
graph TB
    Start(开始) --> A[初始化操作]
    Start --> B[获取词汇表]
    Start --> C[全局场景融合]
    A --> End(结束)
    B --> End
    C --> End
```

这个流程图表示了工作流的执行顺序：初始化操作，获取词汇表，全局场景融合这三个步骤会并行执行。

## UT

```shell
mvn clean test -D test=com.xiaopeng.workflow.HelloEasyFlowBpmnApplicationTests.testSimpleParallel -e

11:31:32.547 [main] INFO   - jsonStr:{"name":"工作流","type":"parallel","parallelSteps":[{"name":"初始化操作","component":"INIT_ENV"},{"name":"获取词汇表","component":"RETRIEVE_VOCAB"},{"name":"全局场景融合","component":"GLOBAL_SCENE_FUSION"}]}
11:31:32.624 [main] INFO   - ===================> parallel UPxPSBi3 build start <====================
11:31:32.624 [main] INFO   - build single component:INIT_ENV
11:31:32.624 [main] INFO   - build single component:RETRIEVE_VOCAB
11:31:32.624 [main] INFO   - build single component:GLOBAL_SCENE_FUSION
11:31:32.628 [main] INFO   - ===================> parallel UPxPSBi3 flow build success, component info  ==> {"name":"工作流","type":"parallel","parallelSteps":[{"name":"初始化操作","component":"INIT_ENV","type":"single"},{"name":"获取词汇表","component":"RETRIEVE_VOCAB","type":"single"},{"name":"全局场景融合","component":"GLOBAL_SCENE_FUSION","type":"single"}]} <===
11:31:32.640 [main] INFO   - Running workflow ''工作流''
11:31:32.641 [pool-1-thread-1] INFO   - INIT_ENV execute start
11:31:32.641 [pool-1-thread-2] INFO   - RETRIEVE_VOCAB execute start
11:31:32.641 [pool-1-thread-3] INFO   - globalSceneFusion execute start
11:31:34.597 [pool-1-thread-2] INFO   - RETRIEVE_VOCAB execute end ==> cost time:1952ms
11:31:34.842 [pool-1-thread-3] INFO   - GLOBAL_SCENE_FUSION execute end ==> cost time:2197ms
11:31:35.162 [pool-1-thread-1] INFO   - INIT_ENV execute end ==> cost time:2518ms
11:31:35.164 [main] INFO   - report:{"reports":[{"status":"COMPLETED","workContext":{}},{"status":"COMPLETED","workContext":{}},{"status":"COMPLETED","workContext":{}}]}
```

## 3.多条件流

首先，我们需要创建一个多条件流工作流。在这个例子中，我们将创建一个包含三个个条件的多条件流：条件1和条件2
以下为JSON字符串表示的工作流：

```json
{
  "name": "工作流",
  "type": "sequential",
  "sequentialSteps": [
    {
      "name": "初始化操作",
      "component": "INIT_ENV"
    },
    {
      "name": "多条件流",
      "type": "conditional",
      "conditionSteps": [
        {
          "predicateClassName": "com.xiaopeng.workflow.components.predict.MulitPredicate.IF_RETRIEVE_VOCAB_CASE",
          "componentStep": {
            "name": "获取词汇表",
            "component": "RETRIEVE_VOCAB"
          }
        },
        {
          "predicateClassName": "com.xiaopeng.workflow.components.predict.MulitPredicate.IF_BERT_CRF_ENTITY_EXTRACTOR_CASE",
          "componentStep": {
            "name": "BERT_CRF_ENTITY_EXTRACTOR",
            "component": "BERT_CRF_ENTITY_EXTRACTOR"
          }
        },
        {
          "predicateClassName": "com.xiaopeng.workflow.components.predict.MulitPredicate.IF_TEMPLATE_QUERY_MATCHER_CASE",
          "componentStep": {
            "name": "TEMPLATE_QUERY_MATCHER",
            "component": "TEMPLATE_QUERY_MATCHER"
          }
        },
        {
          "conditionStep": 2,
          "componentStep": {
            "name": "LLM",
            "component": "LLM"
          }
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

对应流程图如下：

```mermaid
flowchart TD
    INIT_ENV
    subgraph multiCondition
        START --> PREDICT1{PREDICT1}
        START --> PREDICT2{PREDICT2}
        START --> PREDICT3{PREDICT3}
        PREDICT1 -- if hit --> RETRIEVE_VOCAB
        PREDICT2 -- elseif hit --> BERT_CRF_ENTITY_EXTRACTOR
        PREDICT3 -- elseif hit --> TEMPLATE_QUERY_MATCHER
        START -- else --> LLM
    end
    INIT_ENV --> multiCondition
    multiCondition --> GLOBAL_SCENE_FUSION
```

UT

```shell
mvn clean test -D test=com.xiaopeng.workflow.HelloEasyFlowBpmnApplicationTests.testSimpleMulitCondition -e

11:32:11.401 [main] INFO   - jsonStr:{"name":"工作流","type":"sequential","sequentialSteps":[{"name":"初始化操作","component":"INIT_ENV"},{"name":"多条件流","type":"conditional","conditionSteps":[{"predicateClassName":"com.xiaopeng.workflow.components.predict.MulitPredicate.IF_RETRIEVE_VOCAB_CASE","componentStep":{"name":"获取词汇表","component":"RETRIEVE_VOCAB"}},{"predicateClassName":"com.xiaopeng.workflow.components.predict.MulitPredicate.IF_BERT_CRF_ENTITY_EXTRACTOR_CASE","componentStep":{"name":"BERT_CRF_ENTITY_EXTRACTOR","component":"BERT_CRF_ENTITY_EXTRACTOR"}},{"predicateClassName":"com.xiaopeng.workflow.components.predict.MulitPredicate.IF_TEMPLATE_QUERY_MATCHER_CASE","componentStep":{"name":"TEMPLATE_QUERY_MATCHER","component":"TEMPLATE_QUERY_MATCHER"}},{"conditionStep":2,"componentStep":{"name":"LLM","component":"LLM"}}]},{"name":"全局场景融合","component":"GLOBAL_SCENE_FUSION"}]}
11:32:11.486 [main] INFO   - ===================> sequential pAmaBavk build start <====================
11:32:11.486 [main] INFO   - build single component:INIT_ENV
11:32:11.487 [main] INFO   - ===================> conditional b8eX0yYK build start <====================
11:32:11.488 [main] INFO   - build single component:RETRIEVE_VOCAB
11:32:11.499 [main] INFO   - build single component:BERT_CRF_ENTITY_EXTRACTOR
11:32:11.499 [main] INFO   - build single component:TEMPLATE_QUERY_MATCHER
11:32:11.499 [main] INFO   - build single component:LLM
11:32:11.505 [main] INFO   - ===================> conditional b8eX0yYK flow build success, component info  ==> {"name":"多条件流","type":"conditional","conditionSteps":[{"conditionStep":1,"predicateClassName":"com.xiaopeng.workflow.components.predict.MulitPredicate.IF_RETRIEVE_VOCAB_CASE","componentStep":{"name":"获取词汇表","component":"RETRIEVE_VOCAB","type":"single"}},{"conditionStep":1,"predicateClassName":"com.xiaopeng.workflow.components.predict.MulitPredicate.IF_BERT_CRF_ENTITY_EXTRACTOR_CASE","componentStep":{"name":"BERT_CRF_ENTITY_EXTRACTOR","component":"BERT_CRF_ENTITY_EXTRACTOR","type":"single"}},{"conditionStep":1,"predicateClassName":"com.xiaopeng.workflow.components.predict.MulitPredicate.IF_TEMPLATE_QUERY_MATCHER_CASE","componentStep":{"name":"TEMPLATE_QUERY_MATCHER","component":"TEMPLATE_QUERY_MATCHER","type":"single"}},{"conditionStep":2,"componentStep":{"name":"LLM","component":"LLM","type":"single"}}]} <===
11:32:11.506 [main] INFO   - build single component:GLOBAL_SCENE_FUSION
11:32:11.507 [main] INFO   - ===================> sequential pAmaBavk flow build success, component info  ==> {"name":"工作流","type":"sequential","sequentialSteps":[{"name":"初始化操作","component":"INIT_ENV","type":"single"},{"name":"多条件流","type":"conditional","conditionSteps":[{"conditionStep":1,"predicateClassName":"com.xiaopeng.workflow.components.predict.MulitPredicate.IF_RETRIEVE_VOCAB_CASE","componentStep":{"name":"获取词汇表","component":"RETRIEVE_VOCAB","type":"single"}},{"conditionStep":1,"predicateClassName":"com.xiaopeng.workflow.components.predict.MulitPredicate.IF_BERT_CRF_ENTITY_EXTRACTOR_CASE","componentStep":{"name":"BERT_CRF_ENTITY_EXTRACTOR","component":"BERT_CRF_ENTITY_EXTRACTOR","type":"single"}},{"conditionStep":1,"predicateClassName":"com.xiaopeng.workflow.components.predict.MulitPredicate.IF_TEMPLATE_QUERY_MATCHER_CASE","componentStep":{"name":"TEMPLATE_QUERY_MATCHER","component":"TEMPLATE_QUERY_MATCHER","type":"single"}},{"conditionStep":2,"componentStep":{"name":"LLM","component":"LLM","type":"single"}}]},{"name":"全局场景融合","component":"GLOBAL_SCENE_FUSION","type":"single"}]} <===
11:32:11.510 [main] INFO   - Running workflow ''工作流''
11:32:11.511 [main] INFO   - INIT_ENV execute start
11:32:12.373 [main] INFO   - INIT_ENV execute end ==> cost time:857ms
11:32:12.376 [main] INFO   - IF_RETRIEVE_VOCAB_CASE judge 是否命中分支条件 ==> false
11:32:12.376 [main] INFO   - IF_BERT_CRF_ENTITY_EXTRACTOR_CASE judge 是否命中分支条件 ==> false
11:32:12.376 [main] INFO   - IF_TEMPLATE_QUERY_MATCHER_CASE judge 是否命中分支条件 ==> false
11:32:12.376 [main] INFO   - LLM execute start
11:32:14.312 [main] INFO   - LLM execute end ==> cost time:1932ms
11:32:14.312 [main] INFO   - globalSceneFusion execute start
11:32:17.535 [main] INFO   - GLOBAL_SCENE_FUSION execute end ==> cost time:3219ms
11:32:17.536 [main] INFO   - report:{"status":"COMPLETED","workContext":{}}
```

## 4.重复流

首先，我们需要创建一个重复流工作流。在这个例子中，我们将创建一个包含一个重复流的工作流：重复单元为初始化操作单元。这个步骤步骤会重复执行3次。

以下是JSON字符串表示的工作流：

```json
{
  "name": "工作流",
  "type": "sequential",
  "sequentialSteps": [
    {
      "name": "重复3次",
      "type": "repeat",
      "repeatStep": {
        "predicateClassName": "com.xiaopeng.workflow.components.predict.MulitPredicate.REPEAT_PREDICATE",
        "componentStep": {
          "name": "INIT_ENV",
          "component": "INIT_ENV"
        }
      }
    }
  ]
}
```

对应流程图如下：

```mermaid
graph LR
    Start(开始) --> INIT_ENV(INIT_ENV)
    INIT_ENV -- 是否符合重复条件 --> REPEAT_PREDICATE
    REPEAT_PREDICATE -- 是 --> INIT_ENV
    REPEAT_PREDICATE -- 否 --> End(结束)
```

```shell
mvn clean test -D test=com.xiaopeng.workflow.HelloEasyFlowBpmnApplicationTests.testSimpleRepeatCase -e

11:32:57.833 [main] INFO   - jsonStr:{"name":"工作流","type":"sequential","sequentialSteps":[{"name":"重复3次","type":"repeat","repeatStep":{"predicateClassName":"com.xiaopeng.workflow.components.predict.MulitPredicate.REPEAT_PREDICATE","componentStep":{"name":"INIT_ENV","component":"INIT_ENV"}}}]}
11:32:57.927 [main] INFO   - ===================> sequential TIIgaJNZ build start <====================
11:32:57.927 [main] INFO   - ===================> repeat 5VgDqiqu build start <====================
11:32:57.939 [main] INFO   - build single component:INIT_ENV
11:32:57.945 [main] INFO   - ===================> repeat 5VgDqiqu flow build success, component info  ==> {"name":"重复3次","type":"repeat","repeatStep":{"predicateClassName":"com.xiaopeng.workflow.components.predict.MulitPredicate.REPEAT_PREDICATE","componentStep":{"name":"INIT_ENV","component":"INIT_ENV","type":"single"}}} <===
11:32:57.950 [main] INFO   - ===================> sequential TIIgaJNZ flow build success, component info  ==> {"name":"工作流","type":"sequential","sequentialSteps":[{"name":"重复3次","type":"repeat","repeatStep":{"predicateClassName":"com.xiaopeng.workflow.components.predict.MulitPredicate.REPEAT_PREDICATE","componentStep":{"name":"INIT_ENV","component":"INIT_ENV","type":"single"}}}]} <===
11:32:57.955 [main] INFO   - Running workflow ''工作流''
11:32:57.956 [main] INFO   - INIT_ENV execute start
11:32:59.660 [main] INFO   - INIT_ENV execute end ==> cost time:1701ms
11:32:59.661 [main] INFO   - repeat 次数 1
11:32:59.661 [main] INFO   - INIT_ENV execute start
11:33:00.452 [main] INFO   - INIT_ENV execute end ==> cost time:787ms
11:33:00.453 [main] INFO   - repeat 次数 2
11:33:00.453 [main] INFO   - INIT_ENV execute start
11:33:03.061 [main] INFO   - INIT_ENV execute end ==> cost time:2606ms
11:33:03.061 [main] INFO   - repeat 次数 3
11:33:03.062 [main] INFO   - report:{"status":"COMPLETED","workContext":{}}
```
package com.xiaopeng.workflow.components.parser;

import com.xiaopeng.workflow.components.XPComponentStep;
import org.jeasy.flows.workflow.WorkFlow;

import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * 组件步骤解析器
 */
@FunctionalInterface
public interface ComponentStepParser {
    WorkFlow parse(Map<String, WorkFlow> componentMap, XPComponentStep componentStep, ExecutorService threadPool);
}

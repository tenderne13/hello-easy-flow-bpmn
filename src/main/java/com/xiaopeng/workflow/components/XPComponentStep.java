package com.xiaopeng.workflow.components;

import com.xiaopeng.workflow.components.constants.FlowConstants;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;


/**
 * 编排组件
 */
@Data
@Slf4j
public class XPComponentStep {
    /**
     * 组件名称
     */
    private String name;
    /**
     * 组件标识
     */
    private String component;

    /**
     * 类型 single | sequential | parallel | conditional | repeat
     */
    private String type = "single";

    /**
     * 多条件步骤
     */
    private List<XPConditionStep> conditionSteps;

    /**
     * 重复执行步骤
     */
    private XPRepeatStep repeatStep;


    /**
     * 串行流步骤
     */
    private List<XPComponentStep> sequentialSteps;
    /**
     * 并行流步骤
     */
    private List<XPComponentStep> parallelSteps;
}

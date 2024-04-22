package com.xiaopeng.workflow.components;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;


/**
 * 编排组件
 */
@Data
@Slf4j
@Builder
@AllArgsConstructor
@NoArgsConstructor
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

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
     * 条件流step 标识
     * 1：then
     * 2: otherwise
     */
    private Integer conditionStep = FlowConstants.THEN_STEP;

    /**
     * 类型 single | sequential | parallel | conditional | loop
     */
    private String type = "single";

    /**
     * 条件流的条件类名 类型为条件流 | 循环流 为必填
     */
    private String predicateClassName;


    /**
     * 串行流步骤
     */
    private List<XPComponentStep> sequentialSteps;
    /**
     * 并行流步骤
     */
    private List<XPComponentStep> parallelSteps;
}

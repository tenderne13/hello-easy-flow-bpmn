package com.xiaopeng.workflow.components;

import com.xiaopeng.workflow.components.constants.FlowConstants;
import lombok.Data;

@Data
public class XPConditionStep {

    /**
     * 条件流step 标识
     * 1：then
     * 2: otherwise
     */
    private Integer conditionStep = FlowConstants.THEN_STEP;

    /**
     * 用于判断是否命中条件
     */
    private String predicateClassName;

    /**
     * 命中条件时执行的WorkFlow 配置
     */
    private XPComponentStep componentStep;
}

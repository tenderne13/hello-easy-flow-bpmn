package com.xiaopeng.workflow.components;

import lombok.Data;

/**
 * 重复执行步骤配置
 */
@Data
public class XPRepeatStep {

    /**
     * 用于判断是否命中条件
     */
    private String predicateClassName;

    /**
     * 命中条件时执行的WorkFlow 配置
     */
    private XPComponentStep componentStep;
}

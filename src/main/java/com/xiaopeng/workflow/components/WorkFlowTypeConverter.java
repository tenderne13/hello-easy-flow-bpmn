package com.xiaopeng.workflow.components;

import cn.hutool.core.convert.Converter;

public class WorkFlowTypeConverter implements Converter<WorkFlowType> {
    @Override
    public WorkFlowType convert(Object value, WorkFlowType defaultValue) throws IllegalArgumentException {
        if (value instanceof String) {
            return WorkFlowType.valueOf((String) value);
        }
        return defaultValue;
    }
}
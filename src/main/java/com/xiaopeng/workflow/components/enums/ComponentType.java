package com.xiaopeng.workflow.components.enums;

import lombok.Getter;

public enum ComponentType {
    SINGLE("single", "单一组件"),
    PARALLEL("parallel", "并行组件"),
    SEQUENTIAL("sequential", "顺序组件"),
    CONDITIONAL("conditional", "条件组件"),
    REPEAT("repeat", "重复组件");

    @Getter
    private final String code;
    @Getter
    private final String desc;

    ComponentType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}

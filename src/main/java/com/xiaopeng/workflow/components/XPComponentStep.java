package com.xiaopeng.workflow.components;

import cn.hutool.core.collection.CollectionUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;


/**
 * 编排组件
 */
@Data
@Slf4j
public class XPComponentStep {
    private String name;
    private String component;
    private List<XPComponentStep> sequentialSteps;
    private List<XPComponentStep> parallelSteps;
    private String type = "single";

    public XPComponentStep() {
    }

    public XPComponentStep(String name, String component, List<XPComponentStep> sequentialSteps, List<XPComponentStep> parallelSteps, String type) {
        this.name = name;
        this.component = component;
        this.sequentialSteps = sequentialSteps;
        this.parallelSteps = parallelSteps;
        this.setType();
    }

    public void setType() {
        if (CollectionUtil.isNotEmpty(sequentialSteps)) {
            this.type = "sequential";
        } else if (CollectionUtil.isNotEmpty(parallelSteps)) {
            this.type = "parallel";
        }
    }

    public String getType() {
        if (CollectionUtil.isNotEmpty(sequentialSteps)) {
            this.type = "sequential";
        } else if (CollectionUtil.isNotEmpty(parallelSteps)) {
            this.type = "parallel";
        }
        return type;
    }
}

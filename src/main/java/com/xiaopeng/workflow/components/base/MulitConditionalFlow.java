package com.xiaopeng.workflow.components.base;

import com.xiaopeng.workflow.components.XPConditionStep;
import lombok.extern.slf4j.Slf4j;
import org.jeasy.flows.work.Work;
import org.jeasy.flows.work.WorkContext;
import org.jeasy.flows.work.WorkReport;
import org.jeasy.flows.work.WorkReportPredicate;
import org.jeasy.flows.workflow.WorkFlow;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 多条件工作流
 */
@Slf4j
public class MulitConditionalFlow implements WorkFlow {

    private List<Map<WorkReportPredicate, Work>> thenWorkList = new ArrayList<>();
    private Work oherWishWork = null;

    public MulitConditionalFlow(String name, List<XPConditionStep> conditionSteps) {

    }

    @Override
    public WorkReport execute(WorkContext workContext) {
        return null;
    }
}

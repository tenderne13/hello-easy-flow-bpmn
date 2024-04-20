package com.xiaopeng.workflow.components.predict;

import lombok.extern.slf4j.Slf4j;
import org.jeasy.flows.work.WorkReport;
import org.jeasy.flows.work.WorkReportPredicate;

@Slf4j
public class XGPTSwitchPredicate implements WorkReportPredicate {

    @Override
    public boolean apply(WorkReport workReport) {
        Object flag = workReport.getWorkContext().get("XGPTSwitch");
        log.info("XgptSwitchPredicate 大模型开关开启状态 ==> {}", flag);
        return Boolean.TRUE.equals(flag);
    }
}

package com.xiaopeng.workflow.components.predict;

import lombok.extern.slf4j.Slf4j;
import org.jeasy.flows.work.WorkReport;
import org.jeasy.flows.work.WorkReportPredicate;

@Slf4j
public class XGPTSwitchPredicate implements WorkReportPredicate {

    public WorkReportPredicate  CASE1  = workReport -> {
        Object flag = workReport.getWorkContext().get("XGPTSwitch");
        log.info("测试@号是否能被实例化 ==> {}", flag);
        return Boolean.TRUE.equals(flag);
    };

    @Override
    public boolean apply(WorkReport workReport) {
        Object flag = workReport.getWorkContext().get("XGPTSwitch");
        log.info("XgptSwitchPredicate 大模型开关开启状态 ==> {}", flag);
        return Boolean.TRUE.equals(flag);
    }
}

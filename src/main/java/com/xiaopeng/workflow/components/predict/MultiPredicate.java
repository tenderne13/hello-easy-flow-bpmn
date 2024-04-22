package com.xiaopeng.workflow.components.predict;

import com.xiaopeng.workflow.components.XPConditionStep;
import lombok.extern.slf4j.Slf4j;
import org.jeasy.flows.work.WorkContext;
import org.jeasy.flows.work.WorkReportPredicate;

/**
 * 多条件判断 用于单个节点IF ELSEIF ELSE
 * <p>
 * 使用示例
 * 在 {@link XPConditionStep} 中使用
 * 使用方法1
 * 会尝试去实例化该类（此类需实现WorkReportPredicate 接口）
 * 示例 ：predicateClassName = "com.xiaopeng.workflow.components.predict.XGPTSwitchPredicate"
 * 使用方法2 某个类中的属性
 * 示例 ： predicateClassName = "com.xiaopeng.workflow.components.predict.MultiPredicate.IF_COMPONENT_V_CASE"
 *
 *
 * <p>
 */
@Slf4j
public class MultiPredicate {

    public WorkReportPredicate IF_COMPONENT_V_CASE = workReport -> {
        Object flag = workReport.getWorkContext().get("conditionPath");
        boolean equals = "COMPONENT_V".equals(flag);
        log.info("IF_COMPONENT_V_CASE judge 是否命中分支条件 ==> {}", equals);
        return equals;
    };

    public WorkReportPredicate IF_COMPONENT_BE_CASE = workReport -> {
        Object flag = workReport.getWorkContext().get("conditionPath");
        boolean equals = "COMPONENT_BE".equals(flag);
        log.info("IF_COMPONENT_BE_CASE judge 是否命中分支条件 ==> {}", equals);
        return equals;
    };

    public WorkReportPredicate IF_COMPONENT_QM_CASE = workReport -> {
        Object flag = workReport.getWorkContext().get("conditionPath");
        boolean equals = "COMPONENT_QM".equals(flag);
        log.info("IF_COMPONENT_QM_CASE judge 是否命中分支条件 ==> {}", equals);
        return equals;
    };


    public WorkReportPredicate REPEAT_PREDICATE = workReport -> {
        WorkContext workContext = workReport.getWorkContext();
        Object ott = workContext.get("times");
        int tt;
        if (ott == null) {
            tt = 1;
        } else {
            tt = (int) ott + 1;
        }
        workContext.put("times", tt);
        log.info("repeat 次数 {}", tt);
        return tt <= 2;
    };
}

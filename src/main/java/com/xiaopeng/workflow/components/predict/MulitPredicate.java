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
 * 示例 ： predicateClassName = "com.xiaopeng.workflow.components.predict.MulitPredicate@IF_RETRIEVE_VOCAB_CASE"
 *
 *
 * <p>
 */
@Slf4j
public class MulitPredicate {


    public WorkReportPredicate IF_RETRIEVE_VOCAB_CASE = workReport -> {
        Object flag = workReport.getWorkContext().get("conditionPath");
        boolean equals = "RETRIEVE_VOCAB".equals(flag);
        log.info("IF_RETRIEVE_VOCAB_CASE judge 是否命中分支条件 ==> {}", equals);
        return equals;
    };

    public WorkReportPredicate IF_BERT_CRF_ENTITY_EXTRACTOR_CASE = workReport -> {
        Object flag = workReport.getWorkContext().get("conditionPath");
        boolean equals = "BERT_CRF_ENTITY_EXTRACTOR".equals(flag);
        log.info("IF_BERT_CRF_ENTITY_EXTRACTOR_CASE judge 是否命中分支条件 ==> {}", equals);
        return equals;
    };

    public WorkReportPredicate IF_TEMPLATE_QUERY_MATCHER_CASE = workReport -> {
        Object flag = workReport.getWorkContext().get("conditionPath");
        boolean equals = "TEMPLATE_QUERY_MATCHER".equals(flag);
        log.info("IF_TEMPLATE_QUERY_MATCHER_CASE judge 是否命中分支条件 ==> {}", equals);
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

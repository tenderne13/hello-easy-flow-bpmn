package com.xiaopeng.workflow.components.base;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jeasy.flows.work.*;
import org.jeasy.flows.workflow.WorkFlow;

import java.util.ArrayList;
import java.util.List;


/**
 * 多条件工作流
 */
@Slf4j
public class MulitConditionalFlow implements WorkFlow {

    private List<PredictWorkPair> thenWorkList = new ArrayList<>();
    private Work otherWishWork = new NoOpWork();

    public MulitConditionalFlow(List<PredictWorkPair> thenWorkList, Work otherWishWork) {
        if (thenWorkList != null) {
            this.thenWorkList = thenWorkList;
        }
        if (otherWishWork != null) {
            this.otherWishWork = otherWishWork;
        }
    }

    @Override
    public WorkReport execute(WorkContext workContext) {
        WorkReport workReport = new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
        for (PredictWorkPair predictWorkPair : thenWorkList) {
            WorkReportPredicate predicate = predictWorkPair.getPredicate();
            Work work = predictWorkPair.getWork();
            if (predicate.apply(workReport)) {
                return work.execute(workContext);
            }
        }
        if (otherWishWork != null) {
            return otherWishWork.execute(workContext);
        }

        return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
    }

    @Builder
    @Data
    public static class PredictWorkPair {
        private WorkReportPredicate predicate;
        private Work work;

        public PredictWorkPair(WorkReportPredicate predicate, Work work) {
            this.predicate = predicate;
            this.work = work;
        }
    }
}

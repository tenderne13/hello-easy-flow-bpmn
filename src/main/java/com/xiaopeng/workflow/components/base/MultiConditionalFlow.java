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
public class MultiConditionalFlow implements WorkFlow {

    private List<PredictWorkPair> thenWorkList = new ArrayList<>();
    private Work otherWishWork = new NoOpWork();

    public MultiConditionalFlow(List<PredictWorkPair> thenWorkList, Work otherWishWork) {
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
                try {
                    return work.execute(workContext);
                } catch (Exception e) {
                    log.error("MultiConditionalFlow execute error", e);
                    return new DefaultWorkReport(WorkStatus.FAILED, workContext);
                }
            }
        }
        if (otherWishWork != null) {
            return otherWishWork.execute(workContext);
        }

        return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
    }

    @Data
    public static class PredictWorkPair {
        private WorkReportPredicate predicate;
        private Work work;

        public PredictWorkPair(WorkReportPredicate predicate, Work work) {
            this.predicate = predicate;
            this.work = work;
        }
    }


    public static class Builder {

        private Builder() {

        }

        public static ThenWorkListStep aNewMultiConditionalFlow() {
            return new BuildSteps();
        }

        public interface ThenWorkListStep {
            OtherWishWorkStep withThenWorkList(List<PredictWorkPair> thenWorkList);
        }

        public interface OtherWishWorkStep {
            BuildStep withOtherWishWork(Work otherWishWork);
        }

        public interface BuildStep {
            MultiConditionalFlow build();
        }

        private static class BuildSteps implements ThenWorkListStep, OtherWishWorkStep, BuildStep {

            private List<PredictWorkPair> thenWorkList;
            private Work otherWishWork;

            @Override
            public OtherWishWorkStep withThenWorkList(List<PredictWorkPair> thenWorkList) {
                this.thenWorkList = thenWorkList;
                return this;
            }

            @Override
            public BuildStep withOtherWishWork(Work otherWishWork) {
                this.otherWishWork = otherWishWork;
                return this;
            }

            @Override
            public MultiConditionalFlow build() {
                return new MultiConditionalFlow(this.thenWorkList, this.otherWishWork);
            }
        }
    }
}

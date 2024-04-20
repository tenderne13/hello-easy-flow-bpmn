package com.xiaopeng.workflow.components.factory;

import org.jeasy.flows.work.DefaultWorkReport;
import org.jeasy.flows.work.WorkContext;
import org.jeasy.flows.work.WorkReportPredicate;
import org.jeasy.flows.work.WorkStatus;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

/**
 * WorkReportPredicate 工厂类
 */
public class WorkReportPredicateFactory {
    private static final String regixSplit = "@";

    public static WorkReportPredicate createPredicate(String className) {
        try {
            //className 若包含 S 号，则表示是一个静态字段，需要通过反射获取
            if (className.contains(regixSplit)) {
                String[] split = className.split(regixSplit);
                return getPredicateFromClassName(split[0], split[1]);
            }
            Class<?> clazz = Class.forName(className);
            return (WorkReportPredicate) clazz.getDeclaredConstructor().newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException |
                 InvocationTargetException e) {
            throw new RuntimeException("Failed to create WorkReportPredicate instance for class: " + className, e);
        }
    }

    public static WorkReportPredicate getPredicateFromClassName(String className, String fieldName) {
        try {
            Class<?> clazz = Class.forName(className);
            Object instance = clazz.getDeclaredConstructor().newInstance();
            Field field = clazz.getField(fieldName);
            return (WorkReportPredicate) field.get(instance);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get WorkReportPredicate instance from class: " + className, e);
        }
    }

    public static void main(String[] args) {
        String[] split = "到嘞@到嘞".split("@");
        System.out.println(split);
        WorkReportPredicate predicate = WorkReportPredicateFactory.createPredicate("com.xiaopeng.workflow.components.predict.XGPTSwitchPredicate@CASE1");
        predicate.apply(new DefaultWorkReport(WorkStatus.COMPLETED,new WorkContext()));
        System.out.println(predicate);
    }
}

package com.xiaopeng.workflow.components.factory;

import org.jeasy.flows.work.WorkReportPredicate;

import java.lang.reflect.InvocationTargetException;

/**
 * WorkReportPredicate 工厂类
 */
public class WorkReportPredicateFactory {
    public static WorkReportPredicate createPredicate(String className) {
        try {
            Class<?> clazz = Class.forName(className);
            return (WorkReportPredicate) clazz.getDeclaredConstructor().newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException |
                 InvocationTargetException e) {
            throw new RuntimeException("Failed to create WorkReportPredicate instance for class: " + className, e);
        }
    }
}

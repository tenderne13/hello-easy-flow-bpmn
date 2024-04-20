package com.xiaopeng.workflow.components.factory;

import org.jeasy.flows.work.WorkReportPredicate;

import java.lang.reflect.Field;

/**
 * WorkReportPredicate 工厂类
 */
public class WorkReportPredicateFactory {

    public static WorkReportPredicate createPredicate(String className) {
        try {
            Class<?> clazz = Class.forName(className);
            return (WorkReportPredicate) clazz.getDeclaredConstructor().newInstance();
        } catch (ClassNotFoundException classNotFoundException) {
            //如果类不存在，则尝试去获取静态字段
            int lastDotIndex = className.lastIndexOf('.');
            if (lastDotIndex != -1) {
                String realClassName = className.substring(0, lastDotIndex);
                String fieldName = className.substring(lastDotIndex + 1);
                return getPredicateByField(realClassName, fieldName);
            }
            throw new RuntimeException("Failed to create WorkReportPredicate instance from class: " + className, classNotFoundException);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create WorkReportPredicate instance for class: " + className, e);
        }
    }

    public static WorkReportPredicate getPredicateByField(String className, String fieldName) {
        try {
            Class<?> clazz = Class.forName(className);
            Object instance = clazz.getDeclaredConstructor().newInstance();
            Field field = clazz.getField(fieldName);
            return (WorkReportPredicate) field.get(instance);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get WorkReportPredicate instance from class: " + className, e);
        }
    }
}

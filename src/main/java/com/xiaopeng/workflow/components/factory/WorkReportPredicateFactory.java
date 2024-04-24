package com.xiaopeng.workflow.components.factory;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.json.JSONUtil;
import com.xiaopeng.workflow.components.ParamTypeValuePair;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.jeasy.flows.work.WorkReportPredicate;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * WorkReportPredicate 工厂类
 */
@Slf4j
public class WorkReportPredicateFactory {


    public static WorkReportPredicate createPredicate(String predicateClassName, List<ParamTypeValuePair> predicateParameterTypes) {
        Pair<List<Class<?>>, List> parameterTypesAndValues = getParameterTypesAndValues(predicateParameterTypes);
        try {
            Class<?> predicateClass = Class.forName(predicateClassName);
            Constructor<?> constructor = predicateClass.getConstructor(parameterTypesAndValues.getLeft().toArray(new Class[0]));
            return (WorkReportPredicate) constructor.newInstance(parameterTypesAndValues.getRight().toArray());
        }catch (ClassNotFoundException classNotFoundException) {
            //如果类不存在，则尝试去获取静态字段
            int lastDotIndex = predicateClassName.lastIndexOf('.');
            if (lastDotIndex != -1) {
                String realClassName = predicateClassName.substring(0, lastDotIndex);
                String fieldName = predicateClassName.substring(lastDotIndex + 1);
                return getPredicateByField(realClassName, fieldName);
            }
            throw new RuntimeException("Failed to create WorkReportPredicate instance from class: " + predicateClassName, classNotFoundException);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException |
                 InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public static Pair<List<Class<?>>, List> getParameterTypesAndValues(List<ParamTypeValuePair> predicateParameterTypes) {
        List<Class<?>> parameterTypesClasses = CollectionUtil.newArrayList();
        List parameters = CollectionUtil.newArrayList();
        if (CollectionUtil.isNotEmpty(predicateParameterTypes)) {
            for (ParamTypeValuePair paramTypeValuePair : predicateParameterTypes) {
                try {
                    Class<?> clazz = Class.forName(paramTypeValuePair.getClassName());
                    //构造方法入参类型
                    parameterTypesClasses.add(clazz);
                    //构造方法入参值
                    parameters.add(convertValueToType(paramTypeValuePair));
                } catch (Exception e) {
                    throw new RuntimeException("Failed to create WorkReportPredicate instance from class: " + paramTypeValuePair.getClassName(), e);
                }
            }
        }
        return Pair.of(parameterTypesClasses, parameters);
    }

    public static Object convertValueToType(ParamTypeValuePair paramTypeValuePair) {
        String className = paramTypeValuePair.getClassName();
        Object value = paramTypeValuePair.getValue();

        try {
            Class<?> clazz = Class.forName(className);
            if (value instanceof String) {
                Method valueOfMethod = clazz.getMethod("valueOf", String.class);
                return valueOfMethod.invoke(null, value);
            } else if (value.getClass().equals(clazz)) {
                return value;
            } else {
                //转换复杂的对象
                log.info("start convert {} to type {}", value, className);
                return JSONUtil.toBean(JSONUtil.toJsonStr(value), clazz);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert value to type: " + className, e);
        }
    }

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

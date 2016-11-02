package com.qgutech.fs.utils;

import org.apache.log4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

public final class ReflectUtil {
    public static Logger LOG = Logger.getLogger(ReflectUtil.class);

    private ReflectUtil() {
        // NP
    }

    /**
     * 通过字段对象和实体对象获取字段的值
     *
     * @param field 字段
     * @param obj   实体对象
     * @return 字段的值
     */
    public static Object getFieldValue(Field field, Object obj) {
        if (field == null || obj == null) {
            throw new IllegalArgumentException("Can query field (" + field
                    + ") from object (" + obj + ")!");
        }

        try {
            field.setAccessible(true);
            return field.get(obj);
        } catch (Exception e) {
            LOG.error("Can't get value by reflect!", e);
        }

        return null;
    }

    /**
     * 该方法主要是把一些数据库的类型适配成合适的Java类型。目前只适用于支持枚举。
     */
    public static Object adapterValue(Field field, Object value) {
        if (value == null) {
            return null;
        }

        Class<?> fieldType = field.getType();
        if (fieldType.isEnum()) {
            value = Enum.valueOf((Class<? extends Enum>) fieldType, value.toString());
        }

        return value;
    }

    /**
     * 将值保存到实体类的字段中
     *
     * @param field 字段
     * @param obj   实体类
     * @param value 值
     * @return 成功标志
     */
    public static boolean setFieldValue(Field field, Object obj, Object value) {
        if (field == null || obj == null) {
            throw new IllegalArgumentException("Can query field (" + field + ") from object (" + obj + ")!");
        }

        value = adapterValue(field, value);

        try {
            field.setAccessible(true);
            field.set(obj, value);
            return true;
        } catch (Exception e) {
            LOG.error("Can't set value by reflect!", e);
            throw new RuntimeException(e);
        }
    }

    public static Object createInstance(Class<?> objectClass) {

        if (objectClass == null) {
            throw new IllegalArgumentException("Object class mustn't be null");
        }

        try {
            return objectClass.newInstance();
        } catch (Exception e) {
            LOG.error("Can't create instance  by reflect!", e);
        }

        return null;
    }

    public static Class<?> getClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Can't find class from name: " + className);
        }
    }

    public static <T> T newInstance(Class<T> objectClass) {

        if (objectClass == null) {
            throw new IllegalArgumentException("Object class mustn't be null");
        }

        try {
            return objectClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Can't create instance by reflect!", e);
        }
    }

    /**
     * 该方法递归的过去当前类及父类中声明的字段。最终结果以list形式返回。
     *
     * @param objClass 开始查询的类别.
     * @return List形式的结果。
     */
    public static List<Field> getFields(Class<?> objClass) {

        if (objClass == null) {
            return null;
        }

        List<Field> fields = new ArrayList<Field>(15);
        Field[] classFields = objClass.getDeclaredFields();
        fields.addAll(Arrays.asList(classFields));

        Class<?> superclass = objClass.getSuperclass();
        if (superclass != null) {
            List<Field> superClassFields = getFields(superclass);
            fields.addAll(superClassFields);
        }

        return fields;
    }

    /**
     * 这个方法用于通过名称查找一个实体的字段属性
     *
     * @param objClass  需要查找的对象。
     * @param fieldName 需要查找的属性字段名称
     * @return 该类对应名称的属性或者null，如果没有该名称的属性。
     */
    public static Field getField(Class<?> objClass, String fieldName) {

        List<Field> fields = ReflectUtil.getFields(objClass);
        for (Field field : fields) {
            String name = field.getName();
            if (name.equals(fieldName)) {
                return field;
            }
        }

        return null;
    }

    /**
     * 找到继承链中所有类声明的属性，如果父类和子类有同名的属性，子类的属性优先获取。
     *
     * @param clazz 要分析获取的当前类
     * @return 所有类声明的属性
     */
    public static Map<String, Field> getClassFields(Class<?> clazz) {
        Class<?> tempClazz = clazz;
        List<Class<?>> clazzChain = new ArrayList<Class<?>>();
        while (tempClazz != null && tempClazz != Object.class) {
            clazzChain.add(tempClazz);
            tempClazz = tempClazz.getSuperclass();
        }

        Map<String, Field> fieldMap = new HashMap<String, Field>();
        for (int i = clazzChain.size() - 1; i >= 0; i--) {
            tempClazz = clazzChain.get(i);
            Field[] declaredFields = tempClazz.getDeclaredFields();
            for (Field field : declaredFields) {
                fieldMap.put(field.getName(), field);
            }
        }

        return fieldMap;
    }

    public static <T> Class<T> getGenericParamClass(Class<?> clazz) {
        ParameterizedType genericSuperclass = (ParameterizedType) clazz.getGenericSuperclass();
        Type[] actualTypeArguments = genericSuperclass.getActualTypeArguments();
        Class<T> typeArgument = (Class<T>) actualTypeArguments[0];
        return typeArgument;
    }
}

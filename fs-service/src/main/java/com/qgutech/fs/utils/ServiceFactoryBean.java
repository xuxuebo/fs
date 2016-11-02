package com.qgutech.fs.utils;


import com.qgutech.fs.service.FileServerService;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Method;

public class ServiceFactoryBean implements MethodInterceptor, InitializingBean, FactoryBean<Object> {
    private Object serviceProxy;
    private ClassLoader beanClassLoader = ClassUtils.getDefaultClassLoader();
    private Object service;
    private Class<?> serviceInterface;

    @Override
    public Object getObject() throws Exception {
        return this.serviceProxy;
    }

    @Override
    public Class<?> getObjectType() {
        return this.serviceInterface;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.serviceProxy = new ProxyFactory(FileServerService.class, this)
                .getProxy(beanClassLoader);
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        AfterMethod afterAnnotation = null;
        BeforeMethod beforeAnnotation = null;
        Class<?> clazz = getService().getClass();
        Object[] arguments = invocation.getArguments();
        try {
            Method method = invocation.getMethod();
            NoBeforeMethod noBeforeMethod = method.getAnnotation(NoBeforeMethod.class);
            if (noBeforeMethod == null) {
                beforeAnnotation = method.getAnnotation(BeforeMethod.class);
                if (beforeAnnotation == null) {
                    beforeAnnotation = clazz.getAnnotation(BeforeMethod.class);
                }
            }

            NoAfterMethod noAfterMethod = method.getAnnotation(NoAfterMethod.class);
            if (noAfterMethod == null) {
                afterAnnotation = method.getAnnotation(AfterMethod.class);
                if (afterAnnotation == null) {
                    afterAnnotation = clazz.getAnnotation(AfterMethod.class);
                }
            }

            executeBeforeMethod(beforeAnnotation, clazz, arguments);
            return method.invoke(getService(), arguments);
        } finally {
            executeAfterMethod(afterAnnotation, clazz, arguments);
        }
    }

    private void executeBeforeMethod(BeforeMethod beforeAnnotation, Class<?> clazz
            , Object[] arguments) throws Exception {
        if (beforeAnnotation == null) {
            return;
        }

        String name = beforeAnnotation.name();
        if (name == null || name.trim().length() <= 0) {
            return;
        }

        name = name.trim();
        Class<?>[] parameters = beforeAnnotation.parameters();
        if (parameters == null || parameters.length == 0) {
            Method beforeMethod = clazz.getMethod(name);
            if (beforeMethod != null) {
                beforeMethod.invoke(getService());
            }
            return;
        }

        if (arguments != null && arguments.length != 0
                && parameters.length <= arguments.length) {
            Method beforeMethod = clazz.getMethod(name, parameters);
            if (beforeMethod != null) {
                Object[] args = new Object[parameters.length];
                System.arraycopy(arguments, 0, args, 0, parameters.length);
                beforeMethod.invoke(getService(), args);
            }
        }
    }

    private void executeAfterMethod(AfterMethod afterAnnotation, Class<?> clazz
            , Object[] arguments) throws Exception {
        if (afterAnnotation == null) {
            return;
        }

        String name = afterAnnotation.name();
        if (name == null || name.trim().length() <= 0) {
            return;
        }

        name = name.trim();
        Class<?>[] parameters = afterAnnotation.parameters();
        Method afterMethod;
        if (parameters == null || parameters.length == 0) {
            afterMethod = clazz.getMethod(name);
            if (afterMethod != null) {
                afterMethod.invoke(getService());
            }

            return;
        }

        if (arguments != null && arguments.length != 0
                && parameters.length <= arguments.length) {
            afterMethod = clazz.getMethod(name, parameters);
            if (afterMethod != null) {
                Object[] args = new Object[parameters.length];
                System.arraycopy(arguments, 0, args, 0, parameters.length);
                afterMethod.invoke(getService(), args);
            }
        }
    }

    public Object getService() {
        return service;
    }

    public void setService(Object service) {
        this.service = service;
    }

    public Class<?> getServiceInterface() {
        return serviceInterface;
    }

    public void setServiceInterface(Class<?> serviceInterface) {
        this.serviceInterface = serviceInterface;
    }
}

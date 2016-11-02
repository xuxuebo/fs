package com.qgutech.fs.utils;


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
    private Class<?> serviceClass;

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
        this.serviceProxy = new ProxyFactory(this.serviceInterface, this)
                .getProxy(this.beanClassLoader);
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        AfterMethod afterAnnotation = null;
        BeforeMethod beforeAnnotation = null;
        Object[] arguments = invocation.getArguments();
        try {
            Method method = invocation.getMethod();
            Method actualMethod = this.serviceClass.getMethod(method.getName(), method.getParameterTypes());
            NoBeforeMethod noBeforeMethod = actualMethod.getAnnotation(NoBeforeMethod.class);
            if (noBeforeMethod == null) {
                beforeAnnotation = actualMethod.getAnnotation(BeforeMethod.class);
                if (beforeAnnotation == null) {
                    beforeAnnotation = this.serviceClass.getAnnotation(BeforeMethod.class);
                }
            }

            NoAfterMethod noAfterMethod = actualMethod.getAnnotation(NoAfterMethod.class);
            if (noAfterMethod == null) {
                afterAnnotation = actualMethod.getAnnotation(AfterMethod.class);
                if (afterAnnotation == null) {
                    afterAnnotation = this.serviceClass.getAnnotation(AfterMethod.class);
                }
            }

            executeBeforeMethod(beforeAnnotation, arguments);
            return method.invoke(getService(), arguments);
        } finally {
            executeAfterMethod(afterAnnotation, arguments);
        }
    }

    private void executeBeforeMethod(BeforeMethod beforeAnnotation, Object[] arguments) throws Exception {
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
            Method beforeMethod = this.serviceClass.getMethod(name);
            if (beforeMethod != null) {
                beforeMethod.invoke(getService());
            }
            return;
        }

        if (arguments != null && arguments.length != 0
                && parameters.length <= arguments.length) {
            Method beforeMethod = this.serviceClass.getMethod(name, parameters);
            if (beforeMethod != null) {
                Object[] args = new Object[parameters.length];
                System.arraycopy(arguments, 0, args, 0, parameters.length);
                beforeMethod.invoke(getService(), args);
            }
        }
    }

    private void executeAfterMethod(AfterMethod afterAnnotation, Object[] arguments) throws Exception {
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
            afterMethod = this.serviceClass.getMethod(name);
            if (afterMethod != null) {
                afterMethod.invoke(getService());
            }

            return;
        }

        if (arguments != null && arguments.length != 0
                && parameters.length <= arguments.length) {
            afterMethod = this.serviceClass.getMethod(name, parameters);
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

    public Class<?> getServiceClass() {
        return serviceClass;
    }

    public void setServiceClass(Class<?> serviceClass) {
        this.serviceClass = serviceClass;
    }
}

package com.qgutech.fs.utils;

import com.google.gson.Gson;
import org.springframework.web.context.ContextLoaderListener;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.*;

public class RemoteRequest {

    private static final Gson gson = new Gson();

    private String interfaceName;

    private String methodName;

    private Class<?>[] parameterTypes;

    private List<String> arguments;

    private Map<String, String> attributes;

    private Long timestamp;

    private String sign;

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Class<?>[] getParameterTypes() {
        return parameterTypes;
    }

    public void setParameterTypes(Class<?>[] parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    public List<String> getArguments() {
        return arguments;
    }

    public void setArguments(List<String> arguments) {
        this.arguments = arguments;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    protected boolean checkSign() {
        if (System.currentTimeMillis() - getTimestamp() > PropertiesUtils.getMaxWaitForRequest()) {
            return false;
        }

        String newSign = Signer.md5(getInterfaceName()
                + FsConstants.VERTICAL_LINE + getMethodName()
                + FsConstants.VERTICAL_LINE + getTimestamp()
                + FsConstants.VERTICAL_LINE + getTimestamp()
                + FsConstants.VERTICAL_LINE + getMethodName()
                + FsConstants.VERTICAL_LINE + getInterfaceName());

        return newSign.equals(getSign());
    }

    public RemoteResponse invoke() throws Exception {
        RemoteResponse remoteResponse = new RemoteResponse();
        if (!checkSign()) {
            remoteResponse.setExceptionOccurs(true);
            remoteResponse.setContent(new RuntimeException("Request invalid!"));
            return remoteResponse;
        }

        try {
            ExecutionContext.setContextMap(getAttributes());
            Class clazz = Class.forName(getInterfaceName());
            Object targetService = ContextLoaderListener.getCurrentWebApplicationContext().getBean(clazz);
            Class<?>[] parameterTypes = getParameterTypes();
            Method method = targetService.getClass().getMethod(getMethodName(), parameterTypes);
            Object[] args = prepareArguments(method.getGenericParameterTypes(), getArguments());
            Object result = method.invoke(targetService, args);
            remoteResponse.setContent(result);
        } catch (Exception ex) {
            remoteResponse.setExceptionOccurs(true);
            if (ex instanceof InvocationTargetException) {
                InvocationTargetException invocationTargetException = (InvocationTargetException) ex;
                Throwable targetException = invocationTargetException.getTargetException();
                if (targetException == null) {
                    remoteResponse.setContent(invocationTargetException);
                } else {
                    remoteResponse.setContent(targetException);
                }
            } else {
                remoteResponse.setContent(ex);
            }
        } finally {
            ExecutionContext.setContextMap(null);
        }

        return remoteResponse;
    }

    protected Object[] prepareArguments(Type[] types, List<String> arguments) throws Exception {
        if (types == null || types.length == 0) {
            return null;
        }

        Object[] args = new Object[types.length];
        for (int i = 0; i < types.length; i++) {
            Type type = types[i];
            args[i] = gson.fromJson(arguments.get(i), type);
        }

        return args;
    }
}

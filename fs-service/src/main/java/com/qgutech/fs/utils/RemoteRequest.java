package com.qgutech.fs.utils;

import com.google.gson.Gson;
import org.springframework.web.context.ContextLoaderListener;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class RemoteRequest {

    private static final Gson gson = new Gson();

    private String interfaceName;

    private String methodName;

    private Class<?>[] parameterTypes;

    private Object[] arguments;

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

    public Object[] getArguments() {
        return arguments;
    }

    public void setArguments(Object[] arguments) {
        this.arguments = arguments;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public void mergeAttributes(Map<String, String> attributes) {
        if (this.attributes == null) {
            this.attributes = new TreeMap<String, String>();
        }

        if (attributes != null) {
            this.attributes.putAll(attributes);
        }

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
            Class classType = Class.forName(getInterfaceName());
            Object targetService = ContextLoaderListener.getCurrentWebApplicationContext().getBean(classType);
            Class<?>[] parameterTypes = getParameterTypes();
            Method method = targetService.getClass().getMethod(getMethodName(), parameterTypes);
            Object[] arguments = getArguments();
            prepareArguments(parameterTypes, arguments);
            Object result = method.invoke(targetService, arguments);
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

    protected void prepareArguments(Class<?>[] parameterTypes, Object[] arguments) throws Exception {
        if (parameterTypes == null) {
            return;
        }

        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> clazz = parameterTypes[i];
            if (clazz == null) {
                continue;
            }

            processArray(clazz, arguments, i);
            processSet(clazz, arguments, i);
            processEnum(clazz, arguments, i);
            processDate(clazz, arguments, i);
            processLong(clazz, arguments, i);
        }
    }


    @SuppressWarnings("unchecked")
    protected void processEnum(Class<?> clazz, Object[] arguments, int i) {
        if (!clazz.isEnum()) {
            return;
        }

        Object argument = arguments[i];
        if (argument == null) {
            return;
        }

        if (!(argument instanceof String)) {
            return;
        }

        arguments[i] = Enum.valueOf((Class<Enum>) clazz, (String) argument);
    }

    protected void processDate(Class<?> clazz, Object[] arguments, int i) {
        if (!(Date.class.equals(clazz))) {
            return;
        }

        Object argument = arguments[i];
        if (argument == null) {
            return;
        }

        if (!(argument instanceof Long)) {
            return;
        }

        arguments[i] = new Date((Long) argument);
    }

    protected void processLong(Class<?> clazz, Object[] arguments, int i) {
        if (!(Long.class.equals(clazz) || Long.TYPE.equals(clazz))) {
            return;
        }

        Object argument = arguments[i];
        if (argument == null) {
            return;
        }

        if (!(argument instanceof Integer)) {
            return;
        }

        arguments[i] = ((Integer) argument).longValue();
    }


    protected void processArray(Class clazz, Object[] arguments, int i) {
        if (!clazz.isArray()) {
            return;
        }

        Object argument = arguments[i];
        if (argument == null) {
            return;
        }

        if (!(argument instanceof List)) {
            return;
        }

        List list = ((List) argument);
        int size = list.size();
        Class<?> componentType = clazz.getComponentType();
        if (String.class.equals(componentType)) {
            String[] stringArray = new String[size];
            for (int j = 0; j < size; j++) {
                stringArray[j] = (String) list.get(j);
            }

            arguments[i] = stringArray;

        } else if (Class.class.equals(clazz.getComponentType())) {
            Class[] classArray = new Class[size];
            for (int j = 0; j < size; j++) {
                classArray[j] = (Class) list.get(j);
            }

            arguments[i] = classArray;

        } else if (Object.class.equals(clazz.getComponentType())) {
            Object[] objectArray = new Object[size];
            for (int j = 0; j < size; j++) {
                objectArray[j] = list.get(j);
            }

            arguments[i] = objectArray;
        }
    }

    protected void processSet(Class clazz, Object[] arguments, int i)
            throws InstantiationException, IllegalAccessException {
        if (!(Set.class.isAssignableFrom(clazz))) {
            return;
        }

        Object argument = arguments[i];
        if (argument == null) {
            return;
        }

        if (!(argument instanceof List)) {
            return;
        }

        List list = (List) argument;
        int size = list.size();
        Set set;
        if (clazz.isInterface()) {
            set = (TreeSet.class.isAssignableFrom(clazz)) ? new TreeSet() : new HashSet();
        } else {
            set = (Set) clazz.newInstance();
        }

        for (int j = 0; j < size; j++) {
            set.add(list.get(j));
        }

        arguments[i] = set;
    }
}

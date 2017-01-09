package com.qgutech.fs.utils;

public class RemoteResponse {

    private boolean exceptionOccurs;

    private Object content;

    public boolean isExceptionOccurs() {
        return exceptionOccurs;
    }

    public void setExceptionOccurs(boolean exceptionOccurs) {
        this.exceptionOccurs = exceptionOccurs;
    }

    public Object getContent() {
        return content;
    }

    public void setContent(Object content) {
        this.content = content;
    }

}

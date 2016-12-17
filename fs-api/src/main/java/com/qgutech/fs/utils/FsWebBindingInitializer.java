package com.qgutech.fs.utils;

import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.support.ConfigurableWebBindingInitializer;
import org.springframework.web.context.request.WebRequest;

import java.util.Date;

public class FsWebBindingInitializer extends ConfigurableWebBindingInitializer {
    @Override
    public void initBinder(WebDataBinder binder, WebRequest request) {
        super.initBinder(binder, request);
        binder.registerCustomEditor(Date.class, new DatePropertyEditor());
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(false));
    }
}

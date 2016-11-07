package com.qgutech.fs.utils;

import com.qgutech.fs.domain.base.BaseEntity;
import org.hibernate.CallbackException;
import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;

import java.io.Serializable;
import java.util.Date;

import static com.qgutech.fs.domain.base.BaseEntity.*;

public class BaseModelInterceptor extends EmptyInterceptor {

    @Override
    public boolean onSave(Object entity, Serializable id, Object[] state
            , String[] propertyNames, Type[] types) {
        return audit(entity, state, propertyNames);
    }

    @Override
    public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState
            , Object[] previousState, String[] propertyNames, Type[] types) throws CallbackException {
        for (int i = 0; i < propertyNames.length; i++) {
            if (_updateTime.equals(propertyNames[i])) {
                currentState[i] = new Date();
            } else if (_updateBy.equals(propertyNames[i])) {
                currentState[i] = ExecutionContext.getUserId();
            } else if (_corpCode.equals(propertyNames[i])) {
                Object currState = currentState[i];
                if (currState == null) {
                    currentState[i] = ExecutionContext.getCorpCode();
                }
            }
        }

        return true;
    }

    private boolean audit(Object entity, Object[] state, String[] propertyNames) {
        if (!(entity instanceof BaseEntity)) {
            return false;
        }

        boolean changed = false;
        for (int i = 0; i < propertyNames.length; i++) {
            String propertyName = propertyNames[i];
            if (_createTime.equals(propertyName)
                    || _updateTime.equals(propertyName)) {
                Object currState = state[i];
                if (currState == null) {
                    state[i] = new Date();
                    changed = true;
                }
            } else if (_createBy.equals(propertyName)
                    || _updateBy.equals(propertyName)) {
                Object currState = state[i];
                if (currState == null) {
                    state[i] = ExecutionContext.getUserId();
                    changed = true;
                }
            } else if (_corpCode.equals(propertyName)) {
                Object currState = state[i];
                if (currState == null) {
                    state[i] = ExecutionContext.getCorpCode();
                    changed = true;
                }
            }
        }

        return changed;
    }
}
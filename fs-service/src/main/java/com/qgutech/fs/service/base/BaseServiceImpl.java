package com.qgutech.fs.service.base;

import com.qgutech.fs.domain.base.BaseEntity;
import com.qgutech.fs.utils.*;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.*;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.TypedValue;
import org.hibernate.internal.CriteriaImpl;
import org.hibernate.loader.criteria.CriteriaQueryTranslator;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.util.*;

import static com.qgutech.fs.domain.base.BaseEntity.*;

/**
 * 这个基础类，抽象了Service接口的一些通常实现，简化普通服务类的开发。
 */

@SuppressWarnings("unchecked")
public class BaseServiceImpl<T extends BaseEntity> implements BaseService<T>, BaseConstant {

    protected final Log LOG = LogFactory.getLog(getClass());

    protected final Class<T> modelClass;

    protected static final int BATCH_SIZE = 1000;

    @Resource
    SessionFactory sessionFactory;

    public BaseServiceImpl() {
        modelClass = ReflectUtil.getGenericParamClass(this.getClass());
    }

    protected Session getSession() {
        return sessionFactory.getCurrentSession();
    }

    protected Criteria createCriteria() {
        return createCriteria(modelClass);
    }

    protected Criteria createCriteria(Class<?> clazz) {
        return getSession().createCriteria(clazz);
    }

    protected String getEntityName() {
        return ClassUtils.getShortName(modelClass);
    }

    protected String getFullEntityName() {
        return modelClass.getName();
    }

    @Override
    @Transactional(readOnly = false, isolation = Isolation.READ_COMMITTED)
    public String save(T model) {
        Assert.notNull(model, "model is null!");
        String modelId = model.getId();
        Session session = getSession();
        if (StringUtils.isBlank(modelId)) {
            return (String) session.save(model);
        }

        session.update(model);
        session.flush();
        return modelId;
    }

    @Override
    @Transactional(readOnly = false, isolation = Isolation.READ_COMMITTED)
    public void update(T model, String... fields) {
        Assert.notNull(model, "model is null!");
        if (fields == null || fields.length == 0) {
            getSession().update(model);
            return;
        }

        Set<String> set = new HashSet<String>(fields.length + 2);
        Collections.addAll(set, fields);
        set.add(_updateBy);
        set.add(_updateTime);
        model.setUpdateBy(ExecutionContext.getUserId());
        model.setUpdateTime(new Date());
        update(set.toArray(new String[set.size()]), model);
    }

    private int update(String[] fields, T model) {
        HqlBuilder builder = new HqlBuilder();
        builder.append(UPDATE).append(getEntityName()).append(SET);
        builder.append(fields[0]).append(EQUAL).append(QUESTION_MARK);
        builder.addParameter(getFieldValue(fields[0], model));

        for (int i = 1; i < fields.length; i++) {
            builder.append(COMMA).append(fields[i]).append(EQUAL).append(QUESTION_MARK);
            builder.addParameter(getFieldValue(fields[i], model));
        }

        builder.append(WHERE).append(_id).append(EQUAL).append(QUESTION_MARK);
        builder.addParameter(model.getId());
        return executeUpdate(builder);
    }

    @Override
    @Transactional(readOnly = false, isolation = Isolation.READ_COMMITTED)
    public void update(List<T> models, String... fields) {
        Assert.notEmpty(models, "models is empty!");
        if (fields == null || fields.length == 0) {
            saveOrUpdate(models);
            return;
        }

        for (T model : models) {
            update(model, fields);
        }
    }

    @Override
    @Transactional(readOnly = false, isolation = Isolation.READ_COMMITTED)
    public int update(String modelId, String fieldName, Object value) {
        Assert.hasText(modelId, "modelId is empty!");
        Assert.hasText(fieldName, "fieldName is empty!");

        List<String> modelIds = new ArrayList<String>(1);
        modelIds.add(modelId);
        return update(modelIds, fieldName, value);
    }

    @Override
    @Transactional(readOnly = false, isolation = Isolation.READ_COMMITTED)
    public int update(List<String> modelIds, String fieldName, Object value) {
        Assert.notEmpty(modelIds, "modelIds is empty!");
        Assert.hasText(fieldName, "fieldName is empty!");

        getSession().flush();
        HqlBuilder builder = new HqlBuilder();
        builder.append(UPDATE).append(getEntityName()).append(SET);
        builder.append(fieldName).append(EQUAL).append(COLON + "fieldValue").append(COMMA);
        builder.append(_updateTime).append(EQUAL).append(COLON + _updateTime).append(COMMA);
        builder.append(_updateBy).append(EQUAL).append(COLON + _updateBy);
        builder.append(WHERE).append(_id).append(IN).append(LEFT_BRACKET)
                .append(COLON + "primaryKeyValues").append(RIGHT_BRACKET);
        Query query = getSession().createQuery(builder.toString());
        query.setParameter("fieldValue", value);
        query.setParameter(_updateTime, new Date());
        query.setParameter(_updateBy, ExecutionContext.getUserId());
        int size = modelIds.size();
        int count = size % BATCH_SIZE == 0 ? size / BATCH_SIZE : (size / BATCH_SIZE + 1);
        int execute = 0;
        for (int i = 0; i < count; i++) {
            int start = i * BATCH_SIZE;
            int end = (i + 1) * BATCH_SIZE >= size ? size : (i + 1) * BATCH_SIZE;
            query.setParameterList("primaryKeyValues", modelIds.subList(start, end));
            execute += query.executeUpdate();
        }

        getSession().clear();
        return execute;
    }

    @Override
    @Transactional(readOnly = false, isolation = Isolation.READ_COMMITTED)
    public int update(List<String> modelIds, T model, String... fields) {
        Assert.notEmpty(modelIds, "modelIds is empty!");
        Assert.notNull(model, "model is null!");

        return updateByCriterion(Restrictions.in(_id, modelIds), model, fields);
    }

    private Object getFieldValue(String fieldName, Object model) {
        Class<?> clazz = model.getClass();
        if (!fieldName.contains(".")) {
            Field field = ReflectUtil.getField(clazz, fieldName);
            Assert.notNull(field, "field[fieldName:" + fieldName + "] not in class[" + clazz + "]!");
            return ReflectUtil.getFieldValue(field, model);
        }

        String beforeField = fieldName.substring(0, fieldName.indexOf("."));
        Assert.hasText(beforeField, "field[fieldName" + fieldName + "] is illegal!");
        Field field = ReflectUtil.getField(clazz, beforeField);
        Assert.notNull(field, "field[fieldName:" + beforeField + "] not in class[" + clazz + "]!");
        Object fieldValue = ReflectUtil.getFieldValue(field, model);
        if (fieldValue == null) {
            return null;
        }

        String afterField = fieldName.substring(fieldName.indexOf(".") + 1);
        Assert.hasText(afterField, "field[fieldName" + fieldName + "] is illegal!");
        return getFieldValue(afterField, fieldValue);
    }

    @Override
    @Transactional(readOnly = true)
    public T load(String modelId) {
        Assert.hasText(modelId, "modelId is empty!");
        return (T) getSession().load(modelClass, modelId);
    }

    @Override
    @Transactional(readOnly = true)
    public T get(String modelId, String... fieldNames) {
        Assert.hasText(modelId, "modelId is empty!");
        if (fieldNames == null || fieldNames.length == 0) {
            return (T) getSession().get(modelClass, modelId);
        }

        return getByFieldNameAndValue(_id, modelId, fieldNames);
    }

    @Override
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public boolean exist(String modelId) {
        Assert.hasText(modelId, "modelId is empty!");
        return exist(_id, modelId);
    }

    @Override
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public boolean exist(String fieldName, Object fieldValue) {
        Assert.hasText(fieldName, "fieldName is empty!");

        return exist(Restrictions.eq(fieldName, fieldValue));
    }

    @Override
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public boolean exist(Criterion criterion) {
        Assert.notNull(criterion, "criterion is null!");

        return getRowCountByCriterion(criterion) > 0;
    }

    @Override
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public Long getRowCountByFieldNameAndValue(String fieldName, Object fieldValue) {
        Assert.hasText(fieldName, "fieldName is empty!");

        return getRowCountByCriterion(Restrictions.eq(fieldName, fieldValue));
    }

    @Override
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public Long getRowCountByCriterion(Criterion criterion) {
        Assert.notNull(criterion, "criterion is null!");

        Object result = createCriteria().add(criterion)
                .setProjection(Projections.rowCount()).uniqueResult();
        return result == null ? 0L : (Long) result;
    }

    @Override
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public <V> V sumByFieldNameAndValue(String fieldName, Object fieldValue, String sumField) {
        Assert.hasText(fieldName, "fieldName is empty!");
        Assert.hasText(sumField, "sumField is empty!");

        return sumByCriterion(Restrictions.eq(fieldName, fieldValue), sumField);
    }

    @Override
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public <V> V sumByCriterion(Criterion criterion, String sumField) {
        Assert.notNull(criterion, "criterion is null!");
        Assert.hasText(sumField, "sumField is empty!");

        return (V) createCriteria().add(criterion)
                .setProjection(Projections.sum(sumField)).uniqueResult();
    }

    @Override
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public <K, V> Map<K, V> groupSumByCriterion(Criterion criterion, String groupField, String sumField) {
        Assert.notNull(criterion, "criterion is null!");
        Assert.hasText(groupField, "groupField is empty!");
        Assert.hasText(sumField, "sumField is empty!");

        Criteria criteria = createCriteria().add(criterion);
        Map<String, Boolean> aliasMap = new HashMap<String, Boolean>(1);
        if (groupField.contains(".")) {
            String alias = groupField.substring(0, groupField.indexOf("."));
            String subField = groupField.substring(groupField.indexOf(".") + 1);
            Assert.hasText(subField, "subField[" + groupField + "] is empty!");
            if (!validPrimaryKey(alias, subField)) {
                aliasMap.put(alias, true);
                criteria.createAlias(alias, alias);
            }
        }

        if (sumField.contains(".")) {
            String alias = sumField.substring(0, sumField.indexOf("."));
            String subField = sumField.substring(sumField.indexOf(".") + 1);
            Assert.hasText(subField, "subField[" + sumField + "] is empty!");
            if (!validPrimaryKey(alias, subField) && aliasMap.get(alias) == null) {
                criteria.createAlias(alias, alias);
            }
        }

        List<Object[]> list = criteria.setProjection(Projections.projectionList()
                .add(Projections.groupProperty(groupField))
                .add(Projections.sum(sumField))).list();
        if (list == null || list.size() == 0) {
            return new HashMap<K, V>(0);
        }

        Map<K, V> resultMap = new HashMap<K, V>(list.size());
        for (Object[] objects : list) {
            resultMap.put((K) objects[0], (V) (objects[1]));
        }

        return resultMap;
    }

    @Override
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public <K, V> Map<K, V> groupByCriterion(Criterion criterion, String groupField, String maxField) {
        Assert.notNull(criterion, "criterion is null!");
        Assert.hasText(groupField, "groupField is empty!");
        Assert.hasText(maxField, "maxField is empty!");

        Criteria criteria = createCriteria().add(criterion);
        Map<String, Boolean> aliasMap = new HashMap<String, Boolean>(1);
        if (groupField.contains(".")) {
            String alias = groupField.substring(0, groupField.indexOf("."));
            String subField = groupField.substring(groupField.indexOf(".") + 1);
            Assert.hasText(subField, "subField[" + groupField + "] is empty!");
            if (!validPrimaryKey(alias, subField)) {
                aliasMap.put(alias, true);
                criteria.createAlias(alias, alias);
            }
        }

        if (maxField.contains(".")) {
            String alias = maxField.substring(0, maxField.indexOf("."));
            String subField = maxField.substring(maxField.indexOf(".") + 1);
            Assert.hasText(subField, "subField[" + maxField + "] is empty!");
            if (!validPrimaryKey(alias, subField) && aliasMap.get(alias) == null) {
                criteria.createAlias(alias, alias);
            }
        }

        List<Object[]> list = criteria.setProjection(Projections.projectionList()
                .add(Projections.groupProperty(groupField))
                .add(Projections.max(maxField))).list();
        if (list == null || list.size() == 0) {
            return new HashMap<K, V>(0);
        }

        Map<K, V> resultMap = new HashMap<K, V>(list.size());
        for (Object[] objects : list) {
            resultMap.put((K) objects[0], (V) (objects[1]));
        }

        return resultMap;
    }

    @Override
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public <K, V> Map<K, V> groupByCriterion(Criterion criterion, Order[] orders
            , String groupField, String field) {
        Assert.notNull(criterion, "criterion is null!");
        Assert.notEmpty(orders, "orders is empty!");
        Assert.hasText(groupField, "groupField is empty!");
        Assert.hasText(field, "field is empty!");

        ProjectionList projectionList = Projections.projectionList()
                .add(Projections.groupProperty(groupField))
                .add(Projections.max(field));
        Criteria criteria = createCriteria().add(criterion);
        for (Order order : orders) {
            Assert.notNull(order, "order in orders is null!");
            criteria.addOrder(order);
            if (order.isAscending()) {
                projectionList.add(Projections.min(order.getPropertyName()).as(order.getPropertyName()));
            } else {
                projectionList.add(Projections.max(order.getPropertyName()).as(order.getPropertyName()));
            }
        }

        List<Object[]> list = criteria.setProjection(projectionList).list();
        if (list == null || list.size() == 0) {
            return new HashMap<K, V>(0);
        }

        Map<K, V> resultMap = new HashMap<K, V>(list.size());
        for (Object[] objects : list) {
            resultMap.put((K) objects[0], (V) (objects[1]));
        }

        return resultMap;
    }

    private boolean validPrimaryKey(String firstField, String subField) {
        Field field = ReflectUtil.getField(modelClass, firstField);
        Assert.notNull(field, "field[" + firstField + "] not in class[" + modelClass + "]!");
        if (!subField.contains(".")) {
            Field f = ReflectUtil.getField(field.getType(), subField);
            Assert.notNull(f, "field[" + subField + "] not in class[" + field.getType() + "]!");
            return _id.equals(f.getName());
        }

        String secondFieldName = subField.substring(0, subField.indexOf("."));
        Field secondField = ReflectUtil.getField(field.getType(), secondFieldName);
        Assert.notNull(secondField, "field[" + secondFieldName + "] not in class[" + field.getType() + "]!");
        String thirdFieldName = subField.substring(subField.indexOf(".") + 1);
        Field thirdField = ReflectUtil.getField(secondField.getType(), thirdFieldName);
        Assert.notNull(thirdField, "field[" + thirdFieldName + "] not in class[" + secondField.getType() + "]!");
        return false;
    }

    private void setFieldValue(String fieldName, Object fieldValue, Object model) {
        Class<?> clazz = model.getClass();
        if (!fieldName.contains(".")) {
            Field field = ReflectUtil.getField(clazz, fieldName);
            Assert.notNull(field, "field[fieldName:" + fieldName + "] not in class[" + clazz + "]!");
            ReflectUtil.setFieldValue(field, model, fieldValue);
            return;
        }

        String beforeField = fieldName.substring(0, fieldName.indexOf("."));
        Assert.hasText(beforeField, "field[fieldName" + fieldName + "] is illegal!");
        Field field = ReflectUtil.getField(clazz, beforeField);
        Assert.notNull(field, "field[fieldName:" + beforeField + "] not in class[" + clazz + "]!");
        Object relModel = ReflectUtil.getFieldValue(field, model);
        if (relModel == null) {
            relModel = ReflectUtil.newInstance(field.getType());
            ReflectUtil.setFieldValue(field, model, relModel);
        }

        String afterField = fieldName.substring(fieldName.indexOf(".") + 1);
        Assert.hasText(afterField, "field[fieldName" + fieldName + "] is illegal!");
        setFieldValue(afterField, fieldValue, relModel);
    }

    @Override
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public T getByFieldNameAndValue(String fieldName, Object fieldValue, String... fieldNames) {
        Assert.hasText(fieldName, "fieldName is empty!");

        if (fieldNames == null || fieldNames.length == 0) {
            return getByCriterion(Restrictions.eq(fieldName, fieldValue));
        }

        Set<String> fields = new HashSet<String>(fieldNames.length + 1);
        Collections.addAll(fields, fieldNames);
        fields.add(fieldName);

        return getByCriterion(Restrictions.eq(fieldName, fieldValue), fields.toArray(new String[fields.size()]));
    }

    @Override
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public T getByCriterion(Criterion criterion, String... fieldNames) {
        Assert.notNull(criterion, "criterion is null!");

        Criteria criteria = createCriteria().add(criterion);
        if (fieldNames == null || fieldNames.length == 0) {
            return (T) criteria.uniqueResult();
        }

        return getByCriteriaAndFieldNames(criteria, fieldNames);
    }

    @Override
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public T getByFieldNameAndValue(String fieldName, Object fieldValue, Order[] orders, String... fieldNames) {
        Assert.hasText(fieldName, "fieldName is empty!");
        Assert.notEmpty(orders, "orders is empty!");

        if (fieldNames == null || fieldNames.length == 0) {
            return getByCriterion(Restrictions.eq(fieldName, fieldValue), orders);
        }

        Set<String> fields = new HashSet<String>(fieldNames.length + 1);
        Collections.addAll(fields, fieldNames);
        fields.add(fieldName);
        return getByCriterion(Restrictions.eq(fieldName, fieldValue), orders, fieldNames);
    }

    @Override
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public T getByCriterion(Criterion criterion, Order[] orders, String... fieldNames) {
        Assert.notNull(criterion, "criterion is null!");
        Assert.notEmpty(orders, "orders is empty!");

        Criteria criteria = createCriteria().add(criterion);
        for (Order order : orders) {
            Assert.notNull(order, "order in orders is null!");
            criteria.addOrder(order);
        }

        criteria.setMaxResults(1);
        if (fieldNames == null || fieldNames.length == 0) {
            return (T) criteria.uniqueResult();
        }

        return getByCriteriaAndFieldNames(criteria, fieldNames);
    }

    @Override
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public <V> V getFieldValueById(String modelId, String fieldName) {
        Assert.hasText(modelId, "modelId is empty!");
        Assert.hasText(fieldName, "fieldName is empty!");

        return getFieldValueByFieldNameAndValue(_id, modelId, fieldName);
    }

    @Override
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public <V> V getFieldValueByFieldNameAndValue(String name, Object value, String fieldName) {
        Assert.hasText(name, "name is empty!");
        Assert.hasText(fieldName, "fieldName is empty!");

        return getFieldValueByCriterion(Restrictions.eq(name, value), fieldName);
    }

    @Override
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public <V> V getFieldValueByCriterion(Criterion criterion, String fieldName) {
        Assert.notNull(criterion, "criterion is null!");
        Assert.hasText(fieldName, "fieldName is empty!");

        Criteria criteria = createCriteria().add(criterion);
        if (fieldName.contains(".")) {
            String alias = fieldName.substring(0, fieldName.indexOf("."));
            String subField = fieldName.substring(fieldName.indexOf(".") + 1);
            if (!validPrimaryKey(alias, subField)) {
                criteria.createAlias(alias, alias);
            }
        }

        return (V) criteria.setProjection(Projections.property(fieldName)).uniqueResult();
    }

    @Override
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public <V> V getFieldValueByFieldNameAndValue(String name, Object value, Order[] orders, String fieldName) {
        Assert.hasText(name, "name is empty!");
        Assert.notEmpty(orders, "orders is empty!");
        Assert.hasText(fieldName, "fieldName is empty!");

        return getFieldValueByCriterion(Restrictions.eq(name, value), orders, fieldName);
    }

    @Override
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public <V> V getFieldValueByCriterion(Criterion criterion, Order[] orders, String fieldName) {
        Assert.notNull(criterion, "criterion is null!");
        Assert.notEmpty(orders, "orders is empty!");
        Assert.hasText(fieldName, "fieldName is empty!");

        Criteria criteria = createCriteria().add(criterion);
        if (fieldName.contains(".")) {
            String alias = fieldName.substring(0, fieldName.indexOf("."));
            String subField = fieldName.substring(fieldName.indexOf(".") + 1);
            if (!validPrimaryKey(alias, subField)) {
                criteria.createAlias(alias, alias);
            }
        }

        for (Order order : orders) {
            Assert.notNull(order, "order in orders is null!");
            criteria.addOrder(order);
        }

        return (V) criteria.setProjection(Projections.property(fieldName)).setMaxResults(1).uniqueResult();
    }

    private T getByCriteriaAndFieldNames(Criteria criteria, String[] fieldNames) {
        ProjectionList projectionList = Projections.projectionList();
        Map<String, Boolean> aliasMap = new HashMap<String, Boolean>();
        for (String fieldName : fieldNames) {
            Assert.hasText(fieldName, "field is empty!");
            if (fieldName.contains(".")) {
                String alias = fieldName.substring(0, fieldName.indexOf("."));
                String subField = fieldName.substring(fieldName.indexOf(".") + 1);
                Assert.hasText(subField, "subField[" + fieldName + "] is empty!");
                if (!validPrimaryKey(alias, subField) && aliasMap.get(alias) == null) {
                    aliasMap.put(alias, true);
                    criteria.createAlias(alias, alias);
                }
            }

            projectionList.add(Projections.property(fieldName));
        }

        Object o = criteria.setProjection(projectionList).uniqueResult();
        if (o == null) {
            return null;
        }

        T model = (T) ReflectUtil.createInstance(modelClass);
        Assert.notNull(model, "createInstance[class:" + modelClass + "] is failed!");
        if (o.getClass().isArray()) {
            Object[] objects = (Object[]) o;
            for (int i = 0; i < fieldNames.length; i++) {
                Object value = objects[i];
                if (value == null) {
                    continue;
                }

                setFieldValue(fieldNames[i], value, model);
            }
        } else {
            setFieldValue(fieldNames[0], o, model);
        }

        return model;
    }

    @Override
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public List<T> listByIds(List<String> modelIds, String... fieldNames) {
        Assert.notEmpty(modelIds, "modelIds is empty!");

        Criterion criterion = Restrictions.in(_id, modelIds);
        if (fieldNames == null || fieldNames.length == 0) {
            return listByCriterion(criterion);
        }

        Set<String> fields = new HashSet<String>(fieldNames.length + 1);
        Collections.addAll(fields, fieldNames);
        fields.add(_id);
        return listByCriterion(criterion, fields.toArray(new String[fields.size()]));
    }

    @Override
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public List<T> listByIds(List<String> modelIds, Order[] orders, String... fieldNames) {
        Assert.notEmpty(modelIds, "modelIds is empty!");
        Assert.notEmpty(orders, "orders is empty!");

        Criterion criterion = Restrictions.in(_id, modelIds);
        if (fieldNames == null || fieldNames.length == 0) {
            return listByCriterion(criterion, orders);
        }

        Set<String> fields = new HashSet<String>(fieldNames.length + 1);
        Collections.addAll(fields, fieldNames);
        fields.add(_id);
        return listByCriterion(criterion, orders, fields.toArray(new String[fields.size()]));
    }

    @Override
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public List<T> listByFieldNameAndValue(String fieldName, Object fieldValue, String... fieldNames) {
        Assert.hasText(fieldName, "fieldName is empty!");
        Criterion criterion = Restrictions.eq(fieldName, fieldValue);
        if (fieldNames == null || fieldNames.length == 0) {
            return listByCriterion(criterion);
        }

        Set<String> fields = new HashSet<String>(fieldNames.length + 1);
        Collections.addAll(fields, fieldNames);
        fields.add(fieldName);
        return listByCriterion(criterion, fields.toArray(new String[fields.size()]));
    }

    @Override
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public List<T> listByFieldNameAndValue(String fieldName, Object fieldValue
            , Order[] orders, String... fieldNames) {
        Assert.hasText(fieldName, "fieldName is empty!");
        Assert.notEmpty(orders, "orders is empty!");

        Criterion criterion = Restrictions.eq(fieldName, fieldValue);
        if (fieldNames == null || fieldNames.length == 0) {
            return listByCriterion(criterion, orders);
        }

        Set<String> fields = new HashSet<String>(fieldNames.length + 1);
        Collections.addAll(fields, fieldNames);
        fields.add(fieldName);
        return listByCriterion(criterion, orders, fields.toArray(new String[fields.size()]));
    }

    @Override
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public List<T> listByCriterion(Criterion criterion, String... fieldNames) {
        Assert.notNull(criterion, "criterion is null!");

        Criteria criteria = createCriteria().add(criterion);
        if (fieldNames == null || fieldNames.length == 0) {
            return criteria.list();
        }

        return listByCriteriaAndFieldNames(criteria, fieldNames);
    }

    @Override
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public List<T> listByCriterion(Criterion criterion, Order[] orders, String... fieldNames) {
        Assert.notNull(criterion, "criterion is null!");
        Assert.notEmpty(orders, "orders is empty!");

        Criteria criteria = createCriteria().add(criterion);
        for (Order order : orders) {
            Assert.notNull(order, "order in orders is null!");
            criteria.addOrder(order);
        }

        if (fieldNames == null || fieldNames.length == 0) {
            return criteria.list();
        }

        return listByCriteriaAndFieldNames(criteria, fieldNames);
    }

    @Override
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public <V> List<V> listFieldValueByFieldNameAndValue(String name, Object value
            , Order[] orders, String fieldName) {
        Assert.hasText(name, "name is empty!");
        Assert.notEmpty(orders, "orders is empty!");
        Assert.hasText(fieldName, "fieldName is empty!");

        return listFieldValueByCriterion(Restrictions.eq(name, value), orders, fieldName);
    }

    @Override
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public <V> List<V> listFieldValueByFieldNameAndValue(String name, Object value, String fieldName) {
        Assert.hasText(name, "name is empty!");
        Assert.hasText(fieldName, "fieldName is empty!");

        return listFieldValueByCriterion(Restrictions.eq(name, value), fieldName);
    }

    @Override
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public <V> List<V> listFieldValueByCriterion(Criterion criterion, Order[] orders, String fieldName) {
        Assert.notNull(criterion, "criterion is null!");
        Assert.notEmpty(orders, "orders is empty!");
        Assert.hasText(fieldName, "fieldName is empty!");

        Criteria criteria = createCriteria().add(criterion);
        for (Order order : orders) {
            Assert.notNull(order, "order in orders is null!");
            criteria.addOrder(order);
        }

        if (fieldName.contains(".")) {
            String alias = fieldName.substring(0, fieldName.indexOf("."));
            String subField = fieldName.substring(fieldName.indexOf(".") + 1);
            if (!validPrimaryKey(alias, subField)) {
                criteria.createAlias(alias, alias);
            }
        }

        return (List<V>) criteria.setProjection(Projections.property(fieldName)).list();
    }

    @Override
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public <V> List<V> listFieldValueByCriterion(Criterion criterion, String fieldName) {
        Assert.notNull(criterion, "criterion is null!");
        Assert.hasText(fieldName, "fieldName is empty!");

        Criteria criteria = createCriteria().add(criterion);
        if (fieldName.contains(".")) {
            String alias = fieldName.substring(0, fieldName.indexOf("."));
            String subField = fieldName.substring(fieldName.indexOf(".") + 1);
            if (!validPrimaryKey(alias, subField)) {
                criteria.createAlias(alias, alias);
            }
        }

        return (List<V>) criteria.setProjection(Projections.property(fieldName)).list();
    }

    private List<T> listByCriteriaAndFieldNames(Criteria criteria, String[] fieldNames) {
        ProjectionList projectionList = Projections.projectionList();
        Map<String, Boolean> aliasMap = new HashMap<String, Boolean>();
        for (String fieldName : fieldNames) {
            Assert.hasText(fieldName, "fieldName is empty!");
            if (fieldName.contains(".")) {
                String alias = fieldName.substring(0, fieldName.indexOf("."));
                String subField = fieldName.substring(fieldName.indexOf(".") + 1);
                Assert.hasText(subField, "subField[" + fieldName + "] is empty!");
                if (!validPrimaryKey(alias, subField) && aliasMap.get(alias) == null) {
                    aliasMap.put(alias, true);
                    criteria.createAlias(alias, alias);
                }
            }

            projectionList.add(Projections.property(fieldName));
        }

        List<Object> list = criteria.setProjection(projectionList).list();
        if (list == null || list.size() == 0) {
            return new ArrayList<T>(0);
        }

        List<T> models = new ArrayList<T>(list.size());
        for (Object o : list) {
            T model = (T) ReflectUtil.createInstance(modelClass);
            Assert.notNull(model, "createInstance[class:" + modelClass + "] is failed!");
            if (o.getClass().isArray()) {
                Object[] objects = (Object[]) o;
                for (int i = 0; i < fieldNames.length; i++) {
                    Object value = objects[i];
                    if (value == null) {
                        continue;
                    }

                    setFieldValue(fieldNames[i], value, model);
                }
            } else {
                setFieldValue(fieldNames[0], o, model);
            }

            models.add(model);
        }

        return models;
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public int delete(String modelId) {
        Assert.hasText(modelId, "modelId is empty!");
        return delete(Arrays.asList(modelId));
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public int delete(List<String> modelIds) {
        Assert.notEmpty(modelIds, "modelIds is empty!");
        return delete(Restrictions.in(_id, modelIds));
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public int delete(Criterion condition) {
        Assert.notNull(condition, "condition is null!");

        HqlBuilder builder = new HqlBuilder();
        builder.append(DELETE).append(FROM).append(getEntityName())
                .append(WHERE).append(toHqlBuilder(condition));

        return executeUpdate(builder);
    }

    @Override
    @Transactional(readOnly = false, isolation = Isolation.READ_COMMITTED)
    public List<String> save(List<T> models) {
        Assert.notEmpty(models, "Models is empty!");
        List<String> modelIds = new ArrayList<String>(models.size());
        Session session = getSession();
        for (int i = 0; i < models.size(); i++) {
            T model = models.get(i);
            String modelId = (String) session.save(model);
            modelIds.add(modelId);
            if (i % 100 == 0) {
                session.flush();
                session.clear();
            }
        }

        return modelIds;
    }

    @Override
    @Transactional(readOnly = false, isolation = Isolation.READ_COMMITTED)
    public void saveOrUpdate(List<T> models) {
        Assert.notEmpty(models, "Models is empty!");
        Session session = getSession();
        for (int i = 0; i < models.size(); i++) {
            T model = models.get(i);
            session.saveOrUpdate(model);
            if (i % 100 == 0) {
                session.flush();
                session.clear();
            }
        }
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public int updateByCriterion(Criterion condition, String fieldName, Object fieldValue) {
        Assert.hasText(fieldName, "fieldName is empty!");

        if (condition == null) {
            HqlBuilder builder = new HqlBuilder();
            builder.append(UPDATE).append(getEntityName()).append(SET)
                    .append(fieldName).append(EQUAL).append(QUESTION_MARK)
                    .append(COMMA).append(_updateBy).append(EQUAL).append(QUESTION_MARK)
                    .append(COMMA).append(_updateTime).append(EQUAL).append(QUESTION_MARK);
            builder.addParameter(fieldValue).addParameter(ExecutionContext.getUserId())
                    .addParameter(new Date());
            return executeUpdate(builder);
        }

        T model = (T) ReflectUtil.createInstance(modelClass);
        Assert.notNull(model, "create instance[class:" + modelClass + "] failed!");
        setFieldValue(fieldName, fieldValue, model);
        return updateByCriterion(condition, model, fieldName);
    }

    private String[] getPersistPropertyNames() {
        return sessionFactory.getClassMetadata(modelClass).getPropertyNames();
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public int updateByCriterion(Criterion condition, T model, String... fields) {
        Assert.notNull(condition, "condition is null!");
        Assert.notNull(model, "model is null!");

        model.setUpdateBy(ExecutionContext.getUserId());
        model.setUpdateTime(new Date());
        if (fields == null || fields.length == 0) {
            String[] propertyNames = getPersistPropertyNames();
            Set<String> properties = new HashSet<String>(propertyNames.length);
            Collections.addAll(properties, propertyNames);
            properties.remove(_corpCode);
            properties.remove(_createBy);
            properties.remove(_createTime);
            fields = properties.toArray(new String[properties.size()]);
        } else {
            Set<String> properties = new HashSet<String>(fields.length + 2);
            Collections.addAll(properties, fields);
            properties.add(_updateBy);
            properties.add(_updateTime);
            fields = properties.toArray(new String[properties.size()]);
        }

        HqlBuilder builder = new HqlBuilder();
        builder.append(UPDATE).append(getEntityName()).append(SET);
        builder.append(fields[0]).append(EQUAL).append(QUESTION_MARK);
        builder.addParameter(getFieldValue(fields[0], model));
        for (int i = 1; i < fields.length; i++) {
            builder.append(COMMA).append(fields[i]).append(EQUAL).append(QUESTION_MARK);
            builder.addParameter(getFieldValue(fields[i], model));
        }

        builder.append(WHERE).append(toHqlBuilder(condition));
        return executeUpdate(builder);
    }

    private int executeUpdate(HqlBuilder builder) {
        Session session = getSession();
        session.flush();
        Query query = session.createQuery(builder.toString());
        List<Object> parameterList = builder.getParameterList();
        for (int i = 0; i < parameterList.size(); i++) {
            query.setParameter(i, parameterList.get(i));
        }

        Map<String, Object> parameterMap = builder.getParameterMap();
        for (Map.Entry<String, Object> entry : parameterMap.entrySet()) {
            Object value = entry.getValue();
            String key = entry.getKey();
            if (value == null) {
                query.setParameter(key, null);
            } else if (value instanceof Collection) {
                query.setParameterList(key, (Collection) value);
            } else if (value.getClass().isArray()) {
                query.setParameterList(key, (Object[]) value);
            } else {
                query.setParameter(key, value);
            }
        }

        int execute = query.executeUpdate();
        session.clear();
        return execute;
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public int incrByCriterion(Criterion condition, String fieldName, Object increment) {
        Assert.hasText(fieldName, "fieldName is empty!");
        Assert.notNull(increment, "increment is null!");

        if (condition == null) {
            HqlBuilder builder = new HqlBuilder();
            builder.append(UPDATE).append(getEntityName()).append(SET)
                    .append(fieldName).append(EQUAL)
                    .append(COALESCE).append(LEFT_BRACKET).append(fieldName).append(COMMA)
                    .append(ZERO).append(RIGHT_BRACKET).append(PLUS).append(QUESTION_MARK)
                    .append(COMMA).append(_updateBy).append(EQUAL).append(QUESTION_MARK)
                    .append(COMMA).append(_updateTime).append(EQUAL).append(QUESTION_MARK);
            builder.addParameter(increment)
                    .addParameter(ExecutionContext.getUserId())
                    .addParameter(new Date());
            return executeUpdate(builder);
        }

        T model = (T) ReflectUtil.createInstance(modelClass);
        Assert.notNull(model, "create instance[class:" + modelClass + "] failed!");
        setFieldValue(fieldName, increment, model);
        return incrByCriterion(condition, model, new String[]{fieldName});
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public int incrByCriterion(Criterion condition, T model, String[] incrFields, String... fields) {
        Assert.notNull(condition, "condition is null!");
        Assert.notNull(model, "model is null!");
        Assert.notEmpty(incrFields, "incrFields is empty!");

        model.setUpdateBy(ExecutionContext.getUserId());
        model.setUpdateTime(new Date());
        if (fields == null || fields.length == 0) {
            fields = new String[]{_updateBy, _updateTime};
        } else {
            Set<String> properties = new HashSet<String>(fields.length + 2);
            Collections.addAll(properties, fields);
            properties.add(_updateBy);
            properties.add(_updateTime);
            fields = properties.toArray(new String[properties.size()]);
        }

        HqlBuilder builder = new HqlBuilder();
        builder.append(UPDATE).append(getEntityName()).append(SET);
        builder.append(incrFields[0]).append(EQUAL)
                .append(COALESCE).append(LEFT_BRACKET).append(incrFields[0]).append(COMMA)
                .append(ZERO).append(RIGHT_BRACKET).append(PLUS).append(QUESTION_MARK);
        builder.addParameter(getFieldValue(incrFields[0], model));
        for (int i = 1; i < incrFields.length; i++) {
            builder.append(COMMA).append(incrFields[i]).append(EQUAL)
                    .append(COALESCE).append(LEFT_BRACKET).append(incrFields[i]).append(COMMA)
                    .append(ZERO).append(RIGHT_BRACKET).append(PLUS).append(QUESTION_MARK);
            builder.addParameter(getFieldValue(incrFields[i], model));
        }

        for (String field : fields) {
            builder.append(COMMA).append(field).append(EQUAL).append(QUESTION_MARK);
            builder.addParameter(getFieldValue(field, model));
        }


        builder.append(WHERE).append(toHqlBuilder(condition));
        return executeUpdate(builder);
    }

    private HqlBuilder toHqlBuilder(Criterion criterion) {
        HqlBuilder builder = new HqlBuilder();
        Criteria criteria = createCriteria();
        CriteriaQueryTranslator translator = new CriteriaQueryTranslator(
                (SessionFactoryImplementor) sessionFactory,
                (CriteriaImpl) criteria, getFullEntityName(), CriteriaQueryTranslator.ROOT_SQL_ALIAS);
        CriteriaQuery criteriaQuery = new FsCriteriaQuery(translator);
        String sqlString = criterion.toSqlString(criteria, criteriaQuery)
                .replace(CriteriaQueryTranslator.ROOT_SQL_ALIAS + _point, StringUtils.EMPTY);
        builder.append(sqlString);
        TypedValue[] typedValues = criterion.getTypedValues(criteria, criteriaQuery);
        for (TypedValue typedValue : typedValues) {
            builder.addParameter(typedValue.getValue());
        }

        return builder;
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public int incr(String modelId, String fieldName, Object increment) {
        Assert.hasText(modelId, "modelId is empty!");
        Assert.hasText(fieldName, "fieldName is empty!");
        Assert.notNull(increment, "increment is null!");

        List<String> modelIds = new ArrayList<String>(1);
        modelIds.add(modelId);
        return incr(modelIds, fieldName, increment);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public int incr(List<String> modelIds, String fieldName, Object increment) {
        Assert.notEmpty(modelIds, "modelIds is empty!");
        Assert.hasText(fieldName, "fieldName is empty!");
        Assert.notNull(increment, "increment is null!");

        return incrByCriterion(Restrictions.in(_id, modelIds), fieldName, increment);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public int incr(String modelId, T model, String[] incrFields, String... fields) {
        Assert.hasText(modelId, "modelId is empty!");
        Assert.notNull(model, "model is null!");
        Assert.notEmpty(incrFields, "incrFields is null!");

        List<String> modelIds = new ArrayList<String>(1);
        modelIds.add(modelId);
        return incr(modelIds, model, incrFields, fields);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public int incr(List<String> modelIds, T model, String[] incrFields, String... fields) {
        Assert.notEmpty(modelIds, "modelIds is null!");
        Assert.notNull(model, "model is null!");
        Assert.notEmpty(incrFields, "incrFields is empty!");

        return incrByCriterion(Restrictions.in(_id, modelIds), model, incrFields, fields);
    }
}

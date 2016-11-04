package com.qgutech.fs.utils;


import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.criterion.CriteriaQuery;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.TypedValue;
import org.hibernate.type.Type;

public class ScCriteriaQuery implements CriteriaQuery {

    private CriteriaQuery criteriaQuery;

    public ScCriteriaQuery() {

    }

    public ScCriteriaQuery(CriteriaQuery criteriaQuery) {
        this.criteriaQuery = criteriaQuery;
    }

    public void setCriteriaQuery(CriteriaQuery criteriaQuery) {
        this.criteriaQuery = criteriaQuery;
    }

    @Override
    public SessionFactoryImplementor getFactory() {
        return criteriaQuery.getFactory();
    }

    @Override
    public String getColumn(Criteria criteria, String propertyPath) throws HibernateException {
        return propertyPath;
    }

    @Override
    public String[] getColumns(String propertyPath, Criteria criteria) throws HibernateException {
        return new String[]{propertyPath};
    }

    @Override
    public String[] findColumns(String propertyPath, Criteria criteria) throws HibernateException {
        return new String[]{propertyPath};
    }

    @Override
    public Type getType(Criteria criteria, String propertyPath) throws HibernateException {
        return criteriaQuery.getType(criteria, propertyPath);
    }

    @Override
    public String[] getColumnsUsingProjection(Criteria criteria, String propertyPath) throws HibernateException {
        return new String[]{propertyPath};
    }

    @Override
    public Type getTypeUsingProjection(Criteria criteria, String propertyPath) throws HibernateException {
        return criteriaQuery.getTypeUsingProjection(criteria, propertyPath);
    }

    @Override
    public TypedValue getTypedValue(Criteria criteria, String propertyPath, Object value) throws HibernateException {
        return criteriaQuery.getTypedValue(criteria, propertyPath, value);
    }

    @Override
    public String getEntityName(Criteria criteria) {
        return criteriaQuery.getEntityName(criteria);
    }

    @Override
    public String getEntityName(Criteria criteria, String propertyPath) {
        return criteriaQuery.getEntityName(criteria, propertyPath);
    }

    @Override
    public String getSQLAlias(Criteria subcriteria) {
        return criteriaQuery.getSQLAlias(subcriteria);
    }

    @Override
    public String getSQLAlias(Criteria criteria, String propertyPath) {
        return criteriaQuery.getSQLAlias(criteria, propertyPath);
    }

    @Override
    public String getPropertyName(String propertyName) {
        return criteriaQuery.getPropertyName(propertyName);
    }

    @Override
    public String[] getIdentifierColumns(Criteria subcriteria) {
        return criteriaQuery.getIdentifierColumns(subcriteria);
    }

    @Override
    public Type getIdentifierType(Criteria subcriteria) {
        return criteriaQuery.getIdentifierType(subcriteria);
    }

    @Override
    public TypedValue getTypedIdentifierValue(Criteria subcriteria, Object value) {
        return criteriaQuery.getTypedIdentifierValue(subcriteria, value);
    }

    @Override
    public String generateSQLAlias() {
        return criteriaQuery.generateSQLAlias();
    }
}

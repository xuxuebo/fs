package com.qgutech.fs.service;

import com.qgutech.fs.domain.StoredFile;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service("storedFileService")
public class StoredFileServiceImpl implements StoredFileService {
    @Resource
    private SessionFactory sessionFactory;

    protected Session getSession() {
        return sessionFactory.getCurrentSession();
    }

    protected Criteria createCriteria() {
        return getSession().createCriteria(StoredFile.class);
    }

}

package com.qgutech.fs.service;

import com.qgutech.fs.domain.StoredFile;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

import static com.qgutech.fs.domain.StoredFile.*;

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

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, readOnly = true)
    public StoredFile get(String storedFileId) {
        Assert.hasText(storedFileId, "StoredFileId is empty!");
        return (StoredFile) getSession().get(StoredFile.class, storedFileId);
    }

    @Override
    @SuppressWarnings("unchecked")
    @Transactional(isolation = Isolation.READ_COMMITTED, readOnly = true)
    public List<StoredFile> listByIds(List<String> storedFileIds) {
        Assert.notEmpty(storedFileIds, "StoredFileIds is empty!");
        return createCriteria().add(Restrictions.in(_storedFileId, storedFileIds)).list();
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public String save(StoredFile storedFile) {
        Assert.notNull(storedFile, "StoredFile is null!");
        storedFile.setCreateTime(new Date());
        storedFile.setUpdateTime(new Date());
        return (String) getSession().save(storedFile);
    }
}

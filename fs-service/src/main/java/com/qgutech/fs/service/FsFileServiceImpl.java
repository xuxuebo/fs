package com.qgutech.fs.service;

import com.qgutech.fs.domain.FsFile;
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

import static com.qgutech.fs.domain.FsFile.*;

@Service("fsFileService")
public class FsFileServiceImpl implements FsFileService {
    @Resource
    private SessionFactory sessionFactory;

    protected Session getSession() {
        return sessionFactory.getCurrentSession();
    }

    protected Criteria createCriteria() {
        return getSession().createCriteria(FsFile.class);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, readOnly = true)
    public FsFile get(String storedFileId) {
        Assert.hasText(storedFileId, "StoredFileId is empty!");
        return (FsFile) getSession().get(FsFile.class, storedFileId);
    }

    @Override
    @SuppressWarnings("unchecked")
    @Transactional(isolation = Isolation.READ_COMMITTED, readOnly = true)
    public List<FsFile> listByIds(List<String> storedFileIds) {
        Assert.notEmpty(storedFileIds, "StoredFileIds is empty!");
        return createCriteria().add(Restrictions.in(_storedFileId, storedFileIds)).list();
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public String save(FsFile fsFile) {
        Assert.notNull(fsFile, "FsFile is null!");
        fsFile.setCreateTime(new Date());
        fsFile.setUpdateTime(new Date());
        return (String) getSession().save(fsFile);
    }
}

package com.qgutech.fs.service;


import com.qgutech.fs.domain.FsServer;
import com.qgutech.fs.service.base.BaseServiceImpl;
import com.qgutech.fs.utils.FsConstants;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Set;

import static com.qgutech.fs.domain.FsServer.*;

@Service("fsServerService")
public class FsServerServiceImpl extends BaseServiceImpl<FsServer> implements FsServerService {

    @Override
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public List<FsServer> getUploadFsServerList(String corpCode) {
        Assert.hasText(corpCode, "CorpCode is empty!");
        return listByCriterion(Restrictions.conjunction()
                .add(Restrictions.in(_corpCode, new String[]{FsConstants.DEFAULT_CORP_CODE, corpCode}))
                .add(Restrictions.eq(_upload, true)));
    }

    @Override
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public List<FsServer> getDownloadFsServerListByServerCode(String corpCode, String serverCode) {
        Assert.hasText(corpCode, "CorpCode is empty!");
        Assert.hasText(serverCode, "ServerCode is empty!");
        return listByCriterion(Restrictions.conjunction()
                .add(Restrictions.in(_corpCode, new String[]{FsConstants.DEFAULT_CORP_CODE, corpCode}))
                .add(Restrictions.eq(_download, true))
                .add(Restrictions.eq(_serverCode, serverCode)));
    }

    @Override
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public List<FsServer> getDownloadFsServerList(Set<String> corpCodes, Set<String> serverCodes) {
        Assert.notEmpty(corpCodes, "CorpCodes is empty!");
        Assert.notEmpty(serverCodes, "ServerCodes is empty!");
        corpCodes.add(FsConstants.DEFAULT_CORP_CODE);
        return listByCriterion(Restrictions.conjunction()
                .add(Restrictions.in(_corpCode, corpCodes))
                .add(Restrictions.eq(_download, true))
                .add(Restrictions.in(_serverCode, serverCodes)));
    }

    @Override
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public FsServer getFsServerByServerHostAndServerCode(String serverHost, String serverCode) {
        Assert.hasText(serverHost, "ServerHost is empty!");
        Assert.hasText(serverCode, "ServerCode is empty!");

        return getByCriterion(Restrictions.conjunction()
                .add(Restrictions.eq(_host, serverHost))
                .add(Restrictions.eq(_serverCode, serverCode)));
    }


}

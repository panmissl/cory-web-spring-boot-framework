package com.cory.service;

import com.cory.dao.ExportJobDao;
import com.cory.enums.ExportJobStatus;
import com.cory.model.ExportJob;
import com.cory.util.DateFormatUtils;
import com.cory.util.IpUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;

import java.util.function.Supplier;

/**
 * generated by CodeGenerator on 2017/5/10.
 */
@Slf4j
@Service
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
public class ExportJobService extends BaseService<ExportJob> {

    @Autowired
    private ExportJobDao exportJobDao;
    @Value("${server.port}")
    private Integer port;

    @Override
    public ExportJobDao getDao() {
        return exportJobDao;
    }

    /**
     * 导出
     * @param type 类型，自定义字符串，用于隔离不同的导出数据。在获取导出列表时用到。
     * @param exportExecutor 导出器，导出后返回导出文件的下载地址。比如将文件上传到OSS，然后返回下载地址。
     */
    public void doExport(String type, Supplier<String> exportExecutor) {
        String code = DateFormatUtils.formatNowAsSecondWithoutDash() + RandomStringUtils.randomNumeric(6);
        ExportJob job = ExportJob.builder()
                .type(type)
                .code(code)
                .status(ExportJobStatus.init)
                .ip(buildIpPort())
                .build();
        exportJobDao.add(job);
        try {
            exportJobDao.updateStatusAndDownloadUrl(code, ExportJobStatus.running, null, null);
            String downloadUrl = exportExecutor.get();
            exportJobDao.updateStatusAndDownloadUrl(code, ExportJobStatus.success, downloadUrl, null);
        } catch (Throwable t) {
            log.error("export job fail, job code: {}", job.getCode(), t);
            String msg = t.getMessage();
            if (null != msg && msg.length() > 200) {
                msg = msg.substring(0, 200);
            }
            exportJobDao.updateStatusAndDownloadUrl(code, ExportJobStatus.fail, null, msg);
        }
    }

    private String buildIpPort() {
        return IpUtil.getHostIp() + ":" + port;
    }
}

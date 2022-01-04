package com.hsn.epic4j.start;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.hsn.epic4j.bean.AliMvnDto;
import com.hsn.epic4j.config.EpicConfig;
import com.hsn.epic4j.util.VersionCompare;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * @author hsn
 * 2022/1/4
 * MavenUpdate
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "epic", name = IUpdate.CONFIG, havingValue = "maven")
public class MavenUpdate implements IUpdate {
    @Autowired
    EpicConfig epicConfig;

    @Override
    public void checkForUpdate() {
        String aliMvnSearch = "https://developer.aliyun.com/artifact/aliyunMaven/searchArtifactByGav?groupId=io.github.huisunan&artifactId=epic4j&version=&repoId=all&_input_charset=utf-8";
        String aliDownloadUrl = "https://archiva-maven-storage-prod.oss-cn-beijing.aliyuncs.com/repository/central/io/github/huisunan/epic4j/{}/epic4j-{}.jar";
        String string = HttpUtil.get(aliMvnSearch);
        AliMvnDto aliMvnDto = JSONUtil.toBean(string, AliMvnDto.class);
        VersionCompare compare = new VersionCompare();
        aliMvnDto.getObject().stream().filter(item -> "jar".equals(item.getPackaging()))
                .max(((o1, o2) -> compare.compare(o1.getVersion(), o2.getVersion())))
                .ifPresent(latestDto -> {
                    if (compare.compare(latestDto.getVersion(), epicConfig.getVersion()) > 0) {
                        String downloadUrl = StrUtil.format(aliDownloadUrl, latestDto.getVersion(), latestDto.getVersion());
                        File file = new File("test/" + latestDto.getFileName());
                        HttpUtil.downloadFile(downloadUrl, file);
                        log.debug("need update");
                    }
                });
        log.info(string);
    }
}

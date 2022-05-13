package com.hsn.epic4j.core;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.hsn.epic4j.core.config.EpicConfig;
import com.hsn.epic4j.core.util.VersionCompare;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * 通过阿里云的maven仓库更新,有一定的延迟
 *
 * @author hsn
 * 2022/1/4
 * MavenUpdate
 */
@Slf4j
@RequiredArgsConstructor
public class MavenUpdate implements IUpdate {
    private final String currentVersion;

    /**
     * 有0-4小时的延迟
     */
    @Override
    public void checkForUpdate() {
        String sonatypeSearch = "https://search.maven.org/solrsearch/select?q=g:io.github.huisunan%20AND%20a:epic4j&start=0&rows=20";
        String downloadUrl = "http://search.maven.org/remotecontent?filepath=io/github/huisunan/epic4j/{}/epic4j-{}.jar";
        String string = HttpUtil.get(sonatypeSearch);
        JSONObject jsonObject = JSONUtil.parseObj(string);
        Integer numFound = (Integer) jsonObject.getByPath("response.numFound");
        if (numFound < 1) {
            log.warn("not found jar package");
            return;
        }
        String lastVersion = (String) jsonObject.getByPath("response.docs[0].latestVersion");

        VersionCompare compare = new VersionCompare();
        if (compare.compare(lastVersion, currentVersion) > 0) {
            //需要更新
            String url = StrUtil.format(downloadUrl, lastVersion, lastVersion);
            File file = new File("epic4j.jar.update");
            HttpUtil.downloadFile(url, file);
            log.info("download new version:{}", lastVersion);
            System.exit(UPDATE_EXIT_CODE);
        }
    }
}

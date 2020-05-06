package com.qiwen.base.service.impl;

import com.qiwen.base.config.QWAppConfig;
import com.qiwen.base.service.IIp2RegionService;
import org.lionsoul.ip2region.DataBlock;
import org.lionsoul.ip2region.DbConfig;
import org.lionsoul.ip2region.DbMakerConfigException;
import org.lionsoul.ip2region.DbSearcher;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.IOException;

@Service
public class Ip2RegionServiceImpl implements IIp2RegionService {

    private final QWAppConfig appConfig;

    private final DbSearcher searcher;

    public Ip2RegionServiceImpl(QWAppConfig appConfig) throws DbMakerConfigException, FileNotFoundException {
        this.appConfig = appConfig;
        this.searcher = new DbSearcher(new DbConfig(), appConfig.getIp2Region().getDbPath());
    }

    @Override
    public DataBlock ip2Region(String ip) throws IOException {
        switch (appConfig.getIp2Region().getAlgorithm().toLowerCase()) {
            case "b-tree":
                return this.searcher.btreeSearch(ip);
            case "memory":
                return this.searcher.memorySearch(ip);
            case "binary":
                return this.searcher.binarySearch(ip);
        }
        return null;
    }
}

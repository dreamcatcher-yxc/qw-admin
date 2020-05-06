package com.qiwen.base.service;

import org.lionsoul.ip2region.DataBlock;

import java.io.IOException;

/**
 * 根据 IP 地址地址
 */
public interface IIp2RegionService {

    /**
     * 根据 IP 地址查询位置信息<br/>
     * eg:
     * <pre>
     * 101.105.35.57 -> 2163|中国|华南|广东省|深圳市|鹏博士
     * </pre>
     * @param ip
     * @return
     */
    DataBlock ip2Region(String ip) throws IOException;

}

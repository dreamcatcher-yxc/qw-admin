package com.qiwen.base.entity.pojo;

import lombok.Data;

@Data
public class QWCacheMeta {

    private String name;

    private long memoryStoreSize;

    private long diskStoreSize;

    private long offHeapStoreSize;

    private long onMemoryStoreBytes;

    private long onDiskStoreSizeBytes;

    private long onOffHeapSizeBytes;
}

package com.example;

import lombok.Data;

@Data
public class ChinaRegionsInfo {
    /**
     * 行政区域编码。
     */
    private String code;

    /**
     * 行政区域名称。
     */
    private String name;

    /**
     * 行政区域类型，1:省份，2：城市，3：区或者县城。
     */
    private Integer type;

    /**
     * 上一级行政区域编码。
     */
    private String parentCode;
}

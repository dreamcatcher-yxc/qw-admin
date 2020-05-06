package com.qiwen.base.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MenuVO {

    private long id;

    private String icon;

    private String name;

    private String url;

    private String treeId;

    private List<MenuVO> subMenus;

}

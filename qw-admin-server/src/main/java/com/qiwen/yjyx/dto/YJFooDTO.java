package com.qiwen.yjyx.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 *  系统用户
 * </p>
 *
 * @author yangxiuchu
 * @since 2020-03-13
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class YJFooDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer jlid;

    private String gh;

    private String xm;

    private String bm;

    private String gw;

    private String kl;

    private String xt;

    private String zplj;

    private Double xtYwxt;

    private Double xtChgl;

    private Double xtCwgl;

    private Double xtRzxz;

    private Double ywJhd;

    private Double ywZyfp;

    private Double ywEcfp;

    private Double ywZysh;

    private Double ywBjd;

    private Double ywBjsh;

    private Double ywJc;

    private Double ywCc;

    private Double ywXwgl;

    private Double ywXwcx;

    private Double ywKjcx;

    private Double ywCzQx;

    private Double ywCzWx;

    private Double ywCzNj25;

    private Double ywCzNj5;

    private Double ywCzGz;

    private Double ywCzFx;

    private Double ywCzJr;

    private Double ywCzQtz;

    private Double ywCzDc;

    private Double ywCzQt;

    private Double ywCx;

    private Double ywBjcx;

    private Double ywZpgl;

    private Double sysRcsj;

    private Double sysRccx;

    private Double setKh;

    private Double setBm;

    private Double setUser;

    private Double setQx;

    private Double setFm;

    private Double kcRk;

    private Double kcCk;

    private Double kcCkqr;

    private Double kcYzjz;

    private Double kcRkcx;

    private Double kcCkcx;

    private Double kcJxc;

    private Double kcMx;

    private Double kcGys;

    private Double kcPcb;

    private Double setGys;

    private Double setChdm;

    private Double setChlb;

    private Double cwDz;

    private Double cwKp;

    private Double cwDx;

    private Double cwHx;

    private Double cwSkht;

    private Double cwSkhtzx;

    private Double cwSkhtsh;

    private Double cwSkhtcx;


}

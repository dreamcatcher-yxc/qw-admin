package com.qiwen.base.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class Result {

    public static final int OK = 1;

    public static final int ERROR = 2;

    //参数验证错误响应编码
    public static final int VALI_ERROR = 3;

    public static final int AUTHENTICATION_ERROR = 403;

    private static final String STATE_KEY = "code";
    private static final String DESC_KEY = "message";
    private static final String DATA_KEY = "body";
    private static final String PAGE_DATA_KEY = "rows";
    private static final String PAGE_COUNT_KEY = "count";

    private int state = OK;
    private String desc = "";
    private Map<String, Object> data;

    private Result() {
        data = new HashMap<>();
    }

    public void setState(int state) {
        this.state = state;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public static Result getOKInstance() {
        Result rb = new Result();
        rb.setState(OK);
        return rb;
    }

    public static Result getOKInstance(String desc) {
        Result rb = new Result();
        rb.setState(OK);
        rb.setDesc(desc);
        return rb;
    }

    public static Result ok() {
        Result rb = new Result();
        rb.setState(OK);
        return rb;
    }

    public static Result ok(String desc) {
        Result rb = new Result();
        rb.setState(OK);
        if(desc == null) {
            return ok();
        }
        rb.setDesc(desc);
        return rb;
    }

    public static Result getErrorInstance() {
        Result rb = new Result();
        rb.setState(ERROR);
        return rb;
    }

    public static Result getErrorInstance(String desc) {
        Result rb = new Result();
        rb.setState(ERROR);
        rb.setDesc(desc);
        return rb;
    }

    public static Result error() {
        Result rb = new Result();
        rb.setState(ERROR);
        return rb;
    }

    public static Result error(String desc) {
        Result rb = new Result();
        rb.setState(ERROR);
        rb.setDesc(desc);
        return rb;
    }

    public static Result validError() {
        Result rb = new Result();
        rb.setState(VALI_ERROR);
        return rb;
    }

    public static Result validError(String desc) {
        Result rb = new Result();
        rb.setState(VALI_ERROR);
        rb.setDesc(desc);
        return rb;
    }

    public static Result page(Integer count, Collection<?> data) {
        return ok()
                .put(PAGE_COUNT_KEY, count)
                .put(PAGE_DATA_KEY, data);
    }

    public static Result page(Page<?> page) {
        return ok()
                .put(PAGE_COUNT_KEY, page.getTotalElements())
                .put(PAGE_DATA_KEY, page.getContent());
    }

    public static Result getValErrorInstance(String desc) {
        Result rb = new Result();
        rb.setState(VALI_ERROR);
        rb.setDesc(desc);
        return rb;
    }

    public static Result valError(String desc) {
        Result rb = new Result();
        rb.setState(VALI_ERROR);
        rb.setDesc(desc);
        return rb;
    }

    public Result putData(String key, Object value) {
        data.put(key, value);
        return this;
    }

    //添加Put Body
    public Result putData(Object value) {
        data.put(DATA_KEY, value);
        return this;
    }

    public Result put(String key, Object value) {
        data.put(key, value);
        return this;
    }

    //添加Put Body
    public Result put(Object value) {
        data.put(DATA_KEY, value);
        return this;
    }

	public String toJsonString() {
		Map<String, Object> rm = new HashMap<>();
		rm.put(STATE_KEY, state);
		rm.put(DESC_KEY, desc);
		rm.put(DATA_KEY, data);
        return GsonUtil.toJson(rm);
	}

    public String json() {
        return toJsonString();
    }

    public void toResponse(HttpServletResponse response) {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=utf-8");
        try {
            PrintWriter writer = response.getWriter();
            writer.append(json());
            writer.flush();
        } catch (IOException e) {
            log.error("cause: {}, message: {}", e.getCause(), e.getMessage());
        }
    }
}
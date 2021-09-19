/**
 * Copyright (c) 2016-2019 人人开源 All rights reserved.
 * <p>
 * https://www.renren.io
 * <p>
 * 版权所有，侵权必究！
 */

package cn.edu.zjut.common.utils;

import cn.edu.zjut.common.constant.DefaultConstant;
import cn.edu.zjut.common.exception.EmBizError;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import org.apache.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * 返回数据
 * 关于 R<T>
 * Jackson 对于 HashMap 类型会有特殊的处理方式，具体来说就是会对类进行向上转型为 Map，导致子类的私有属性消失
 *
 * @author Mark sunlightcs@gmail.com
 */
public class R extends HashMap<String, Object> {
    private static final long serialVersionUID = 1L;

    public R() {
        put("code", DefaultConstant.R_SUCCESS_CODE);
        put("msg", "success");
    }

    public R put(String key, Object value) {
        super.put(key, value);
        return this;
    }

    public Integer getCode() {
        return (Integer) this.get("code");
    }

    public <T> T getData(TypeReference<T> typeReference) {
        // SpringMVC 会自动将 data 转换为 ArrayList
        Object data = get("data");

        // 这里利用 fastjson 先把 data 转为 JSON 字符串再转为指定格式
        return JSON.parseObject(JSON.toJSONString(data), typeReference);
    }

    public <T> T getData(String key, TypeReference<T> typeReference) {
        // SpringMVC 会自动将 data 转换为 ArrayList
        Object data = get(key);

        // 这里利用 fastjson 先把 data 转为 JSON 字符串再转为指定格式
        return JSON.parseObject(JSON.toJSONString(data), typeReference);
    }

    public static R error() {
        return error(HttpStatus.SC_INTERNAL_SERVER_ERROR, "未知异常，请联系管理员");
    }

    public static R error(String msg) {
        return error(HttpStatus.SC_INTERNAL_SERVER_ERROR, msg);
    }

    public static R error(int code, String msg) {
        R r = new R();
        r.put("code", code);
        r.put("msg", msg);
        return r;
    }

    // 进一步封装全局异常
    public static R error(EmBizError emBizError) {
        return error(emBizError.getErrCode(), emBizError.getErrMsg());
    }

    public static R ok(String msg) {
        R r = new R();
        r.put("msg", msg);
        return r;
    }

    public static R ok(Map<String, Object> map) {
        R r = new R();
        r.putAll(map);
        return r;
    }

    public static R ok() {
        return new R();
    }

}

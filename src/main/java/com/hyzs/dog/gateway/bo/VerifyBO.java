package com.hyzs.dog.gateway.bo;

import lombok.Data;
import org.apache.commons.lang.StringUtils;

/**
 * @author Hua-cloud
 */
@Data
public class VerifyBO {
    /**
     * 请求方法
     */
    private  String method;
    /**
     * url
     */
    private  String uri;
    /**
     * 查询字符串
     */
    private  String queryString;
    /**
     * 一次性随机数
     */
    private  String nonce;
    /**
     * 时间戳
     */
    private  String timestamp;
    /**
     * token
     */
    private  String Authorization;
    /**
     *  请求数据
     */
    private  String requestData;

    @Override
    public String toString() {
        if(StringUtils.isEmpty(method)){
            method="";
        }
        if(StringUtils.isEmpty(uri)){
            uri="";
        }
        if(StringUtils.isEmpty(queryString)){
            queryString="";
        }
        if(StringUtils.isEmpty(nonce)){
            nonce="";
        }
        if(StringUtils.isEmpty(timestamp)){
            timestamp="";
        }
        if(StringUtils.isEmpty(Authorization)){
            Authorization="";
        }
        if(StringUtils.isEmpty(requestData)){
            requestData="";
        }
        return method + '\n' + uri + '\n' +
                queryString + '\n' + nonce + '\n' +
                 timestamp + '\n' + Authorization + '\n' + requestData;
    }
}

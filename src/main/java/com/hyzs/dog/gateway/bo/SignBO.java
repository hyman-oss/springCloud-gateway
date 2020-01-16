package com.hyzs.dog.gateway.bo;

import lombok.Data;

/**
 * @author Hua-cloud
 */
@Data
public class SignBO {
    /**
     * 一次性随机数
     */
    private String nonce;
    /**
     * 时间戳
     */
    private String timestamp;
    /**
     * token
     */
    private String Authorization;
    /**
     * 响应数据
     */
    private String responseData;

    @Override
    public String toString() {
        return  nonce + '\n' +
                timestamp + '\n' +
                Authorization + '\n' +
                responseData;
    }
}

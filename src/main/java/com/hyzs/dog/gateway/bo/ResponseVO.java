//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.hyzs.dog.gateway.bo;
import java.io.Serializable;

/**
 * @author Hua-cloud
 */
public class ResponseVO<T> implements Serializable {
    private static final long serialVersionUID = 6775422262797117144L;
    private Integer code;
    private String msg;
    private T data;

    public ResponseVO() {
    }

    public Integer getCode() {
        return this.code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return this.msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return this.data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("ResponseVO{");
        sb.append("code=").append(this.code);
        sb.append(", msg='").append(this.msg).append('\'');
        sb.append(", data=").append(this.data);
        sb.append('}');
        return sb.toString();
    }
}

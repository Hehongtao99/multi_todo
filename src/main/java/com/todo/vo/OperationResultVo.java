package com.todo.vo;

import lombok.Data;

/**
 * 操作结果视图对象
 */
@Data
public class OperationResultVo {
    
    /**
     * 操作是否成功
     */
    private Boolean success;
    
    /**
     * 操作消息
     */
    private String message;
    
    /**
     * 操作数据
     */
    private Object data;
    
    public OperationResultVo() {}
    
    public OperationResultVo(Boolean success, String message) {
        this.success = success;
        this.message = message;
    }
    
    public OperationResultVo(Boolean success, String message, Object data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }
    
    public static OperationResultVo success(String message) {
        return new OperationResultVo(true, message);
    }
    
    public static OperationResultVo success(String message, Object data) {
        return new OperationResultVo(true, message, data);
    }
    
    public static OperationResultVo error(String message) {
        return new OperationResultVo(false, message);
    }
} 
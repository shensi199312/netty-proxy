package com.shensi.util;

/**
 * Created by shensi on 2018-12-18
 */
public enum  ProxyStatus {
    CONNECT(0),
    SSL_TO_TARGET(1),
    SSL_TO_TARGET_SUC(2);


    private int status;

    ProxyStatus(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }}

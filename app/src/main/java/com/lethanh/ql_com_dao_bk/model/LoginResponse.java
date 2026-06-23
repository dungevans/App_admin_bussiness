package com.lethanh.ql_com_dao_bk.model;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {
//    @SerializedName("success")
//    private boolean success;
//
//    @SerializedName("message")
//    private String message;
//    private String token; // Optional, if your server uses tokens

    public String getJwt() {
        return jwt;
    }

    public void setJwt(String jwt) {
        this.jwt = jwt;
    }

    //    public boolean isSuccess() { return success; }
//    public void setSuccess(boolean success) { this.success = success; }
//    public String getMessage() { return message; }
//    public void setMessage(String message) { this.message = message; }
//    public String getToken() { return token; }
//    public void setToken(String token) { this.token = token; }
    @SerializedName("jwt")
    private String jwt;
}

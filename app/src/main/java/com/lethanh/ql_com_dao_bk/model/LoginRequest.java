package com.lethanh.ql_com_dao_bk.model;

import com.google.gson.annotations.SerializedName;

public class LoginRequest {
    @SerializedName("login_id")
    private String loginId;
    
    @SerializedName("password")
    private String password;

    public LoginRequest(String loginId, String password) {
        this.loginId = loginId;
        this.password = password;
    }

    public String getLoginId() { return loginId; }
    public void setLoginId(String loginId) { this.loginId = loginId; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}

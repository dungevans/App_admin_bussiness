package com.lethanh.ql_com_dao_bk.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class NoticeResponse {
    @SerializedName("content")
    private List<Notice> content;

    public List<Notice> getContent() {
        return content;
    }

    public void setContent(List<Notice> content) {
        this.content = content;
    }
}

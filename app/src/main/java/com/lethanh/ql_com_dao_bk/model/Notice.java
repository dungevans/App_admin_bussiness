package com.lethanh.ql_com_dao_bk.model;

public class Notice {
    private String id;
    private String type;
    private String title;
    private String summary;
    private String content;
    private String created;

    public Notice() {}

    public Notice(String title, String summary, String content) {
        this.title = title;
        this.summary = summary;
        this.content = content;
        this.type = "SYSTEM";
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getCreated() { return created; }
    public void setCreated(String created) { this.created = created; }
}

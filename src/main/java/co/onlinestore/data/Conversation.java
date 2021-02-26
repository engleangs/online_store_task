package co.onlinestore.data;

import java.util.Date;

public class Conversation {
    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPageId() {
        return pageId;
    }

    public void setPageId(String pageId) {
        this.pageId = pageId;
    }

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
    public Conversation(){

    }

    public Conversation(String senderId, String receiverId, String type, String pageId, String companyId, String id, String content) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.type = type;
        this.pageId = pageId;
        this.companyId = companyId;
        this.id = id;
        this.content = content;
    }

    private String senderId;
    private String receiverId;
    private String type;
    private String pageId;
    private String companyId;
    private String id;
    private String content;
    private Date createdAt;

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}

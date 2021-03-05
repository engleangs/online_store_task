package co.onlinestore.data;

import com.google.gson.JsonObject;

public class PageEntry {
    private JsonObject jsonObject;
    private String pageId;
    private String senderId;
    private String field;

    public JsonObject getJsonObject() {
        return jsonObject;
    }

    public void setJsonObject(JsonObject jsonObject) {
        this.jsonObject = jsonObject;
    }

    public String getPageId() {
        return pageId;
    }

    public void setPageId(String pageId) {
        this.pageId = pageId;
    }
    public PageEntry(){

    }

    public PageEntry(JsonObject jsonObject, String pageId, String senderId) {
        this.jsonObject = jsonObject;
        this.pageId = pageId;
        this.senderId = senderId;
    }



    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }
}

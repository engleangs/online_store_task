package co.onlinestore.data;

public class PageLog {
    public String getPageId() {
        return pageId;
    }

    public void setPageId(String pageId) {
        this.pageId = pageId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public PageLog(String pageId, String content) {
        this.pageId = pageId;
        this.content = content;
    }

    private String pageId;
    private String content;
}

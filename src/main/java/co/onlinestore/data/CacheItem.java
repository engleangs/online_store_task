package co.onlinestore.data;

public class CacheItem {
    public CacheItem(String id, String content) {
        this.id = id;
        this.content = content;
    }

    private String id;
    private String content;

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
}

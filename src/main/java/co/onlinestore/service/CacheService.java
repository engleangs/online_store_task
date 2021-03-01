package co.onlinestore.service;

import co.onlinestore.data.Conversation;

public interface CacheService {
    void  cache(Conversation conversation,String id1, String id2);
}

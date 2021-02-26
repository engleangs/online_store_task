package co.onlinestore.service;

import co.onlinestore.data.CacheItem;
import co.onlinestore.data.Conversation;
import co.onlinestore.data.ConversationComparator;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class CacheServiceImpl implements CacheService {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private Gson gson;
    private static Type LIST_MAP_TYPE = new TypeToken<List<Conversation>>() {
    }.getType();

    @Override
    public void cache(Conversation conversation) {
        String id1 = conversation.getSenderId() + ":" + conversation.getReceiverId();
        String id2 = conversation.getReceiverId() + ":" + conversation.getSenderId();
        Object[] p = {id1, id2};
        String json;
        List<CacheItem> itemList = jdbcTemplate.query("SELECT id,content FROM conversation_cache WHERE id = ? or id =?",
                p, (rs, rowNum) -> new CacheItem(rs.getString("id"), rs.getString("content")));

        if (itemList.size() > 0) {
            CacheItem cacheItem = itemList.get(0);
            List<Conversation> messages = gson.fromJson(cacheItem.getContent(), LIST_MAP_TYPE);
            messages.sort(new ConversationComparator());
            if (messages.size() > 10) {
                messages.remove(0);

            }
            messages.add(0, conversation);
            json = gson.toJson(messages);


        } else {
            List<Conversation> messages = new ArrayList<>();
            messages.add(conversation);
            json = gson.toJson(messages);
        }
        store(id1, json);
        store(id2, json);
    }

    private void store(String id, String content) {
        String sql = "INSERT INTO conversation_cache (id,content,last_update)  " +
                " VALUES( ? , ? , ? ) ON DUPLICATE KEY UPDATE content= ?";
        Object[] param = {id, content, new Date(),content};
        jdbcTemplate.update(sql, param);
    }
}

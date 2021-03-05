package co.onlinestore.service;

import co.onlinestore.data.PageEntry;
import co.onlinestore.data.PageLog;
import co.onlinestore.data.Post;

public interface DataParserObjectService {
    PageEntry page(String json);
    PageLog pageLog(String json);
    Post pagePost(String json);
}

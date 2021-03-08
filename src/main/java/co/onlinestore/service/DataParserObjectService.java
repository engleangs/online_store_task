package co.onlinestore.service;

import co.onlinestore.data.Comment;
import co.onlinestore.data.PageEntry;
import co.onlinestore.data.PageLog;
import co.onlinestore.data.Post;

public interface DataParserObjectService {
    PageEntry page(String json);
    PageLog pageLog(String json);
    Post pagePost(String json);
    Post pagePost(PageEntry pageEntry);
    Comment pageComment(String json);
    Comment pageComment(PageEntry pageEntry);

}

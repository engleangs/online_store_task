package co.onlinestore.service;

import co.onlinestore.data.Comment;
import co.onlinestore.data.PageLog;
import co.onlinestore.data.Post;

public interface PageDataService  {
    void store(PageLog pageLog) ;
    void store(Post post);
    void store(Comment comment);
}

package co.onlinestore.service;

import co.onlinestore.config.KafkaTopic;
import co.onlinestore.data.Comment;
import co.onlinestore.data.PageEntry;
import co.onlinestore.data.PageLog;
import co.onlinestore.data.Post;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class PageEventListenerServiceImpl implements PageEventListenerService {
    private static final Logger LOGGER = LoggerFactory.getLogger( PageEventListenerServiceImpl.class);
    @Autowired
    private DataParserObjectService dataParserObjectService;
    @Autowired
    private PageDataService pageDataService;
    @KafkaListener(id = "task_page_listener", topics = KafkaTopic.FB_PAGE_EVENT)
    @Override
    public void onEvent(String msg) {
        LOGGER.info("receive default message : "+msg);
        PageEntry pageEntry = dataParserObjectService.page(msg);
        if( "feed".equals(pageEntry.getField())) {
            if( pageEntry.getJsonObject().has("item") ) {
                String type = pageEntry.getJsonObject().get("item").getAsString();
                if("status".equals(type)) {
                    Post post = dataParserObjectService.pagePost( pageEntry);
                    pageDataService.store(post);
                }
                else if("comment".equals(type)){
                    Comment comment = dataParserObjectService.pageComment( pageEntry);
                    pageDataService.store(comment);
                }
                else {
                    PageLog pageLog = new PageLog(pageEntry.getPageId(),msg);
                    pageDataService.store(pageLog);
                }
            }
        }
        else {
            PageLog pageLog = new PageLog(pageEntry.getPageId(),msg);
            pageDataService.store(pageLog);
        }
    }
}

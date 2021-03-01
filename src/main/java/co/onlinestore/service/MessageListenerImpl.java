package co.onlinestore.service;

import co.onlinestore.config.KafkaTopic;
import co.onlinestore.data.Conversation;
import co.onlinestore.data.Customer;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class MessageListenerImpl implements MessageListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageListenerImpl.class);
    private static Type MAP_TYPE = new TypeToken<Map<String, Object>>() {
    }.getType();
    private static final Type MAP_STRING_TYPE = new TypeToken<Map<String, String>>() {}.getType();
    @Autowired
    private DataService dataService;
    @Autowired
    private Gson gson;
    @Autowired
    private CacheService cacheService;
    @KafkaListener(id = "task_sender", topics = KafkaTopic.FB_MESSAGE_REPLY)
    public void onSend(String msg){
        LOGGER.info("sending msg to fb ["+msg +"]");
        Map<String,String>messageMap  = gson.fromJson( msg, MAP_STRING_TYPE);
        String pageId = messageMap.get("pageId")+ "";
        String customerId = messageMap.get("receiverId");
        String senderId = messageMap.get("senderId");
        String companyId = dataService.getCompanyId(pageId);
        String message = messageMap.get("content");
        String type = messageMap.get("type");
        Conversation conversation = new Conversation(senderId,customerId,type,pageId,companyId,messageMap.get("id"), message);
        conversation.setCreatedAt( new Date());
        dataService.storeMsg( conversation);
        LOGGER.info("done storing reply message..");
        String id1 = conversation.getPageId()+":"+conversation.getReceiverId();
        String id2  = conversation.getReceiverId()+":"+conversation.getPageId();
        cacheService.cache(conversation, id1,id2);
        LOGGER.info("cache conversation");



    }

    @KafkaListener(id = "task_receiver", topics = KafkaTopic.FB_MESAGE_RECEIVE)
    @Override
    public void listen(String in) {
        LOGGER.info("recieve message from broker :" + in);
        try {
            Map<String, Object> msgItem = gson.fromJson(in, MAP_TYPE);
            List<Map<String, Object>> items = (List<Map<String, Object>>) msgItem.get("entry");
            Map<String, Object> item = items.get(0);
            if (item.containsKey("messaging")) {
                List<Map<String, Object>> msgList = (List<Map<String, Object>>) item.get("messaging");
                Map<String, Object> messaging = msgList.get(0);
                if (messaging.containsKey("sender")) {
                    Map<String, Object> sender = (Map<String, Object>) messaging.get("sender");
                    Map<String, Object> receiver = (Map<String, Object>) messaging.get("recipient");
                    Map<String, Object> message = (Map<String, Object>) messaging.get("message");
                    String pageId = receiver.get("id") + "";
                    String customerId = sender.get("id") + "";
                    String companyId = dataService.getCompanyId(pageId);
                    String pageToken = dataService.getPageToken(pageId);
                    if( pageToken == null){
                        LOGGER.info("it's not for page so it's done : "+pageId);
                        return;
                    }

                    if( message !=null) {
                        String msgId = message.get("mid") + "";
                        String text = message.get("text") + "";
                        Conversation conversation = new Conversation(customerId, pageId, "text",pageId, companyId ,msgId, text);

                        long time = new Date().getTime();
                        if (item.containsKey("time")) {
                            Number number = (Number)item.get("time");
                            time = number.longValue();
                        }
                        conversation.setCreatedAt( new Date( time));
                        dataService.storeMsg( conversation);//store in db
                        String id1 = customerId+":"+ pageId;
                        String id2  = pageId+":"+customerId;
                        cacheService.cache(conversation,id1,id2); //cache also in db

                    }


                    Customer customer = dataService.get(customerId);
                    if (customer == null) {
                        try {
                            customer = dataService.fetchFromFb(customerId, pageToken);
                        } catch (IOException e) {
                            LOGGER.error("error fetching customer info", e);
                        }
                        if (customer != null) {
                            customer.setCompanyId( companyId);
                            dataService.store(customer);
                        }
                    } else if (dataService.shouldUpdate(customer)) {
                        try {
                            customer = dataService.fetchFromFb(customerId, pageToken);
                        } catch (Exception e) {
                            LOGGER.error("error fetch customer info", e);
                        }
                        if (customer != null) {
                            customer.setCompanyId( companyId);
                            dataService.store(customer);
                        }
                    }

                }


            }
        } catch (Exception e) {
            LOGGER.error("error parsing json ", e);
        }


    }
}

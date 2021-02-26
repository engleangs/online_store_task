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
    @Autowired
    private DataService dataService;
    @Autowired
    private Gson gson;
    @Autowired
    private CacheService cacheService;
    @KafkaListener(id = "task_sender", topics = KafkaTopic.FB_MESSAGE_REPLY)
    public void onSend(String msg){
        LOGGER.info("sending msg to fb "+msg);
        Map<String,Object>messageMap  = gson.fromJson( msg, MAP_TYPE);
        //todo store and cache conversation as well

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
                    if( message !=null) {
                        String msgId = message.get("mid") + "";
                        String text = message.get("text") + "";
                        Conversation conversation = new Conversation(customerId, pageId, "text",pageId, dataService.getCompanyId(pageId),msgId, text);

                        long time = new Date().getTime();
                        if (item.containsKey("time")) {
                            Number number = (Number)item.get("time");
                            time = number.longValue();
                        }
                        conversation.setCreatedAt( new Date( time));
                        dataService.storeMsg( conversation);//store in db
                        cacheService.cache(conversation); //cache also in db

                    }

                    String pageToken = dataService.getPageToken(pageId);
                    Customer customer = dataService.get(customerId);
                    if (customer == null) {
                        try {
                            customer = dataService.fetchFromFb(customerId, pageToken);
                        } catch (IOException e) {
                            LOGGER.error("error fetching customer info", e);
                        }
                        if (customer != null) {
                            dataService.store(customer);
                        }
                    } else if (dataService.shouldUpdate(customer)) {
                        try {
                            customer = dataService.fetchFromFb(customerId, pageToken);
                        } catch (Exception e) {
                            LOGGER.error("error fetch customer info", e);
                        }
                        if (customer != null) {
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

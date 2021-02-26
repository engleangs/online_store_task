package co.onlinestore.service;

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
import java.util.LinkedHashMap;
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

    @KafkaListener(id = "task_id", topics = "fb_receive_message")
    @Override
    public void listen(String in) {
        LOGGER.info("recieve message" + in);
        try {
            Map<String, Object> msgItem = gson.fromJson(in, MAP_TYPE);
            List<Map<String, Object>> items = (List<Map<String, Object>>) msgItem.get("entry");
            Map<String, Object> item = items.get(0);
            if (item.containsKey("messaging")) {
                List<Map<String, Object>> msgList = (List<Map<String, Object>>) item.get("messaging");
                Map<String, Object> messsaging = msgList.get(0);
                if (messsaging.containsKey("sender")) {
                    Map<String, Object> sender = (Map<String, Object>) messsaging.get("sender");
                    Map<String, Object> receiver = (Map<String, Object>) messsaging.get("recipient");
                    Map<String, Object> message = (Map<String, Object>) messsaging.get("message");
                    String pageId = receiver.get("id") + "";
                    String customerId = sender.get("id") + "";
                    if( message !=null) {
                        String msgId = message.get("mid") + "";
                        String text = message.get("text") + "";
                        Map<String, String> msgContent = new LinkedHashMap<>();
                        msgContent.put("sender_id", customerId);
                        msgContent.put("receiver_id", pageId);
                        msgContent.put("type", "text"); //todo check all type of message
                        msgContent.put("page_id", pageId);
                        msgContent.put("content", text);
                        msgContent.put("id", msgId);
                        long time = new Date().getTime();
                        if (item.containsKey("time")) {
                            Number number = (Number)item.get("time");
                            time = number.longValue();
                        }
                        dataService.storeMsg(msgContent, time);
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

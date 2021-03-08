package co.onlinestore.service;

import co.onlinestore.config.KafkaTopic;
import org.springframework.kafka.annotation.KafkaListener;

public interface PageEventListenerService {
     void onEvent(String msg);
    }


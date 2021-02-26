package co.onlinestore.service;

import co.onlinestore.data.Conversation;
import co.onlinestore.data.Customer;

import java.io.IOException;


public interface DataService {
    Customer get(String id);
    void store(Customer customer);
    boolean shouldUpdate(Customer customer);
    Customer fetchFromFb(String id,String pageToken) throws IOException;
    String getPageToken(String pageId);
    String getCompanyId(String pageId);
    void storeMsg(Conversation conversation);
}

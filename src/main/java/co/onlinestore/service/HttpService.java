package co.onlinestore.service;

import java.io.IOException;
import java.util.Map;

public interface HttpService {
     String get(String url, Map<String,String> headers) throws IOException;
     String post(String url,String body, Map<String,String> headers) throws IOException;
}

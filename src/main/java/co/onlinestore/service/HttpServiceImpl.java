package co.onlinestore.service;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;


@Service
public class HttpServiceImpl implements HttpService {
    HttpClient client = null ;
    private PoolingHttpClientConnectionManager manager;
    private RequestConfig config;
    private final int timeToLive =10;
    @PostConstruct
    private void init(){

        manager = new PoolingHttpClientConnectionManager();
        manager.setMaxTotal( 100);
        manager.setDefaultMaxPerRoute(10);
        config = RequestConfig.custom()
                .setConnectTimeout( (int)timeToLive * 1000)
                .setConnectionRequestTimeout((int)timeToLive*1000)
                .setSocketTimeout( (int)timeToLive * 1000)
                .build();

        client = HttpClientBuilder.create()
                .setDefaultRequestConfig( config)
                .build();
    }
    private HttpClient getClient(){
        client = HttpClients.custom().setConnectionManager(manager).setDefaultRequestConfig(config).build();
        return client;
    }
    private String executeAndConsume(HttpRequestBase httpRequestBase,Map<String,String>headers) throws IOException{
        for(Map.Entry<String,String> item : headers.entrySet()){
            httpRequestBase.addHeader(item.getKey(),item.getValue());
        }

        HttpResponse response = getClient().execute(httpRequestBase);
        String result = EntityUtils.toString(response.getEntity());
        EntityUtils.consume( response.getEntity());
        return result;

    }


    @Override
    public String get(String url,Map<String,String> headers) throws IOException {
        HttpGet httpGet = new HttpGet( url );
        return executeAndConsume(httpGet,headers);
    }

    @Override
    public String post(String url, String body,Map<String, String> headers) throws IOException {
        StringEntity entity = new StringEntity(body, ContentType.create("application/json", Charset.forName("UTF-8")));
        HttpPost httpPost = new HttpPost(url);
        httpPost.setEntity(entity);
        return executeAndConsume(httpPost, headers);
    }
}

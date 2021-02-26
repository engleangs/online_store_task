package co.onlinestore.service;

import co.onlinestore.data.Customer;
import co.onlinestore.data.CustomerRowMapper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

@Service
public class DataServiceImpl implements DataService {
    private static final Logger LOGGER = LoggerFactory.getLogger( DataServiceImpl.class);
    @Autowired
    private Gson gson;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    private static final Type MAP_TYPE = new TypeToken<Map<String,String>>(){}.getType();
    @Autowired
    private HttpService httpService;
    private final String fetchFromFB = "first_name,last_name,profile_pic";
    private final String FACEBOOK_GRAPH ="https://graph.facebook.com";

    @Override
    public Customer get(String id) {
        String sql = " SELECT * FROM ecom_customers WHERE id = ? ";
        Object[] param = {id};
        List<Customer> customerList = jdbcTemplate.query(sql, param, new CustomerRowMapper());
        if (customerList.size() == 0) {
            return null;
        }
        return customerList.get(0);
    }

    @Override
    public boolean shouldUpdate(Customer customer){
        if( customer.getUpdatedAt() == null){
            return true;
        }
        Date now = new Date();
        long diff = (  now.getTime() - customer.getUpdatedAt().getTime()) / ( 1000*3600);
        return diff > 24;
    }


    @Override
    public Customer fetchFromFb(String id,String pageToken) throws IOException {
        String url = FACEBOOK_GRAPH+"/"+id+"?fields="+fetchFromFB+"&access_token="+pageToken;
        Map<String,String> headers = Collections.singletonMap("Content-Type","application/json");
        String result =    httpService.get(url, headers);
        try{
            Map<String,String> json = gson.fromJson( result, MAP_TYPE);
            Customer customer = new Customer();
            customer.setName( json.get("first_name") +" "+json.get("last_name"));
            customer.setPhoto( json.get("profile_pic"));
            customer.setId( id );
            customer.setNewCustomer( true);
            return customer;

        }catch (Exception e){
                LOGGER.error("error deserialzied profile from JSON "+result,e);
        }
        return null;
    }

    @Override
    public String getPageToken(String pageId) {
        String sql= " SELECT configuration FROM social_pages WHERE page_id = ?"; //todo get from cache
        Object[] param = { pageId};
        List<Map<String,Object>>  items =   jdbcTemplate.queryForList(sql,param);
        if(items.size() > 0) {
            return items.get(0).get("configuration")+"";
        }
        return null;
    }

    @Override
    public String getCompanyId(String pageId) {
        String sql =" SELECT company_id FROM social_pages WHERE page_id = ? ";//todo get from cache
        Object[] param = { pageId};
        List<Map<String,Object>> items = jdbcTemplate.queryForList(sql, param);
        if( items.size() > 0) {
            return items.get(0).get("company_id")+"";
        }
        return null;
    }


    @Override
    public void storeMsg(Map<String, String> msg,long timestamp) {
        String content = msg.get("content");
        String senderId = msg.get("sender_id");
        String receiverId = msg.get("receiver_id");
        String type = msg.get("type");
        String msgId = msg.get("id");
        String pageId = msg.get("page_id");
        String companyId = getCompanyId( pageId);
        Date createdAt = new Date( timestamp);
        String sql = " INSERT INTO conversation(id,created_at,sender_id,receiver_id,content,type,page_id,company_id)" +
                "   VALUES( ? , ? , ? , ? , ? , ? , ? , ? ) ON DUPLICATE KEY UPDATE content= ? ";
        Object[] param = { msgId, createdAt, senderId,receiverId,content,type,pageId,companyId,content};
        jdbcTemplate.update(sql, param);


    }


    @Override
    public void store( Customer customer) {
        if (customer.isNewCustomer()) {
            String sql = "INSERT INTO ecom_customers(id,name,created_at,updated_at,photo) " +
                    "       VALUES ( ? , ? , ?, ?, ?) ";//todo
            Object[] param = { customer.getId() , customer.getName(), new Date(), new Date(), customer.getPhoto() };
            jdbcTemplate.update(sql, param);
        } else {
            String sql = "UPDATE ecom_customers SET name = ? ,updated_at= ?,photo = ? WHERE id = ? ";
            Object[] param = {  customer.getName(),new Date(), customer.getPhoto(), customer.getId()};
            jdbcTemplate.update( sql, param);
        }
    }


}

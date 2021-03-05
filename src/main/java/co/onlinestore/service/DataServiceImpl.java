package co.onlinestore.service;

import co.onlinestore.data.Conversation;
import co.onlinestore.data.Customer;
import co.onlinestore.data.CustomerRowMapper;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.omg.PortableInterceptor.LOCATION_FORWARD;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class DataServiceImpl implements DataService {
    private static final Logger LOGGER = LoggerFactory.getLogger( DataServiceImpl.class);
    private static final Type MAP_TYPE = new TypeToken<Map<String,String>>(){}.getType();
    private final String fetchFromFB = "first_name,last_name,profile_pic";
    private final String FACEBOOK_GRAPH ="https://graph.facebook.com";
    @Autowired
    private Gson gson;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private HttpService httpService;
    private LoadingCache<String,String> companyCache = CacheBuilder.newBuilder()
            .maximumSize(1000).
                    expireAfterAccess(5, TimeUnit.HOURS).
                    build(new CacheLoader<String, String>() {
        @Override
        public String load(String key) throws Exception {
            LOGGER.info("coming to load ..."+key);
            return getCompanyIdFromDb( key);
        }
    });

    @PostConstruct
    private void test(){
//      String compnayId =   getCompanyId("3548272971904782");
//      LOGGER.info("company id : " + compnayId);
//      LOGGER.info("compnay id : " +getCompanyId("3548272971904782"));
    }
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
        LOGGER.info("begin to fetch profile from facebook : "+url);
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
        try {

            return companyCache.getUnchecked(pageId);
        }catch (CacheLoader.InvalidCacheLoadException  e){
            return null;
        }

    }

    @Override
    public String getCompanyIdFromDb(String pageId) {

        String sql =" SELECT company_id FROM social_pages WHERE page_id = ? ";//todo get from cache
        Object[] param = { pageId};
        List<Map<String,Object>> items = jdbcTemplate.queryForList(sql, param);
        if( items.size() > 0) {
            String companyID = items.get(0).get("company_id")+"";
            return companyID;
        }
        return null;
    }


    @Override
    public void storeMsg(Conversation conversation) {

        String sql = " INSERT INTO conversation(id,created_at,sender_id,receiver_id,content,type,page_id,company_id)" +
                "   VALUES( ? , ? , ? , ? , ? , ? , ? , ? ) ON DUPLICATE KEY UPDATE content= ? ";
        Object[] param = { conversation.getId(), conversation.getCreatedAt(), conversation.getSenderId(),
                conversation.getReceiverId(),conversation.getContent(),conversation.getType(),
                conversation.getPageId(),conversation.getCompanyId(),conversation.getContent()};
        jdbcTemplate.update(sql, param);


    }


    @Override
    public void store( Customer customer) {
        if (customer.isNewCustomer()) {
            String sql = "INSERT INTO ecom_customers(id,name,created_at,updated_at,photo,company_id) " +
                    "       VALUES ( ? , ? , ?, ?, ?, ? ) ON  DUPLICATE KEY UPDATE name = ?, updated_at = ? ,photo=?  ";
            Object[] param = { customer.getId() , customer.getName(), new Date(), new Date(),
                    customer.getPhoto(), customer.getCompanyId() , customer.getName() , new Date() , customer.getPhoto()
            };
            jdbcTemplate.update(sql, param);
        } else {
            String sql = "UPDATE ecom_customers SET name = ? ,updated_at= ?,photo = ? , company_id = ? WHERE id = ? ";
            Object[] param = {  customer.getName(),new Date(), customer.getPhoto(), customer.getCompanyId(), customer.getId()};
            jdbcTemplate.update( sql, param);
        }
    }


}

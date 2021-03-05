package co.onlinestore.service;

import co.onlinestore.data.PageEntry;
import co.onlinestore.data.PageLog;
import co.onlinestore.data.Post;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.UUID;

@Service
public class DataParserObjectServiceImpl implements DataParserObjectService

{
    @Autowired
    private DataService dataService;

    private static final Logger LOGGER = LoggerFactory.getLogger( DataParserObjectServiceImpl.class);
//    @PostConstruct
//    private void test(){
//        String json ="{\n" +
//                "    \"object\": \"page\",\n" +
//                "    \"entry\": [\n" +
//                "        {\n" +
//                "            \"id\": \"678898225861636\",\n" +
//                "            \"time\": 1614690364,\n" +
//                "            \"changes\": [\n" +
//                "                {\n" +
//                "                    \"value\": {\n" +
//                "                        \"from\": {\n" +
//                "                            \"id\": \"678898225861636\",\n" +
//                "                            \"name\": \"ប្រហុកផ្អកធម្មជាតិពិត\"\n" +
//                "                        },\n" +
//                "                        \"link\": \"https://scontent.fisu3-1.fna.fbcdn.net/v/t1.0-9/fr/cp0/e15/q65/156413511_1193241964427257_8622005321871863248_o.jpg?_nc_cat=100&ccb=3&_nc_sid=110474&_nc_eui2=AeHyfsXvMGIZX1WgARsgu0yiab3h4kdnoXxpveHiR2ehfHUwl9GMPDVx6m4VufG_gYuyT9jJm_R7sz15vcM_3UXI&_nc_ohc=6bdUS-P6Mz0AX99nxN_&_nc_oc=AQn01-B9Ek57Kplk6dtKFuSLTWVytpXqBDhFSrnvCVEirpwTqajmqU602ZOGZ8RgBtk&_nc_ad=z-m&_nc_cid=1501&_nc_ht=scontent.fisu3-1.fna&tp=14&_nc_rmd=260&oh=2583db50439f7c0185c53bb4301bacd4&oe=606325EF\",\n" +
//                "                        \"message\": \"Testing post\",\n" +
//                "                        \"post_id\": \"678898225861636_1193241981093922\",\n" +
//                "                        \"created_time\": 1614690359,\n" +
//                "                        \"item\": \"photo\",\n" +
//                "                        \"photo_id\": \"1193241961093924\",\n" +
//                "                        \"published\": 1,\n" +
//                "                        \"verb\": \"add\"\n" +
//                "                    },\n" +
//                "                    \"field\": \"feed\"\n" +
//                "                }\n" +
//                "            ]\n" +
//                "        }\n" +
//                "    ]\n" +
//                "}";
//        PageEntry pageEntry = page(json);
//        LOGGER.info("parse page : "+pageEntry);
//        Post post = pagePost( json);
//        LOGGER.info("post : "+post);
//
//    }
    @Autowired
    private Gson gson;
    @Override
    public PageEntry page(String json) {
        JsonObject jsonObject =   gson.fromJson( json, JsonObject.class);
        String pageId = "";
        if( "page".equals( jsonObject.get("object").getAsString())) {
            JsonArray jsonElements =  jsonObject.get("entry").getAsJsonArray();
            if( jsonElements.size() > 0) {
                JsonObject item = jsonElements.get(0).getAsJsonObject();
                pageId = item.get("id").getAsString();
                if( item.has("messaging")) {
                    JsonArray messaging = item.get("messaging").getAsJsonArray();
                    if( messaging.size() > 0){
                        JsonObject msg = messaging.get(0).getAsJsonObject();
                        JsonObject sender = msg.get("sender").getAsJsonObject();
                       return new  PageEntry(item, pageId, sender.get("id").getAsString());

                    }
                    else {
                        LOGGER.warn("not able to parse page json string ",json);
                    }

                }
                else if ( item.has("changes")) {
                    JsonArray changes = item.get("changes").getAsJsonArray();
                    JsonObject change = changes.get(0).getAsJsonObject();
                    PageEntry pageEntry = new PageEntry( change, pageId,"");
                    if( change.has("field")) {
                        pageEntry.setField( change.get("field").getAsString());
                    }
                    if( change.has("value")) {
                        JsonObject value = change.get("value").getAsJsonObject();
                        pageEntry.setJsonObject( value);
                    }
                    return pageEntry;
                }
                else {
                    return new PageEntry( item, pageId,"");
                }
            }
        }
        return null;
    }

    @Override
    public PageLog pageLog(String json) {
        PageEntry pageEntry = page(json);
        if( pageEntry !=null){
            PageLog pageLog = new PageLog(pageEntry.getPageId() , pageEntry.getJsonObject().getAsString());
            return pageLog;
        }
        return null;
    }

    @Override
    public Post pagePost(String json) {
        PageEntry pageEntry = page( json);
        if( pageEntry !=null){
            String pageId = pageEntry.getPageId();
            JsonObject jsonObject = pageEntry.getJsonObject();
            String content = jsonObject.get("message").getAsString();
            String item = jsonObject.get("item").getAsString();
            String photo = null;
            String photoId = null;
            if( "photo".equals( item) && jsonObject.has("link")) {
                photo = jsonObject.get("link").getAsString();
                photoId = jsonObject.get("photo_id").getAsString();
            }
            String refId = jsonObject.get("post_id").getAsString();
            int published = 0;
            if( jsonObject.has("published")) {
                published = jsonObject.get("published").getAsInt();
            }
            String verb = jsonObject.get("verb").getAsString();
            String companyId = dataService.getCompanyId( pageId);
            Post post = new  Post(verb,UUID.randomUUID().toString(), content, item, "", null, new Date(), null,  companyId, refId, photoId, photo, pageId);
            post.setPublished( published);
            return post;

        }
        return null;
    }


}

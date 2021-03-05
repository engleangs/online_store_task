package co.onlinestore.service;

import co.onlinestore.data.PageEntry;
import co.onlinestore.data.PageLog;
import co.onlinestore.data.Post;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.UUID;

@Service
public class PageDataServiceImpl implements PageDataService {
    @Autowired
    DataParserObjectService parserObjectService;
    @PostConstruct
    private void testLog(){
        //store(new PageLog("0001","{testing}"));
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
//        Post post =  parserObjectService.pagePost( json);
//        store(post);
    }
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Override
    public void store(PageLog pageLog) {
        String sql = "INSERT INTO social_page_logs(id,page_id,content,created_at) " +
                " VALUES(?,?,?,?)";
        Object[] param = {UUID.randomUUID().toString(), pageLog.getPageId(), pageLog.getContent(), new Date()};
        jdbcTemplate.update( sql, param);
    }

    @Override
    public void store(Post post) {
        String sql = "INSERT INTO social_posts(id,created_at, created_by, updated_at, updated_by ," +
                " title, content,  page_id, company_id,type,photo,photo_id,ref_id,state , verb)" +
                " VALUES( ? , ? , ? , ? , ? , ? , ?, ?, ?, ? , ?, ?, ?, ? , ?) ";
        Object[] param  = { post.getId(),post.getCreatedAt(), post.getCreatedBy() , post.getUpdatedAt(), post.getUpdatedBy(), null,
                post.getContent(), post.getPageId(),post.getCompanyId(),
        post.getType(), post.getPhoto(), post.getPostRefId(), post.getPostId(), 1 , post.getVerb()};
        jdbcTemplate.update(sql, param);
    }

}

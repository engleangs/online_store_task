package co.onlinestore.service;

import co.onlinestore.data.Comment;
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
    private void testLog() {
        //store(new PageLog("0001","{testing}"));
        String json ="{\n" +
                "    \"entry\": [\n" +
                "        {\n" +
                "            \"changes\": [\n" +
                "                {\n" +
                "                    \"field\": \"feed\",\n" +
                "                    \"value\": {\n" +
                "                        \"comment_id\": \"1193236334427820_1193237614427692\",\n" +
                "                        \"created_time\": 1614689787,\n" +
                "                        \"from\": {\n" +
                "                            \"id\": \"678898225861636\",\n" +
                "                            \"name\": \"ប្រហុកផ្អកធម្មជាតិពិត\"\n" +
                "                        },\n" +
                "                        \"item\": \"comment\",\n" +
                "                        \"message\": \"Hhhh\",\n" +
                "                        \"parent_id\": \"678898225861636_1193236334427820\",\n" +
                "                        \"post\": {\n" +
                "                            \"id\": \"678898225861636_1193236334427820\",\n" +
                "                            \"is_published\": true,\n" +
                "                            \"permalink_url\": \"https://www.facebook.com/678898225861636/posts/1193236334427820/\",\n" +
                "                            \"promotion_status\": \"inactive\",\n" +
                "                            \"status_type\": \"mobile_status_update\",\n" +
                "                            \"updated_time\": \"2021-03-02T12:56:27+0000\"\n" +
                "                        },\n" +
                "                        \"post_id\": \"678898225861636_1193236334427820\",\n" +
                "                        \"verb\": \"remove\"\n" +
                "                    }\n" +
                "                }\n" +
                "            ],\n" +
                "            \"id\": \"678898225861636\",\n" +
                "            \"time\": 1614689789\n" +
                "        }\n" +
                "    ],\n" +
                "    \"object\": \"page\"\n" +
                "}";
//        Post post =  parserObjectService.pagePost( json);
//        store(post);

        Comment comment =  parserObjectService.pageComment(json);
        store(comment);
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void store(PageLog pageLog) {
        String sql = "INSERT INTO social_page_logs(id,page_id,content,created_at) " +
                " VALUES(?,?,?,?)";
        Object[] param = {UUID.randomUUID().toString(), pageLog.getPageId(), pageLog.getContent(), new Date()};
        jdbcTemplate.update(sql, param);
    }

    @Override
    public void store(Post post) {
        if("add".equals(post.getVerb())) {
            String sql = "INSERT INTO social_posts(id,created_at, created_by, updated_at, updated_by ," +
                    " title, content,  page_id, company_id,type,photo,photo_id,state , verb)" +
                    " VALUES( ? , ? , ? , ? , ? , ? , ?, ?, ?, ? , ?, ?, ? , ?) ON DUPLICATE KEY UPDATE content= ? ";
            Object[] param = {post.getId(), post.getCreatedAt(), post.getCreatedBy(), post.getUpdatedAt(), post.getUpdatedBy(), null,
                    post.getContent(), post.getPageId(), post.getCompanyId(),
                    post.getType(), post.getPhoto(), post.getPostRefId(), 1, post.getVerb(), post.getContent()};
            jdbcTemplate.update(sql, param);
        }
        else {
                int state = "update".equals(post.getVerb()) ? 1: 0;
                String sql = " UPDATE social_posts SET content=? ,type=?, photo=?, photo_id=? , verb=? , updated_at = ? , updated_by = ? , state=? WHERE id = ?";
                Object[] param = { post.getContent(), post.getType() ,post.getPhoto(), post.getPostRefId(), post.getVerb(), new Date(), post.getCreatedBy(), state, post.getId()};
                jdbcTemplate.update( sql,param);
        }
    }

    @Override
    public void store(Comment comment) {
        if("add".equals(comment.getVerb())) {
            String sql = "INSERT INTO social_comments ( id, created_at, created_by , updated_at , updated_by , company_id, post_id, photo,state, page_id, verb)  " +
                    "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ? , ?  ) ON DUPLICATE KEY UPDATE content= ?";
            if (comment.getId() == null) {
                comment.setId(UUID.randomUUID().toString());
            }
            Object[] param = {comment.getId(), comment.getCreatedAt(), comment.getCreatedBy(), comment.getUpdatedAt(),
                    comment.getUpdatedBy(), comment.getCompanyId(), comment.getPostId(), comment.getPhoto(), 1, comment.getPageId(), comment.getVerb(), comment.getContent()};
            jdbcTemplate.update(sql, param);
        }
        else {
            int state = "update".equals(comment.getVerb()) ? 1: 0;
            String sql = "UPDATE social_comments set updated_at = ?, updated_by = ?, content=?, photo= ? , type = ?, state = ? , verb = ? WHERE id = ?";
            Object[] param = { new Date(), comment.getCreatedBy(), comment.getContent(), comment.getPhoto(), comment.getType(), state, comment.getVerb(), comment.getId()};
            jdbcTemplate.update(sql, param);
        }

    }

}

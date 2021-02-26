package co.onlinestore.data;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CustomerRowMapper implements RowMapper<Customer> {

    @Override
    public Customer mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new Customer(rs.getString("id"),rs.getString("name"),rs.getString("photo"),
                rs.getDate("updated_at"),
                rs.getString("created_by"),
                rs.getDate("created_at")
                );
    }
}

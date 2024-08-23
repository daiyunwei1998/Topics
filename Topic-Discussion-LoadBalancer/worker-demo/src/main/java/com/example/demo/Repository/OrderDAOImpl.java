package com.example.demo.Repository;

import com.example.demo.model.Order;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import org.springframework.stereotype.Repository;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
@Log4j2
public class OrderDAOImpl implements OrderDAO {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    public OrderDAOImpl(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Order> getOrdersTotal() {
        String sql = "SELECT user_id, total FROM orders";

        return jdbcTemplate.query(sql, new OrderRowMapper());
    }

    private static class OrderRowMapper implements RowMapper<Order> {

        @Override
        public Order mapRow(ResultSet rs, int rowNum) throws SQLException {
            Order order = new Order();
            order.setTotal(rs.getBigDecimal("total"));
            order.setUserId(rs.getLong("user_id"));
            return order;
        }
    }
}
package com.example.demo.Repository;




import com.example.demo.model.Order;

import java.util.List;

public interface OrderDAO {
    List<Order> getOrdersTotal();
}

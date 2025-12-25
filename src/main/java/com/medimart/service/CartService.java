// src/main/java/com/medimart/service/CartService.java
package com.medimart.service;

import com.medimart.dto.CartItemDto;
import java.util.List;

public interface CartService {

    List<CartItemDto> getCartByUser(Long userId);   // 👈 DTO here

    boolean addItem(Long userId, Long medicineId, int quantity);

    boolean updateQuantity(Long itemId, int qty);

    void removeItem(Long itemId);

    void clearUserCart(Long userId);
}

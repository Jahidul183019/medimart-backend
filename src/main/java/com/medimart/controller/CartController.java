package com.medimart.controller;

import com.medimart.dto.CartItemDto;
import com.medimart.model.CartItem;
import com.medimart.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CartController {

    private final CartService cartService;

    // ----------------- GET CART FOR USER -----------------
    // GET /api/cart/{userId}
    @GetMapping("/{userId}")
    public List<CartItemDto> getUserCart(@PathVariable Long userId) {
        return cartService.getCartByUser(userId);
    }

    // ----------------- ADD ITEM -----------------
    // POST /api/cart/add
    // body: { userId, medicineId, quantity }
    @PostMapping("/add")
    public ResponseEntity<?> addToCart(@RequestBody CartItem req) {
        try {
            if (req.getUserId() == null || req.getMedicineId() == null) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body("Missing userId or medicineId");
            }

            int qty = req.getQuantity() <= 0 ? 1 : req.getQuantity();

            cartService.addItem(req.getUserId(), req.getMedicineId(), qty);

            List<CartItemDto> updatedCart = cartService.getCartByUser(req.getUserId());
            return ResponseEntity.ok(updatedCart);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Add failed: " + e.getMessage());
        }
    }

    // ----------------- UPDATE ITEM QUANTITY -----------------
    // PUT /api/cart/item/{itemId}
    @PutMapping("/item/{itemId}")
    public ResponseEntity<?> updateItem(
            @PathVariable Long itemId,
            @RequestBody CartItem req
    ) {
        try {
            if (req.getUserId() == null) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body("Missing userId");
            }

            int qty = req.getQuantity() <= 0 ? 1 : req.getQuantity();
            boolean ok = cartService.updateQuantity(itemId, qty);

            if (!ok) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body("Update failed (item not found)");
            }

            List<CartItemDto> updatedCart = cartService.getCartByUser(req.getUserId());
            return ResponseEntity.ok(updatedCart);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Update failed: " + e.getMessage());
        }
    }

    // ----------------- REMOVE ONE ITEM -----------------
    // DELETE /api/cart/item/{itemId}
    @DeleteMapping("/item/{itemId}")
    public ResponseEntity<Void> removeItem(@PathVariable Long itemId) {
        cartService.removeItem(itemId);
        return ResponseEntity.noContent().build();
    }

    // ----------------- CLEAR CART -----------------
    // DELETE /api/cart/{userId}
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> clearCart(@PathVariable Long userId) {
        cartService.clearUserCart(userId);
        return ResponseEntity.noContent().build();
    }
}

package com.shoestore.backend.scheduler;

import com.shoestore.backend.model.Cart;
import com.shoestore.backend.model.CartItem;
import com.shoestore.backend.repository.CartItemRepository;
import com.shoestore.backend.repository.CartRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CartCleanupScheduler {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void cleanupExpiredCarts() {
        List<Cart> cartList = cartRepository.findByLastActivityAtBefore(LocalDateTime.now().minusDays(30));

        for (Cart cart : cartList) {
            cartItemRepository.deleteByCartId(cart.getId());
            cartRepository.delete(cart);
        }
    }
}

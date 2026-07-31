package com.shoestore.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shoestore.backend.dto.cart.CartMergeRequestDto;
import com.shoestore.backend.dto.cart.CartResponseDto;
import com.shoestore.backend.dto.cartitem.CartItemDto;
import com.shoestore.backend.dto.cartitem.CreateCartItemRequestDto;
import com.shoestore.backend.dto.cartitem.UpdateCartItemRequestDto;
import com.shoestore.backend.security.jwt.JwtUtil;
import com.shoestore.backend.service.CartService;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CartController.class)
@AutoConfigureMockMvc(addFilters = false)
public class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private CartService cartService;
    @MockitoBean
    private JwtUtil jwtUtil;
    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    void getCartReturnsCart() throws Exception {
        when(cartService.getCart(1L)).thenReturn(cartResponse());

        mockMvc.perform(get("/api/carts/{cartId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartId").value(1L))
                .andExpect(jsonPath("$.cartItems[0].name").value("Runner"));

        verify(cartService).getCart(1L);
    }

    @Test
    void addItemToCartReturnsCart() throws Exception {
        CreateCartItemRequestDto request = new CreateCartItemRequestDto(null, 10L, 2);
        when(cartService.addItemToCart(any(CreateCartItemRequestDto.class),
                nullable(Authentication.class))).thenReturn(cartResponse());

        mockMvc.perform(post("/api/carts/items")
                        .with(user("john@example.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productsCount").value(2));

        verify(cartService).addItemToCart(any(CreateCartItemRequestDto.class),
                nullable(Authentication.class));
    }

    @Test
    void addItemToCartReturnsBadRequestForInvalidQuantity() throws Exception {
        CreateCartItemRequestDto request = new CreateCartItemRequestDto(null, 10L, 0);

        mockMvc.perform(post("/api/carts/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void changeQuantityReturnsCart() throws Exception {
        UpdateCartItemRequestDto request = new UpdateCartItemRequestDto(3);
        when(cartService.changeQuantity(eq(1L), eq(2L), any(UpdateCartItemRequestDto.class)))
                .thenReturn(cartResponse());

        mockMvc.perform(patch("/api/carts/{cartId}/items/{cartItemId}", 1L, 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartId").value(1L));

        verify(cartService).changeQuantity(eq(1L), eq(2L), any(UpdateCartItemRequestDto.class));
    }

    @Test
    void deleteCartItemReturnsCart() throws Exception {
        when(cartService.deleteCartItem(1L, 2L)).thenReturn(cartResponse());

        mockMvc.perform(delete("/api/carts/{cartId}/items/{cartItemId}", 1L, 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartId").value(1L));

        verify(cartService).deleteCartItem(1L, 2L);
    }

    @Test
    void mergeCartsReturnsCart() throws Exception {
        CartMergeRequestDto request = new CartMergeRequestDto(1L);
        when(cartService.mergeCarts(any(CartMergeRequestDto.class),
                nullable(Authentication.class))).thenReturn(cartResponse());

        mockMvc.perform(post("/api/carts/merge")
                        .with(user("john@example.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartSubtotal").value(198));

        verify(cartService).mergeCarts(any(CartMergeRequestDto.class),
                nullable(Authentication.class));
    }

    private CartResponseDto cartResponse() {
        CartItemDto item = new CartItemDto(
                2L,
                "Runner",
                BigDecimal.valueOf(99),
                null,
                "Black",
                "42",
                2,
                BigDecimal.valueOf(198),
                "main.jpg"
        );
        return new CartResponseDto(1L, 2, BigDecimal.valueOf(198), List.of(item));
    }
}

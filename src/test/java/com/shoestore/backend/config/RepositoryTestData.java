package com.shoestore.backend.config;

import com.shoestore.backend.model.AuthProvider;
import com.shoestore.backend.model.Cart;
import com.shoestore.backend.model.Channel;
import com.shoestore.backend.model.DeliveryType;
import com.shoestore.backend.model.DeviceType;
import com.shoestore.backend.model.Discount;
import com.shoestore.backend.model.Order;
import com.shoestore.backend.model.OrderStatus;
import com.shoestore.backend.model.Payment;
import com.shoestore.backend.model.PaymentStatus;
import com.shoestore.backend.model.PaymentType;
import com.shoestore.backend.model.Product;
import com.shoestore.backend.model.ProductImage;
import com.shoestore.backend.model.ProductVariant;
import com.shoestore.backend.model.Role;
import com.shoestore.backend.model.RoleName;
import com.shoestore.backend.model.Session;
import com.shoestore.backend.model.User;
import jakarta.persistence.NoResultException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

public final class RepositoryTestData {

    private RepositoryTestData() {
    }

    public static Role customerRole(TestEntityManager entityManager) {
        try {
            return entityManager.getEntityManager()
                    .createQuery("select r from Role r where r.roleName = :roleName", Role.class)
                    .setParameter("roleName", RoleName.CUSTOMER)
                    .getSingleResult();
        } catch (NoResultException expected) {
            Role role = new Role();
            role.setRoleName(RoleName.CUSTOMER);
            return entityManager.persistAndFlush(role);
        }
    }

    public static User user(TestEntityManager entityManager, String email) {
        User user = new User()
                .setFirstName("John")
                .setLastName("Doe")
                .setEmail(email)
                .setAuthProvider(AuthProvider.LOCAL)
                .setPhoneNumber("+48123456789")
                .setPassword("encoded-password")
                .setRole(customerRole(entityManager));
        return entityManager.persistAndFlush(user);
    }

    public static Product product(TestEntityManager entityManager, String name) {
        Product product = new Product()
                .setCategory("Sneakers")
                .setName(name)
                .setDescription("Comfort shoes")
                .setPrice(BigDecimal.valueOf(99))
                .setGender("MEN")
                .setSeason("SUMMER")
                .setMaterial("Leather");
        return entityManager.persistAndFlush(product);
    }

    public static ProductImage image(TestEntityManager entityManager, Product product,
                                     String color) {
        ProductImage image = new ProductImage()
                .setProduct(product)
                .setColor(color)
                .setMainUrl("main-" + color + ".jpg")
                .setUrls(List.of("side-" + color + ".jpg"));
        return entityManager.persistAndFlush(image);
    }

    public static ProductVariant variant(TestEntityManager entityManager,
                                         Product product,
                                         String color,
                                         String size,
                                         String sku) {
        ProductVariant variant = new ProductVariant()
                .setProduct(product)
                .setColor(color)
                .setSize(size)
                .setStockQty(5)
                .setSku(sku);
        return entityManager.persistAndFlush(variant);
    }

    public static Cart cart(TestEntityManager entityManager, User user,
                            LocalDateTime lastActivityAt) {
        Cart cart = new Cart()
                .setUser(user)
                .setLastActivityAt(lastActivityAt);
        return entityManager.persistAndFlush(cart);
    }

    public static Order order(TestEntityManager entityManager, User user) {
        Order order = new Order()
                .setUser(user)
                .setStatus(OrderStatus.PENDING)
                .setTotalAmount(BigDecimal.valueOf(198))
                .setDiscountAmount(BigDecimal.ZERO)
                .setFinalAmount(BigDecimal.valueOf(198))
                .setCustomerFirstName("John")
                .setCustomerLastName("Doe")
                .setCustomerPhone("+48123456789")
                .setCustomerEmail(user.getEmail())
                .setDeliveryAddress("Street 1")
                .setRecipientName("John Doe")
                .setRecipientPhone("+48123456789")
                .setDeliveryType(DeliveryType.COURIER)
                .setPaymentType(PaymentType.CARD);
        return entityManager.persistAndFlush(order);
    }

    public static Payment payment(TestEntityManager entityManager, Order order, String sessionId) {
        Payment payment = new Payment()
                .setOrder(order)
                .setPaymentStatus(PaymentStatus.PENDING)
                .setSessionUrl("https://checkout.stripe.test/" + sessionId)
                .setSessionId(sessionId)
                .setAmount(order.getFinalAmount());
        return entityManager.persistAndFlush(payment);
    }

    public static Discount discount(TestEntityManager entityManager, String code) {
        Discount discount = new Discount()
                .setCode(code)
                .setDiscountPercentage(BigDecimal.TEN);
        return entityManager.persistAndFlush(discount);
    }

    public static Session session(TestEntityManager entityManager, User user) {
        Session session = new Session()
                .setUser(user)
                .setDeviceType(DeviceType.DESKTOP)
                .setBrowser("Chrome")
                .setCountry("Poland")
                .setChannel(Channel.DIRECT);
        return entityManager.persistAndFlush(session);
    }

    public static void flushAndClear(TestEntityManager entityManager) {
        entityManager.flush();
        entityManager.clear();
    }
}

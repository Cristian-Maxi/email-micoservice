package microservice.email.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreatedEvent {
    private Long orderId;
    private Long userId;
    private String email;
    private List<OrderItemDetails> orderItems;

//    @Data
//    @NoArgsConstructor
//    @AllArgsConstructor
//    public static class OrderItemDetails {
//        private Long productId;
//        private String productName;
//        private Integer quantity;
//    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemDetails {
        private Long productId;
        private String name;
        private String description;
        private Double price;
        private Integer stock;
        private Integer quantity;
    }

    public OrderCreatedEvent(Long orderId, Long userId, List<OrderItemDetails> orderItems) {
        this.orderId = orderId;
        this.userId = userId;
        this.orderItems = orderItems;
    }
}
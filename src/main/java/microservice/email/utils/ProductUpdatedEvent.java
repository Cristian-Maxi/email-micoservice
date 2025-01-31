package microservice.email.utils;

import lombok.Data;

@Data
public class ProductUpdatedEvent {
    private Long productId;
    private String productName;
    private int oldStock;
    private int newStock;

    public ProductUpdatedEvent(Long productId, String productName, int oldStock, int newStock) {
        this.productId = productId;
        this.productName = productName;
        this.oldStock = oldStock;
        this.newStock = newStock;
    }
}
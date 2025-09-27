package com.example.blockchain.dto;
import com.example.blockchain.entity.Product;
import lombok.Data;

import java.util.Date;

@Data
public class ProductDTO {

    private Integer productId;  // 商品ID
    private String productName;  // 商品名称
    private Date creationDate;  // 生产日期
    private String imagePath;  // 图片路径

    public ProductDTO() {
        // 默认构造函数
    }

    // 使用Product实体创建ProductDTO
    public ProductDTO(Integer productId, String productName, Date creationDate, String imagePath) {
        this.productId = productId;
        this.productName = productName;
        this.creationDate = creationDate;
        this.imagePath = imagePath;
    }

    // 从Product实体创建ProductDTO
    public ProductDTO(Product product) {
        this.productId = product.getProductId();
        this.productName = product.getProductName();
        this.creationDate = product.getCreationDate();
        this.imagePath = product.getImagePath();
    }
}



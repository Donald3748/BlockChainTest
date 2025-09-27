package com.example.blockchain.entity;


import lombok.Data;
import jakarta.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "product")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Integer productId;  // 产品ID

    @Column(name = "product_name")
    private String productName;  // 产品名称

    @Column(name = "creation_date")
    private Date creationDate;  // 生产日期

    @Column(name = "image_path")
    private String imagePath;  // 图片路径

}

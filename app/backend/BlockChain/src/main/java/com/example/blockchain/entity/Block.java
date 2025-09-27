package com.example.blockchain.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Data
@Entity
@Table(name = "block")
public class Block {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "block_id")
    private Integer blockId;  // 区块ID

    @Column(name = "product_id")
    private Integer productId;

    @Column(name = "product_status")
    private String productStatus;  // 产品状态（生产中，运输中，待售，已售）

    @Column(name = "data")
    private String data;  // 存储区块的详细信息

    @Column(name = "timestamp")
    private Date timestamp;  // 区块创建时间

    @Column(name = "previous_hash")
    private String previousHash;  // 上一个区块的哈希值

    @Column(name = "current_hash")
    private String currentHash;  // 当前区块的哈希值

    @Column(name = "transaction_signature", length = 512)
    private String transactionSignature;

}

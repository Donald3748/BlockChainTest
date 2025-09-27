package com.example.blockchain.dto;
import com.example.blockchain.entity.Block;
import lombok.Data;

import java.util.Date;

@Data
public class BlockDTO {

    private Integer blockId;  // 区块ID
    private Integer productId;  // 产品ID
    private String productStatus;  // 产品状态（生产中，运输中，待售，已售）
    private String data;  // 区块的详细信息
    private Date timestamp;  // 区块创建时间
    private String previousHash;  // 上一个区块的哈希值
    private String currentHash;  // 当前区块的哈希值
    private String transactionSignature;  // 签名
    public BlockDTO(Integer blockId, Integer productId, String productStatus,
                    String data, Date timestamp, String previousHash, String currentHash,
                    String transactionSignature) {
        this.blockId = blockId;
        this.productId = productId;
        this.productStatus = productStatus;
        this.data = data;
        this.timestamp = timestamp;
        this.previousHash = previousHash;
        this.currentHash = currentHash;
        this.transactionSignature = transactionSignature;
    }

    public BlockDTO(Block block) {
        this.blockId = block.getBlockId();
        this.productId = block.getProductId();
        this.productStatus = block.getProductStatus();
        this.data = block.getData();
        this.timestamp = block.getTimestamp();
        this.previousHash = block.getPreviousHash();
        this.currentHash = block.getCurrentHash();
        this.transactionSignature = block.getTransactionSignature();
    }
}

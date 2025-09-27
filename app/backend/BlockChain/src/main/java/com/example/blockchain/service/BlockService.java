package com.example.blockchain.service;

import com.example.blockchain.dto.BlockDTO;
import com.example.blockchain.entity.Block;
import com.example.blockchain.entity.Product;
import com.example.blockchain.repository.BlockRepository;
import com.example.blockchain.repository.ProductRepository;
import com.example.blockchain.util.HashUtil;
import com.example.blockchain.util.SignatureUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BlockService {

    @Autowired
    private BlockRepository blockRepository;

    @Autowired
    private ProductRepository productRepository;

    public List<BlockDTO> getBlocksByProductId(Integer productId) {
        try {
            List<Block> blocks = blockRepository.findByProductId(productId);

            // 加载公钥
            String publicKeyPath = "src/main/resources/public.pem";
            PublicKey publicKey = SignatureUtil.loadPublicKey(publicKeyPath);

            // 验证每个区块的签名
            for (Block block : blocks) {
                boolean valid = SignatureUtil.verify(block.getCurrentHash(), block.getTransactionSignature(), publicKey);
                if (!valid) {
                    throw new SecurityException("Signature verification failed for block ID: " + block.getBlockId());
                }
            }

            return blocks.stream().map(BlockDTO::new).collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("数据校验失败：" + e.getMessage());
        }
    }


    public Block addBlock(Block block) {
        try {
            Block newBlock = new Block();
            newBlock.setProductId(block.getProductId());
            newBlock.setData(block.getData());
            newBlock.setProductStatus(block.getProductStatus());
            newBlock.setTimestamp(new Date());

            // 获取上一个区块的哈希
            Block lastBlock = blockRepository.findTopByProductIdOrderByTimestampDesc(block.getProductId());
            String previousHash = (lastBlock != null) ? lastBlock.getCurrentHash() : "0";
            newBlock.setPreviousHash(previousHash);

            // 执行 PoW
            int difficulty = 4;
            String currentHash = HashUtil.mineBlock(newBlock, difficulty);
            newBlock.setCurrentHash(currentHash);

            // 签名 currentHash
            String privateKeyPath = "src/main/resources/private.pem";
            PrivateKey privateKey = SignatureUtil.loadPrivateKey(privateKeyPath);
            String signature = SignatureUtil.sign(currentHash, privateKey);
            newBlock.setTransactionSignature(signature);

            return blockRepository.save(newBlock);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

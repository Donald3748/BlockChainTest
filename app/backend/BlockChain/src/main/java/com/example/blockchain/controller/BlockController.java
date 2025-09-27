package com.example.blockchain.controller;

import com.example.blockchain.dto.BlockDTO;
import com.example.blockchain.dto.ProductDTO;
import com.example.blockchain.entity.Block;
import com.example.blockchain.service.BlockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/blocks")
public class BlockController {

    @Autowired
    private BlockService blockService;

    @PostMapping("/add")
    public ResponseEntity<String> addBlock(@RequestBody Block newBlock) {
        try {
            Block block = blockService.addBlock(newBlock);
            if (block != null) {
                return ResponseEntity.ok("区块添加成功！");
            } else {
                return ResponseEntity.status(500).body("添加区块失败，请稍后再试。");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("添加区块失败，请稍后再试。");
        }
    }

    @GetMapping("/findByProductId/{productId}")
    public ResponseEntity<?> getBlocksByProductId(@PathVariable Integer productId) {
        try {
            List<BlockDTO> blocks = blockService.getBlocksByProductId(productId);
            return ResponseEntity.ok(blocks);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("签名验证失败，数据可能被篡改。");
        }
    }

}

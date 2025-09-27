package com.example.blockchain.controller;

import com.example.blockchain.dto.ProductDTO;
import com.example.blockchain.entity.Block;
import com.example.blockchain.entity.Product;
import com.example.blockchain.service.ProductService;
import com.example.blockchain.util.FileUploadUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;


@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @PostMapping("/upload")
    public ResponseEntity<ProductDTO> addProduct(
            HttpServletRequest request,
            @RequestParam(value = "productName") String productName,
            @RequestParam(value = "file", required = false) MultipartFile file) {

        ProductDTO productDTO = new ProductDTO();
        productDTO.setProductName(productName);

        // 校验逻辑
        if (productDTO.getProductName() == null || productDTO.getProductName().isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }

        try {
            ProductDTO savedProduct = productService.addProduct(productDTO, file);

            return ResponseEntity.ok(savedProduct);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        } catch (IOException e) {
            return ResponseEntity.status(500).body(null);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }

    // 查询所有产品
    @GetMapping("/get-list")
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        try {
            // 获取所有商品
            List<ProductDTO> productDTOList = productService.getAllProducts();
            return ResponseEntity.ok(productDTOList);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);  // 如果发生异常，返回500错误
        }
    }
    @GetMapping("/findById/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Integer id) {
        try {
            // 获取商品详情
            ProductDTO productDTO = productService.getById(id);
            return ResponseEntity.ok(productDTO);  // 返回 200 OK 和商品详情
        } catch (Exception e) {
            // 如果没有找到商品，返回 404 Not Found
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

}

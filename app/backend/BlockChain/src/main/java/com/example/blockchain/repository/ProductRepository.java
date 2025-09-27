package com.example.blockchain.repository;

import com.example.blockchain.dto.ProductDTO;
import com.example.blockchain.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
    Product findByProductId(Integer productId);
}

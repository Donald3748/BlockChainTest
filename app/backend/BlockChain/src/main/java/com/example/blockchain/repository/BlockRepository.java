package com.example.blockchain.repository;

import com.example.blockchain.entity.Block;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BlockRepository extends JpaRepository<Block, Integer> {
    List<Block> findByProductId(Integer productId);

    Block findTopByProductIdOrderByTimestampDesc(Integer productId);
}

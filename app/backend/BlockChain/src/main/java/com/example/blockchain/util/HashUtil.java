package com.example.blockchain.util;

import com.example.blockchain.entity.Block;

import java.security.MessageDigest;

public class HashUtil {

    public static String calculateHash(String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes("UTF-8"));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append("0");
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String mineBlock(Block block, int difficulty) {
        String prefix = "0".repeat(difficulty);
        long nonce = 0;

        String hash;
        do {
            nonce++;
            String rawData = block.getProductId()
                    + block.getProductStatus()
                    + block.getData()
                    + block.getTimestamp().getTime()
                    + block.getPreviousHash()
                    + nonce;
            hash = calculateHash(rawData);
        } while (!hash.startsWith(prefix));

        // 挖矿成功，返回 hash
        return hash;
    }
}

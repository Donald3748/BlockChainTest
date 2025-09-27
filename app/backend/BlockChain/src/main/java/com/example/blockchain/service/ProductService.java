package com.example.blockchain.service;

import com.example.blockchain.dto.ProductDTO;
import com.example.blockchain.entity.Block;
import com.example.blockchain.entity.Product;
import com.example.blockchain.repository.BlockRepository;
import com.example.blockchain.repository.ProductRepository;
import com.example.blockchain.util.FileUploadUtil;
import com.example.blockchain.util.HashUtil;
import com.example.blockchain.util.SignatureUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.PrivateKey;
import java.util.List;
import java.util.Optional;
import java.util.Date;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private BlockRepository blockRepository;


    public ProductDTO getById(Integer productId) {
        Product product = productRepository.findByProductId(productId);
        return new ProductDTO(product);  // 将 Product 转换为 ProductDTO
    }


    // 获取所有产品，并返回ProductDTO列表
    public List<ProductDTO> getAllProducts() {
        List<Product> products = productRepository.findAll();
        // 将Product实体转换为ProductDTO
        return products.stream()
                .map(product -> new ProductDTO(product))
                .collect(Collectors.toList());
    }

    // 增加产品
    public ProductDTO addProduct(ProductDTO productDTO, MultipartFile file) throws IOException {
        System.out.println(productDTO.getProductName());

        try{
            // 保存商品到数据库之前，不使用productId，因为productId是自增的
            Product product = new Product();
            product.setProductName(productDTO.getProductName());
            product.setCreationDate(new Date());  // 使用当前时间设置生产日期

            // 保存商品到数据库（数据库会生成自增的productId）
            Product savedProduct = productRepository.save(product);

            // 现在可以获取自增的productId
            int productId = savedProduct.getProductId();

            // 上传图片并获取路径
            String imagePath = null;
            if (file != null && !file.isEmpty()) {
                // 使用productId来为图片命名
                imagePath = FileUploadUtil.saveFile(file, productId, "uploads/images");
            }

            // 更新保存后的商品对象，保存图片路径
            savedProduct.setImagePath(imagePath);
            productRepository.save(savedProduct);  // 更新商品的图片路径

            // 生成新商品时默认创建第一个空节点
            Block newBlock = new Block();
            newBlock.setProductId(productId);
            newBlock.setProductStatus("起始节点");
            newBlock.setData("创建新的商品时自动生成的节点，不储存数据");
            newBlock.setTimestamp(new Date());
            newBlock.setPreviousHash("0");
            int difficulty = 4;
            String currentHash = HashUtil.mineBlock(newBlock, difficulty);
            newBlock.setCurrentHash(currentHash);

            String privateKeyPath = "src/main/resources/private.pem";
            PrivateKey privateKey = SignatureUtil.loadPrivateKey(privateKeyPath);
            String signature = SignatureUtil.sign(currentHash, privateKey);
            newBlock.setTransactionSignature(signature);
            blockRepository.save(newBlock);

            // 返回保存后的商品DTO
            return new ProductDTO(savedProduct);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}

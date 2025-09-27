[toc]

# 项目说明文档

## 1 项目概述

在当前社会背景下，茅台酒作为高价值、高溢价的代表性产品，其真伪问题日益受到消费者和监管部门关注。传统的商品溯源方式依赖中心化数据库，存在数据篡改、伪造记录、责任不清等问题。

所以本项目以区块链不可篡改、可追溯、分布式存储等特性为核心，结合状态记录机制，构建一个完整的商品生命周期管理系统，以实现对茅台酒产品从生产到流通再到消费的全过程、全链条的透明追踪。

本系统为信息安全与基础课程设计项目，涵盖前后端开发、数据库设计、信息安全等综合知识，具有实际应用价值与工程实践意义。

## 2 项目部署

**前端部署：**

```bash
cd frontend
npm install
npm run dev
```

然后浏览器访问 `http://localhost:5173`

**后端部署：**

使用 IntelliJ IDEA 启动 `BlockChainApplication.java`，使其默认运行在 `http://localhost:8080`

数据库中的表将在连接本地数据库之后由 JPA 自动生成。

## 3 系统设计

本系统以“每件商品形成一条独立区块链”为核心思想，结合商品注册、状态变更、图像存证等功能构建完整流程。设计采用“前后端分离”架构，提升模块解耦能力与后期可维护性。

系统的核心设计如下：

1. **每件茅台酒商品具有识别编号（productId）**：作为溯源商品时的唯一标识；
2. **区块链结构设计**：每个商品（每个ProductId）对应唯一一条链，详细记录商品状态的演变过程（生产中，运输中，待售，已售）。
3. **区块哈希值通过 SHA-256 实现哈希计算，并通过 RSA 私钥对区块哈希签名**：链中的每个区块均包含自身数据的哈希值与前一个区块的哈希值，形成不可篡改的数据链。同时每个区块都具有由自身哈希值通过RSA私钥加密后的唯一签名，在前端发送获取商品区块链请求时会先**通过公钥验证区块数据是否被篡改**，以维持数据的完整性与安全性。
4. **前端用户上传的商品图片会存储至服务器本地目录**：供用户前端可视化展示与识别，同时保存商品的数据。
5. **用户在前端通过表单操作新建商品和区块添加**：具有简洁清晰的界面，适用于一般消费者或监管人员操作使用。

### 3.1 后端设计

本系统后端基于 **Spring Boot 框架**构建，采用了典型的三层架构（Controller、Service、Repository）进行模块划分，同时引入 **MySQL 数据库**进行数据持久化存储，结合 **SHA-256 哈希算法与非对称加密（RSA 签名/验签）机制**，构建出完整且安全的区块链式商品溯源数据链路。

#### 3.1.1 项目结构概览

```
backend/BlockChain
├── uploads/images/                   # 上传商品图片存储目录
├── src/main/java/com/example/blockchain/
│   ├── config/                       # 跨域配置类
│   ├── controller/                   # 接收请求，返回数据
│   ├── entity/                       # 与数据库表对应的实体类
│   ├── dto/                          # 请求与响应数据结构
│   ├── repository/                   # 数据库操作接口
│   ├── service/                      # 核心业务逻辑层
│   ├── util/                         # 工具类：加密、哈希、文件上传处理
│   └── BlockChainApplication.java    # 应用主入口
├── src/main/resources/
│   ├── application.properties        # 应用配置
│   ├── private.pem                   # RSA 私钥文件（用于签名）
│   └── public.pem                    # RSA 公钥文件（用于验签）
└── pom.xml                           # Maven 依赖配置
```

------

#### 3.1.2 模块职责说明

| 模块名       | 说明                                                         |
| ------------ | ------------------------------------------------------------ |
| `controller` | 控制器层，定义 RESTful 接口，如 `/products/upload`、`/blocks/findByProductId`。 |
| `service`    | 核心业务逻辑，如商品注册、区块生成、哈希计算、签名与验签等。 |
| `entity`     | 映射数据库表的持久化类，包括 `Product`（商品）和 `Block`（区块）两张表。 |
| `dto`        | 数据传输对象，用于前后端交互时格式化请求与响应内容。         |
| `repository` | Spring Data JPA 接口，封装对 `MySQL` 的操作，如按商品编号查询所有区块。 |
| `util`       | 加密工具类：包括 SHA-256 哈希算法、RSA 签名与验签、图片文件保存处理。 |

------

#### 3.1.3 数据模型设计

`products`：

| 字段名        | 类型    | 描述                 |
| ------------- | ------- | -------------------- |
| id            | int     | 主键，自增           |
| product_id    | varchar | 商品唯一编号（UUID） |
| product_name  | varchar | 商品名称             |
| creation_date | daytime | 商品的创建时间       |
| image_url     | varchar | 图片访问路径         |

`blocks`：

| 字段名                | 类型     | 描述                   |
| --------------------- | -------- | ---------------------- |
| block_id              | int      | 主键                   |
| product_id            | int      | 对应商品编号           |
| previous_hash         | varchar  | 上一区块哈希值         |
| current_hash          | varchar  | 当前区块哈希值         |
| timestamp             | datetime | 生成时间戳             |
| product_status        | varchar  | 备注说明，如“运输中”   |
| transaction_signature | varchar  | 哈希签名值（私钥签名） |
| data                  | varchar  | 储存当前区块的数据     |

每个商品的多个区块通过 `previousHash` 形成逻辑链式结构，实现商品流转链条的不可篡改性。

#### 3.1.4 加密与安全设计

为了实现**数据不可伪造、不可篡改、可追溯**，后端实现了如下安全机制：

1. **哈希生成与工作量证明机制（防篡改、防刷写）**

   系统在创建新区块时使用 **SHA-256 哈希算法** 生成区块的唯一标识 `currentHash`，该值依赖以下字段组合生成：

   - `productId`
   - `productStatus`
   - `data`（区块内容）
   - `timestamp`
   - `previousHash`
   - `nonce`（随机数）

   同时，为提升数据篡改难度，引入了简化版的 **PoW共识协议，即工作量证明机制（Proof of Work）**，即**必须找到一个 `nonce` 使得计算出的哈希值以若干个 0 开头。**

   PoW 实现代码如下：

   ```java
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
   
       block.setNonce(nonce);
       return hash;
   }
   ```

   该机制确保每个区块的生成具有一定计算成本，恶意节点无法批量伪造或重写历史记录。

2. **RSA 数字签名机制（防伪造）**

   为确保区块数据由系统生成且不可伪造，系统使用 RSA 非对称加密算法对哈希值进行签名。签名过程如下：

   - 使用**私钥**对哈希值签名：

   ```java
   public static String sign(String data, PrivateKey privateKey) throws Exception {
       Signature rsa = Signature.getInstance("SHA256withRSA");
       rsa.initSign(privateKey);
       rsa.update(data.getBytes(StandardCharsets.UTF_8));
       return Base64.getEncoder().encodeToString(rsa.sign());
   }
   ```

   - 在添加区块过程中，系统自动执行如下步骤：

   ```java
   String privateKeyPath = "src/main/resources/private.pem";
   PrivateKey privateKey = SignatureUtil.loadPrivateKey(privateKeyPath);
   
   String currentHash = HashUtil.mineBlock(block, DIFFICULTY); // 先 PoW
   String signature = SignatureUtil.sign(currentHash, privateKey);
   
   block.setCurrentHash(currentHash);
   block.setTransactionSignature(signature);
   ```

   - 每个区块保存 `currentHash` 与 `transactionSignature` 字段。

3. **签名验签机制（完整性验证）**

   当用户通过 `/blocks/findByProductId/{productId}` 查询区块链时，系统自动使用服务端公钥对每个区块验签：

   ```java
   for (Block block : blocks) {
       boolean valid = SignatureUtil.verify(
           block.getCurrentHash(),
           block.getTransactionSignature(),
           publicKey
       );
       if (!valid) {
           throw new SecurityException("Signature verification failed for block ID: "
                                       + block.getBlockId());
       }
   }
   ```

   如果验签失败，系统将拒绝返回该区块，提示数据可能遭到非法篡改。

4. **私钥保护机制**
   - 私钥文件存储在后端 `resources/private.pem`，仅服务端可访问；
   - 加载时使用专用方法进行解析，避免泄露；
   - 公钥文件为 `public.pem`，可对外开放用于验证签名，不涉及数据生成权限。

------

#### 3.1.5 图片上传与路径映射

- 用户通过前端表单上传商品图片；
- 后端接口 `/products/upload` 使用 `MultipartFile` 接收图片，保存在 `uploads/images/` 文件夹；
- 同时将图片路径记录到 `Product` 实体中；
- 前端通过图片 URL（如 `http://localhost:8080/images/xxx.jpg`）展示商品封面图。

后端通过引入哈希链结构、数字签名机制以及图片存储逻辑，不仅实现了商品信息的安全登记和过程记录，还保证了溯源数据的**真实性**、**完整性**与**可验证性**。该架构可轻松扩展至其他高价值商品（如药品、文物）等场景，具备良好的实际应用潜力。

------

### 3.2 前端设计

本系统前端采用Vue3 框架开发，基于组件化思想构建单页面应用（SPA），实现商品登记、状态记录、区块链展示等功能。通过调用后端 REST API 接口，完成商品与区块的增查操作，并结合本地图片上传与展示，实现用户友好的溯源体验。

#### 3.2.1 项目结构概览

```
frontend/src/
├── views/
│   ├── AddBlock.vue            # 添加区块信息
│   ├── AddProduct.vue          # 添加新商品
│   ├── ProductList.vue         # 展示所有商品信息
│   └── ProductDetail.vue       # 展示某商品区块链详情
├── router/
│   └── index.js                # 路由配置
├── App.vue                     
└── main.js                    
```

#### 3.2.2 接口说明

| 接口路径                       | 方法 | 描述                         |
| ------------------------------ | ---- | ---------------------------- |
| `/products/upload`             | POST | 注册新商品并上传图片相关数据 |
| `/products/get-list`           | GET  | 请求现有的商品列表           |
| `/products/findById/{id}`      | GET  | 根据商品ID寻找商品           |
| `/blocks/add`                  | POST | 为商品添加区块记录           |
| `/blocks/findByProductId/{id}` | GET  | 查询某商品的区块链           |

------

#### 3.2.3 主要组件设计

##### ① `AddProduct.vue` —— 商品信息登记页

- 提供商品名称、图片选择、提交按钮；

- 使用 `<input type="file">` 接收用户上传的茅台酒照片；

- 通过 `FormData` 向后端 `/products/upload` 提交多媒体数据；

- 上传成功后弹出提示，表单清空。

  ![](D:\various\2025\InfoSecurity\hw5\images\1.png)

##### ② `ProductList.vue` —— 商品总览页

- 加载并展示所有商品（调用 `/products/get-list`）；

- 每个商品展示其缩略图、编号、名称、创建时间；

- 点击商品卡片跳转到其溯源详情页（`ProductDetail.vue`）。

  ![](D:\various\2025\InfoSecurity\hw5\images\2.png)

##### ③ `ProductDetail.vue` —— 区块链详情页

- 根据 URL 中的 `productId` 查询该商品对应的区块链（调用 `/blocks/findByProductId/{id}`）；

- 展示每个区块的信息：状态、时间、哈希值、签名、前一个哈希;

- **调用`/blocks/findByProductId/{id}`时会使用公钥对`signature`进行验证，只有验证成功在会显示在前端**

  ![](D:\various\2025\InfoSecurity\hw5\images\3.png)

- 点击`Previous Hash`，`Current Hash`，`Signature`字样会翻转为当前区块的真实信息，**每一个区块的`Previous Hash`都是前一个区块的`Current Hash`**，以此互相连接成链。

  ![](D:\various\2025\InfoSecurity\hw5\images\4.png)

##### ④ `AddBlock.vue` —— 商品状态记录添加页

- 输入状态信息（如运输、入库）、备注信息；

- 调用后端 `/blocks/add` 接口添加新区块；

- 可实现茅台酒流通过程的各个阶段记录。

  ![](D:\various\2025\InfoSecurity\hw5\images\5.png)

------

## 4 程序界面说明

1. **首页导航**
   - 添加产品
   - 产品列表

2. **添加产品页面**
   - 表单项：
     - 商品名称输入框
     - 图片上传选择框
     - 提交按钮
   - 提交成功后弹窗提示，重置表单。
3. **商品列表页**
   - 展示所有已登记商品及图片缩略图；
   - 点击某商品，跳转查看其详细溯源记录（以区块列表呈现）。
   - 还有一个返回按钮，点击会跳转至上一个页面。
4. **商品详情页**
   - 包含当前商品的所有可溯源区块。
   - 每个区块会显示区块的商品图片，商品状态，具体信息，添加时间，以及**Previous Hash，Current Hash，Signature**。
   - 点击**Previous Hash，Current Hash，Signature**字样可以将其翻转为对应的详细内容。

5. **添加区块页面**
   - 表单项：
     - 当前状态
     - 区块数据（可以为备注，位置等信息）
   - 提交后自动返回详情页，并在后端生成新区块。
   - 还有一个返回按钮，点击会跳转至上一个页面。

## 5 安全性与数据防篡改机制

本系统围绕“**不可伪造、不可篡改、可验证**”的目标，构建了多个安全防护层次，确保商品区块链数据的真实可信、用户行为可控、后端数据不可篡改。

### 5.1 区块链数据结构安全

- 每个商品形成一条独立区块链，每个区块均保存 `previousHash` 与 `currentHash` 字段；
- 通过 SHA-256 哈希算法对关键字段计算得到 `currentHash`，并嵌入下一区块；
- 一旦某个区块数据被篡改，其 hash 将失效，链条结构断裂，整体链条验证失败。

### 5.2 PoW共识机制（Proof of Work）

- 每次生成新区块都要求计算一个满足“以 n 个 0 开头”的哈希值；
- 引入 nonce 字段作为随机因子，增加哈希碰撞计算难度；
- 恶意篡改任何一个区块都将导致必须重新计算所有后续区块的哈希值，成本极高。

### 5.3 RSA 数字签名机制

- 每个区块生成后，对其 `currentHash` 使用私钥签名，生成 `transactionSignature`；
- 签名仅服务端可生成，保障数据来源真实可信；
- 客户端或其他用户可通过公钥验签，确保数据未被伪造或篡改。

### 5.4 前端验签机制

- 在用户访问商品区块链详情页时，系统自动使用公钥对每个区块进行签名验证；
- 验签失败的区块将不予展示，提示“数据可能被非法篡改”；
- 强化用户数据信任保障，提升系统的可审计性和透明度。

### 5.5 私钥存储与访问控制

- 私钥 `private.pem` 存放于后端服务资源目录，未对外暴露；
- 所有签名操作均在服务端完成，前端和第三方均无法访问；

## 6 实验心得

通过本次“基于区块链的茅台酒商品溯源系统”设计与实现，我深入理解了区块链技术的核心思想与工程落地方式，也强化了自己在前后端开发、加密安全机制以及系统架构设计方面的能力。通过这次实验，我理解了区块链的基本结构与数据特性：

- 区块链不仅仅是比特币，更是一种不可篡改、可追溯的结构；
- 每一个区块通过 `previousHash` 与上一区块连接，形成链式结构；
- “共识机制”虽然在本项目中使用简化的 PoW，但已初步理解其价值和难点。

并且通过实际的实践，实现了加密算法与数字签名机制：

- 使用 SHA-256 保证数据不可逆；
- 结合 RSA 签名/验签机制，确保数据的不可伪造；
- 实现前端自动验签功能，增强系统可信性与透明性。



































# 项目源代码

## 1 后端代码

```
backend/BlockChain
├── src/main/java/com/example/blockchain/
│   ├── config/                       # 跨域配置类
│   │   └── WebConfig.java
│   ├── controller/                   # 接收请求，返回数据
│   │   ├── BlockController.java
│   │   └── ProductController.java
│   ├── entity/                       # 与数据库表对应的实体类
│   │   ├── Block.java
│   │   └── Product.java
│   ├── dto/                          # 请求与响应数据结构
│   │   ├── BlockDTO.java
│   │   └── ProductDTO.java
│   ├── repository/                   # 数据库操作接口
│   │   ├── BlockRepository.java
│   │   └── ProductRepository.java
│   ├── service/                      # 核心业务逻辑层
│   │   ├── BlockService.java
│   │   └── ProductService.java
│   ├── util/                         # 工具类：加密、哈希、文件上传处理
│   │   ├── FileUploadUtil.java
│   │   ├── HashUtil.java
│   │   └── SignatureUtil.java
│   └── BlockChainApplication.java    # 应用主入口
├── uploads/images/                   # 上传商品图片存储目录
├── src/main/resources/
│   ├── application.properties        # 应用配置
│   ├── private.pem                   # RSA 私钥文件（用于签名）
│   └── public.pem                    # RSA 公钥文件（用于验签）
└── pom.xml                           # Maven 依赖配置
```

#### 1.1 `config/`

**WebConfig.java: **

```java
package com.example.blockchain.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")  // 允许访问的路径
                .allowedOrigins("http://localhost:5173")  // 允许来自 http://localhost:5173 的请求
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }

    // 配置静态资源路径映射，用于图片在前端显示
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/images/**")  // 对应访问路径
                .addResourceLocations("file:" + System.getProperty("user.dir") + "/uploads/images/");
    }
}
```

#### 1.2 `controller`

**BlockController**:

```java
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
```

**ProductController**:

```java
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
```

#### 1.3 `entity/`

**Block.java**

```java
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
```

**Product.java**

```java
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
```

#### 1.4 `dto/`

**BlockDTO.java**

```java
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
```

**ProductDTO.java**

```java
package com.example.blockchain.dto;
import com.example.blockchain.entity.Product;
import lombok.Data;

import java.util.Date;

@Data
public class ProductDTO {

    private Integer productId;  // 商品ID
    private String productName;  // 商品名称
    private Date creationDate;  // 生产日期
    private String imagePath;  // 图片路径

    public ProductDTO() {
        // 默认构造函数
    }

    // 使用Product实体创建ProductDTO
    public ProductDTO(Integer productId, String productName, Date creationDate, String imagePath) {
        this.productId = productId;
        this.productName = productName;
        this.creationDate = creationDate;
        this.imagePath = imagePath;
    }

    // 从Product实体创建ProductDTO
    public ProductDTO(Product product) {
        this.productId = product.getProductId();
        this.productName = product.getProductName();
        this.creationDate = product.getCreationDate();
        this.imagePath = product.getImagePath();
    }
}
```

#### 1.5 `repository/`

**BlockRepository.java**

```java
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
```

**ProductRepository.java**

```java
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
```

#### 1.6 `service/`

**BlockService.java**

```java
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
```

**ProductService.java**

```java
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
```

#### 1.7 `util/`

**FileUploadUtil.java**

```java
package com.example.blockchain.util;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileUploadUtil {

    private static final String PRODUCT_IMAGE_UPLOAD_DIR = "uploads/images/";

    public static String saveProductImage(MultipartFile file, long productId) throws IOException {
        return saveFile(file, productId, PRODUCT_IMAGE_UPLOAD_DIR);
    }

    public static String saveFile(MultipartFile file, long productId, String directory) throws IOException {
        // 获取项目根目录
        String projectDir = System.getProperty("user.dir");
        Path uploadPath = Paths.get(projectDir, directory);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);  // 如果目录不存在，则创建目录
        }

        // 使用产品ID生成文件名
        String fileName = productId + ".jpg";  // 将文件命名为产品ID.jpg

        Path filePath = uploadPath.resolve(fileName);
        file.transferTo(filePath.toFile());  // 保存文件到指定路径

        // 返回图片的相对路径
        return "/" + directory + "/" + fileName;
    }
}
```

**HashUtil.java**

```java
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
```

**SignatureUtil.java**

```java
package com.example.blockchain.util;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.*;
import java.util.Base64;
import java.io.*;

public class SignatureUtil {

    public static PrivateKey loadPrivateKey(String filename) throws Exception {
        String keyPem = new String(Files.readAllBytes(Paths.get(filename)))
                .replaceAll("-----BEGIN PRIVATE KEY-----", "")
                .replaceAll("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");  // 去掉换行和空格

        byte[] keyBytes = Base64.getDecoder().decode(keyPem);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(keySpec);
    }


    public static PublicKey loadPublicKey(String filename) throws Exception {
        String keyPem = new String(Files.readAllBytes(Paths.get(filename)))
                .replaceAll("-----BEGIN PUBLIC KEY-----", "")
                .replaceAll("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s+", "");  // 去掉换行和空格

        byte[] keyBytes = Base64.getDecoder().decode(keyPem);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(keySpec);
    }


    public static String sign(String data, PrivateKey privateKey) throws Exception {
        Signature rsa = Signature.getInstance("SHA256withRSA");
        rsa.initSign(privateKey);
        rsa.update(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(rsa.sign());
    }

    public static boolean verify(String data, String signature, PublicKey publicKey) throws Exception {
        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initVerify(publicKey);
        sig.update(data.getBytes(StandardCharsets.UTF_8));
        return sig.verify(Base64.getDecoder().decode(signature));
    }
}
```

## 2 前端代码

```
frontend/src/
├── views/
│   ├── AddBlock.vue            # 添加区块信息
│   ├── AddProduct.vue          # 添加新商品
│   ├── ProductList.vue         # 展示所有商品信息
│   └── ProductDetail.vue       # 展示某商品区块链详情
├── router/
│   └── index.js                # 路由配置
├── App.vue                     
└── main.js                    
```

#### 2.1  `views/`

**AddBlock.vue**        

```vue
<template>
  <div class="add-block">
    <h2>添加新Block</h2>
    <button @click="goBack" class="back-button">返回</button>
    <form @submit.prevent="handleSubmit">

      <div class="form-group">
        <label for="productStatus">产品状态</label>
        <select v-model="newBlock.productStatus" id="productStatus" required>
          <option value="生产中">生产中</option>
          <option value="运输中">运输中</option>
          <option value="待售">待售</option>
          <option value="已售">已售</option>
        </select>
      </div>

      <div class="form-group">
        <label for="data">区块数据</label>
        <textarea v-model="newBlock.data" id="data" required></textarea>
      </div>

      <div class="form-group">
        <button type="submit" class="submit-button">提交</button>
      </div>
    </form>
    <div v-if="message" class="message">{{ message }}</div>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  props: ['id'],
  data() {
    return {
      newBlock: {
        productId: this.id,
        productStatus: '待售',
        data: '',
      },
      message:'',
    };
  },
  methods: {
    async handleSubmit() {
      try {
        console.log(this.newBlock);  // 使用 this 来访问 newBlock
        const response = await axios.post('http://localhost:8080/api/blocks/add', this.newBlock);
        if (response.status === 200) {
          this.$router.push({ name: 'product-detail', params: { id: this.newBlock.productId } });
          console.log('Block added successfully');
          this.message = '区块添加成功！';
        }
      } catch (error) {
        console.error('Failed to add block', error);
        this.message = '添加区块失败，请稍后再试。';
      }
    },
    goBack() {
      this.$router.go(-1);
    },
  },

};
</script>

<style scoped>
.add-block {
  padding: 30px;
  background-color: #f8f9fa;
  border-radius: 15px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
}

h2 {
  text-align: center;
  font-size: 28px;
  font-weight: bold;
  margin-bottom: 20px;
  color: #333;
}

.form-group {
  margin-bottom: 15px;
}

label {
  font-size: 16px;
  font-weight: bold;
  color: #333;
  display: block;
  margin-bottom: 8px;
}

input,
select,
textarea {
  width: 100%;
  padding: 10px;
  font-size: 16px;
  border: 1px solid #ddd;
  border-radius: 5px;
}

textarea {
  resize: vertical;
}

button.submit-button {
  padding: 10px 20px;
  font-size: 16px;
  background-color: #28a745;
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
}

button.submit-button:hover {
  background-color: #218838;
}
.message {
  text-align: center;
  font-size: 16px;
  font-weight: bold;
}
.back-button {
  padding: 8px 16px;
  font-size: 16px;
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  background-color: #007bff;
}
.back-button:hover {
  background-color: #0056b3;
}
</style>
```

**AddProduct.vue**

```vue
<template>
  <div class="add-product">
    <h2>添加新产品</h2>
    <form @submit.prevent="submitForm" class="form-container">
      <div class="form-group">
        <label for="productName">产品名称</label>
        <input 
          type="text" 
          id="productName" 
          v-model="product.productName" 
          required 
          placeholder="输入产品名称"
          class="input-field"
        />
      </div>

      <div class="form-group">
        <label for="image">选择图片</label>
        <input 
          type="file" 
          id="image" 
          ref="image" 
          required 
          class="input-field"
        />
      </div>

      <div class="form-group">
        <button type="submit" class="submit-btn">提交</button>
      </div>
    </form>

    <!-- 显示提交结果 -->
    <div v-if="message" :class="{'success': success, 'error': !success}" class="message">
      {{ message }}
    </div>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  data() {
    return {
      product: {
        productName: '',  // 产品名称
        imagePath: ''  // 存储上传的图片路径
      },
      message: '',
      success: false
    };
  },
  methods: {
    // 提交表单
    async submitForm() {
      // 校验商品名称是否为空
      if (!this.product.productName) {
        alert("请填写商品名称");
        return;
      }

      // 创建 FormData
      const formData = new FormData();
      formData.append('productName', this.product.productName);  // 将产品名称传给后端

      // 获取文件并加入 FormData
      const file = this.$refs.image.files[0];
      if (file) {
        formData.append("file", file);  // 将文件添加到 FormData 中
      } else {
        alert("请上传商品图片");
        return;
      }

      try {
        // 发送请求到后端上传商品信息和图片
        const response = await axios.post('http://localhost:8080/api/products/upload', formData, {
          headers: {
            'Content-Type': 'multipart/form-data'
          }
        });

        if (response.status === 200 || response.status === 201) {
          this.success = true;
          this.message = '产品添加成功！';

          // 获取返回的图片路径
          this.product.imagePath = response.data.imagePath;

          // 重置表单
          this.product.productName = '';
          this.product.imagePath = '';
        } else {
          this.success = false;
          this.message = '上传失败，请稍后再试';
        }
      } catch (error) {
        console.error("上传商品时出错", error);
        this.success = false;
        this.message = '发生错误，请重试';
      }
    }
  }
};
</script>

<style scoped>
.add-product {
  width: 100%;
  max-width: 500px;
  margin: 0 auto;
  padding: 30px;
  background: linear-gradient(135deg, #f7f7f7, #ffffff);
  border-radius: 15px;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

h2 {
  text-align: center;
  font-size: 24px;
  font-weight: bold;
  color: #333;
  margin-bottom: 20px;
}

.form-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
}

.input-field {
  padding: 12px 15px;
  font-size: 16px;
  border: 1px solid #ccc;
  border-radius: 8px;
  outline: none;
  transition: border-color 0.3s ease, box-shadow 0.3s ease;
}

.input-field:focus {
  border-color: #007bff;
  box-shadow: 0 0 8px rgba(0, 123, 255, 0.4);
}

.submit-btn {
  padding: 12px;
  font-size: 18px;
  background-color: #28a745;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: background-color 0.3s ease, transform 0.2s ease;
}

.submit-btn:hover {
  background-color: #218838;
}

.submit-btn:active {
  transform: scale(0.98);
}

.message {
  text-align: center;
  font-size: 16px;
  font-weight: bold;
}

.success {
  color: green;
}

.error {
  color: red;
}
</style>
```

**ProductList.vue**      

```vue
<template>
  <div class="product-list">
    <h2>商品列表</h2>
    <ul v-if="products.length > 0">
      <li v-for="(product, index) in reversedProducts" :key="product.productId" class="product-item" @click="goToProductDetail(product.productId)">
        
          <!-- 显示连续编号 -->
          <div class="product-number">{{ products.length - index }}</div>
          <div class="product-details">
            <p class="product-name">{{ product.productName }}</p>
            <img :src="baseURL + product.imagePath" alt="Product Image" class="product-image"/>
            <p class="product-date">{{ "创建时间: "+formatCreationDate(product.creationDate) }}</p>
          </div>
      </li>
    </ul>
    <p v-else>没有商品</p>
  </div>
</template>


<script>
import axios from 'axios';

export default {
  data() {
    return {
      baseURL: "http://localhost:8080",
      products: [],  // 商品列表
    };
  },
  mounted() {
    this.fetchProducts();  // 在组件加载时获取商品列表
  },
  computed: {
    reversedProducts() {
      return [...this.products].reverse();
    }
  },
  methods: {
    async fetchProducts() {
      try {
        const response = await axios.get('http://localhost:8080/api/products/get-list');
        if (response.status === 200) {
          this.products = response.data;  // 更新商品列表
        }
      } catch (error) {
        console.error("获取商品列表失败", error);
      }
    },
    
    // 格式化创建日期为北京时间，精确到分钟
    formatCreationDate(dateString) {
      const date = new Date(dateString);  // 将字符串转换为 Date 对象
      // 设置时区为北京时间 (UTC +8)
      const options = {
        timeZone: 'Asia/Shanghai',
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        hour12: false,  // 24小时制
      };
      // 格式化日期
      const formattedDate = new Intl.DateTimeFormat('zh-CN', options).format(date);
      return formattedDate;  // 返回格式化后的时间
    },
    goToProductDetail(productId) {
      console.log("Navigating to product detail with ID:", productId);
      this.$router.push({ name: 'product-detail', params: { id: productId } });
    }
  }
};
</script>


<style scoped>
.product-list {
  width: 100%;
  max-width: 800px;
  margin: 50px auto;
  padding: 30px;
  background-image: url('../assets/images/background.jpg');
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  border-radius: 15px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  position: relative;
  backdrop-filter: blur(5px);
  -webkit-backdrop-filter: blur(5px);
}

h2 {
  text-align: center;
  font-size: 28px;
  font-weight: bold;
  color:aliceblue;
  margin-bottom: 30px;
}

.product-item {
  display: flex;
  align-items: center;
  padding: 12px;
  background-color: rgba(255, 255, 255, 0.8); 
  border-radius: 10px;
  margin-bottom: 15px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  height: 80px;  
}

.product-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 20px rgba(0, 0, 0, 0.1);
}

.product-number {
  font-size: 22px;
  font-weight: bold;
  color: #007bff;
  margin-right: 20px;
  margin-left: 20px;
  width: 40px; 
}

.product-details {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: flex-start;
  width: 100%;
}

.product-name {
  font-size: 16px;
  font-weight: bold;
  color: #333;
  margin-right: 20px;
  width: 200px;  
  text-overflow: ellipsis;
  white-space: nowrap;
  overflow: hidden;
}

.product-image {
  width: 100px;
  height: 100px;
  object-fit: cover;
  border-radius: 10px;
  margin-right: 20px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
}

.product-date {
  font-size: 14px;
  color: #888;
}

p {
  text-align: center;
  color: #666;
  font-size: 16px;
}
</style>
```

**ProductDetail.vue**  

```vue
<template>
  <div class="product-details">
    <div class="header">
      <h2>{{ product.productName }} - 详情</h2>
      <!-- 返回按钮 -->
      <button @click="goBack" class="back-button">返回</button>
      <!-- 添加新block按钮 -->
      <button @click="goToAddBlock" class="add-block-button">添加新block</button>
    </div>

    <div v-if="blocks.length > 0">
      <div class="block-header">
        <h3>区块列表</h3>
      </div>
      <ul>
        <li v-for="(block, index) in blocks.slice().reverse()" :key="block.blockId" class="block-item">
          <div class="block-number">{{ blocks.length - index }}</div>
          <div class="block-details">
            <img :src="baseURL + product.imagePath" alt="Product Image" class="product-image"/>
            <div class="block-info">
              <p class="block-detail">{{ block.data }}</p>
              <p class="block-detail">{{ block.productStatus }}</p>
              <p class="block-detail">{{ formatCreationDate(block.timestamp) }}</p>
            </div>

            <div class="block-info">
              <p class="block-detail-hash" @click="toggle('previous')">
                {{ flipped.previous ? block.previousHash : 'Previous Hash' }}
              </p>
              <p class="block-detail-hash" @click="toggle('current')">
                {{ flipped.current ? block.currentHash : 'Current Hash' }}
              </p>
              <p class="block-detail-hash" @click="toggle('signature')">
                {{ flipped.signature ? block.transactionSignature : 'Signature' }}
              </p>
            </div>

          </div>
        </li>
      </ul>
    </div>
    <p v-else>该商品没有区块。</p>
  </div>
</template>


<script>
import axios from 'axios';

export default {
  props: ['id'],
  data() {
    return {
      product: {},
      blocks: [],
      baseURL: "http://localhost:8080",  

      flipped: {
        previous: false,
        current: false,
        signature: false
      }
    };
  },
  mounted() {
    this.fetchProductDetails();
  },
  methods: {
    async fetchProductDetails() {
      try {
        console.log("Fetching details for product ID:", this.id);

        const productResponse = await axios.get(`http://localhost:8080/api/products/findById/${this.id}`);
        if (productResponse.status === 200) {
          this.product = productResponse.data;
          console.log("Product details:", this.product);
        }

        const blocksResponse = await axios.get(`http://localhost:8080/api/blocks/findByProductId/${this.id}`);
        if (blocksResponse.status === 200) {
          this.blocks = blocksResponse.data;
          console.log("Blocks for product:", this.blocks);
        }
      } catch (error) {
        console.error("获取商品详情或区块数据失败", error);
      }
    },

    formatCreationDate(dateString) {
      const date = new Date(dateString);
      const options = {
        timeZone: 'Asia/Shanghai',
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        hour12: false,
      };
      const formattedDate = new Intl.DateTimeFormat('zh-CN', options).format(date);
      return formattedDate;
    },

    // 返回上一页
    goBack() {
      this.$router.go(-1);
    },

    // 跳转到添加新block页面
    goToAddBlock() {
      console.log("Navigating to add block with ID:", this.id);
      this.$router.push({ name: 'add-block', params: { id: this.id } });
    },

    // 切换状态
    toggle(key) {
      this.flipped[key] = !this.flipped[key];
      console.log(`Toggled ${key} to`, this.flipped[key]);
    },

  },
};
</script>

<style scoped>
.product-details {
  padding: 30px;
  background-color: #f8f9fa;
  border-radius: 15px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

h2 {
  text-align: center;
  font-size: 28px;
  font-weight: bold;
  margin-bottom: 20px;
  color: #333;
}

.back-button, .add-block-button {
  padding: 8px 16px;
  font-size: 16px;
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
}

.back-button {
  background-color: #007bff;
}

.add-block-button {
  background-color: #28a745; 
}

.back-button:hover {
  background-color: #0056b3;
}

.add-block-button:hover {
  background-color: #218838; 
}

h3 {
  font-size: 20px;
  font-weight: bold;
  color: #007bff;
  margin-bottom: 20px;
}

.block-item {
  display: flex;
  align-items: center;
  padding: 12px;
  background-color: rgba(255, 255, 255, 0.9);
  border-radius: 10px;
  margin-bottom: 15px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  height: 120px;
}

.block-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 20px rgba(0, 0, 0, 0.1);
}

.block-number {
  font-size: 22px;
  font-weight: bold;
  color: #007bff;
  margin-right: 20px;
  width: 40px;
}

.block-details {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  width: 100%;
  gap: 16px; 
}

.product-image {
  width: 100px;
  height: 100px;
  object-fit: cover;
  border-radius: 10px;
  margin-right: 20px;
}

.block-info {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  justify-content: space-between;
  width: 270px;
}

.block-detail {
  font-size: 14px;
  color: #333;
  margin: 5px 0;
  font-family: Consolas, 'Microsoft YaHei', '微软雅黑', 'PingFang SC', 'Hiragino Sans GB', 'Heiti SC', 'WenQuanYi Micro Hei', sans-serif;
  width:200px;
  word-break: break-all; 
}

.block-detail-hash {
  font-size: 14px;
  color: #333;
  margin: 5px 0;
  font-family: Consolas, 'Microsoft YaHei', '微软雅黑', 'PingFang SC', 'Hiragino Sans GB', 'Heiti SC', 'WenQuanYi Micro Hei', sans-serif;
  width:580px;
  word-break: break-all; 
  cursor: pointer;
}


.block-detail-hash:hover {
  color: #007bff;
  text-decoration: underline;
}

.block-signature {
  font-size: 14px;
  color: #444;
  text-align: right;
  width: 100px;
  margin-left: 20px;
}

p {
  text-align: center;
  color: #666;
  font-size: 16px;
}
</style>
```

#### 2.2  `router/`

**index.js**

```js
import { createRouter, createWebHistory } from 'vue-router'
import AddProduct from '../views/AddProduct.vue'
import ProductList from '../views/ProductList.vue'
import ProductDetail from '../views/ProductDetail.vue'
import AddBlock from '../views/AddBlock.vue'

const routes = [
    { path: '/', redirect: '/ProductList' },
    { path: '/AddProduct', component: AddProduct },
    { path: '/ProductList', component: ProductList },
    { path: '/ProductDetail/:id', name: 'product-detail', component: ProductDetail, props: true },
    { path: '/AddBlock/:id', name: 'add-block', component: AddBlock, props: true }
]

export default createRouter({
    history: createWebHistory(),
    routes
})
```

#### 2.3  `App.vue`

```vue
<template>
  <div class="app-layout">
    <aside class="sidebar">
      <h2>功能导航</h2>
      <router-link to="/AddProduct">添加产品</router-link>
      <router-link to="/ProductList">产品列表</router-link>
    </aside>

    <main class="main-content">
      <router-view />
    </main>
  </div>
</template>

<style scoped>
.app-layout {
  display: flex;
  height: 100vh;
}

.sidebar {
  width: 200px;
  background: #f5f5f5;
  padding: 20px;
  border-right: 1px solid #ccc;
}

.sidebar a {
  display: block;
  margin-bottom: 10px;
  text-decoration: none;
  color: #333;
}

.sidebar a.router-link-active {
  font-weight: bold;
  color: #42b983;
}

.main-content {
  flex: 1;
  padding: 30px;
  overflow-y: auto;
}
</style>
```

#### 2.4  `main.js`

```js
import { createApp } from 'vue'
import App from './App.vue'
import router from './router'

createApp(App).use(router).mount('#app')
```

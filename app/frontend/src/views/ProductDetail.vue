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
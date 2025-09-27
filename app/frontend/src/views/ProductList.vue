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
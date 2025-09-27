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

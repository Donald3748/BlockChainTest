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

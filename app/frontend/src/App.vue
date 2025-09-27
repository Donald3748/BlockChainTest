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

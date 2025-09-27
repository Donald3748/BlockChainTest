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

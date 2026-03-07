<template>
  <div class="not-found">
    <h1>404</h1>
    <p>页面不存在，或该入口已从医生端 MVP 中移除。</p>
    <router-link to="/workorders">返回升级工单</router-link>
  </div>
</template>

<style scoped>
.not-found {
  min-height: 60vh;
  display: grid;
  place-items: center;
  padding: 40px 20px;
  text-align: center;
}

h1 {
  margin: 0 0 12px;
  font-size: 72px;
  color: #2979ff;
}

p {
  margin: 0 0 20px;
  color: #556c8a;
}

a {
  color: #2979ff;
  text-decoration: none;
}
</style>

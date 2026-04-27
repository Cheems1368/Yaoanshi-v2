<template>
  <div class="not-found">
    <h1>404</h1>
    <p>页面不存在，或该入口已从药师端 MVP 中移除。</p>
    <router-link to="/dashboard">返回工作台</router-link>
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
  color: var(--pro-brand-primary);
}

p {
  margin: 0 0 20px;
  color: var(--pro-text-secondary);
}

a {
  color: var(--pro-brand-primary);
  text-decoration: none;
}
</style>

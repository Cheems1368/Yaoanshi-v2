<template>
  <section class="page-shell">
    <header class="page-header">
      <div>
        <h2>患者管理</h2>
        <p>用于检索负责患者、补充资料并进入后续计划维护。</p>
      </div>
      <el-input placeholder="搜索姓名 / 手机号 / 病案号" clearable class="search-box" />
    </header>

    <div class="patient-grid">
      <article v-for="patient in patients" :key="patient.id" class="patient-card">
        <div class="patient-main">
          <div>
            <h3>{{ patient.name }}</h3>
            <p>{{ patient.meta }}</p>
          </div>
          <el-tag :type="patient.tagType" effect="plain">{{ patient.status }}</el-tag>
        </div>
        <dl class="patient-info">
          <div>
            <dt>当前计划</dt>
            <dd>{{ patient.plan }}</dd>
          </div>
          <div>
            <dt>依从性</dt>
            <dd>{{ patient.adherence }}</dd>
          </div>
          <div>
            <dt>待补充资料</dt>
            <dd>{{ patient.todo }}</dd>
          </div>
        </dl>
      </article>
    </div>
  </section>
</template>

<script setup>
const patients = [
  {
    id: 1,
    name: '张明',
    meta: '男 · 65 岁 · 责任药师已绑定',
    status: '资料待补充',
    tagType: 'warning',
    plan: '高血压维持方案',
    adherence: '7 日 86%',
    todo: '补充过敏史'
  },
  {
    id: 2,
    name: '王丽',
    meta: '女 · 58 岁 · 家属协助照护',
    status: '正常随访',
    tagType: 'success',
    plan: '糖尿病联合用药',
    adherence: '30 日 94%',
    todo: '无'
  },
  {
    id: 3,
    name: '李华',
    meta: '男 · 72 岁 · 本周新增处方',
    status: '等待建计划',
    tagType: 'info',
    plan: '待确认',
    adherence: '暂无',
    todo: '核对上传处方'
  }
]
</script>

<style lang="scss" scoped>
.page-shell {
  display: grid;
  gap: 24px;
}

.page-header {
  display: flex;
  align-items: end;
  justify-content: space-between;
  gap: 16px;
}

.page-header h2,
.patient-main h3 {
  margin: 0;
  color: var(--pro-text-primary);
}

.page-header p,
.patient-main p,
.patient-info dt {
  margin: 0;
  color: var(--pro-text-secondary);
}

.search-box {
  max-width: 320px;
}

.patient-grid {
  display: grid;
  gap: 16px;
}

.patient-card {
  padding: 20px;
  border: 1px solid var(--pro-border-color);
  border-radius: var(--pro-radius-lg);
  background: var(--pro-bg-primary);
  box-shadow: var(--pro-shadow-sm);
}

.patient-main {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
}

.patient-info {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
  margin: 0;
}

.patient-info dd {
  margin: 6px 0 0;
  color: var(--pro-text-primary);
}

@media (max-width: 768px) {
  .page-header,
  .patient-main,
  .patient-info {
    display: grid;
    grid-template-columns: 1fr;
  }

  .search-box {
    max-width: none;
  }
}
</style>

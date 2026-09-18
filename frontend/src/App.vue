<template>
  <div id="app">
    <router-view />
  </div>
</template>

<script setup>
// App.vue根组件
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: "Helvetica Neue", Helvetica, "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", "微软雅黑", Arial, sans-serif;
  background-color: var(--sm-bg-app, #ecfdf5);
  color: var(--el-text-color-primary);
}

#app {
  height: 100vh;
  width: 100vw;
}

/* 业务页统一页头：仅一行页面标题（模块名在顶栏 Home 已显示，此处不再重复） */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  flex-wrap: wrap;
  gap: 12px 16px;
  margin-bottom: 20px;
  padding-bottom: 4px;
}
.page-subtitle {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  line-height: 1.4;
  color: var(--el-text-color-primary);
  display: inline-flex;
  align-items: center;
  gap: 10px;
}
.page-subtitle > .el-icon {
  font-size: 20px;
  color: var(--el-color-primary);
}
.page-header-actions {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

/* ========== 下拉选择（el-select）：选中后完整展示内容 ========== */
/* 块级表单 / 弹窗表单：下拉占满表单项内容区 */
.el-form:not(.el-form--inline) .el-form-item .el-select {
  width: 100%;
  --el-select-width: 100%;
}

/* 行内筛选栏：保证最小宽度，避免长选项选中后被挤成省略号 */
.el-form--inline .el-form-item .el-select {
  min-width: 240px;
  max-width: min(100%, 520px);
  vertical-align: middle;
}

/* 单选：已选文案允许换行，避免单行省略 */
#app .el-select:not(.el-select--multiple) .el-select__tags-text {
  white-space: normal;
  word-break: break-word;
  overflow: visible;
  text-overflow: clip;
  line-height: 1.45;
  max-height: 4.5em;
}

/* 下拉面板内选项：长名称换行展示 */
.el-select-dropdown__item {
  white-space: normal;
  height: auto;
  min-height: 34px;
  line-height: 1.45;
  padding-top: 8px;
  padding-bottom: 8px;
}

/* ========== 全局表格：表头、单元格、合计行文字居中 ========== */
#app .el-table th.el-table__cell,
#app .el-table td.el-table__cell {
  text-align: center !important;
}
#app .el-table th.el-table__cell > .cell,
#app .el-table td.el-table__cell > .cell {
  text-align: center !important;
  justify-content: center;
}
#app .el-table .el-table__footer-wrapper td.el-table__cell > .cell {
  text-align: center !important;
}
/* 操作列内按钮组仍保持可读间距 */
#app .el-table td.el-table__cell .cell .el-button + .el-button {
  margin-left: 6px;
}
</style>
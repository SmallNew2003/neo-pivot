<script setup lang="ts">
/**
 * 文档页面（MVP 占位）。
 *
 * @remarks
 * - 将用于 S3 presigned PUT 直传 + 文档状态列表
 * - 具体 API 契约见：`openspec/specs/0002-frontend-admin-console-spec.md`
 */
import { ref } from "vue";

const note = ref(
  "文档页将实现：presigned 获取 -> PUT 直传 S3 -> 回调底座创建文档 -> 列表展示索引状态。"
);
</script>

<template>
  <div class="card">
    <h2 style="margin: 0 0 8px">文档</h2>
    <div class="hint">该页面为骨架占位，下一步将按 OpenSpec 补齐上传与列表功能。</div>
    <div style="height: 12px"></div>
    <div class="mono">{{ note }}</div>
  </div>
</template>


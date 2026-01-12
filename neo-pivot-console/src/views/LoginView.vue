<script setup lang="ts">
import { ref } from "vue";
import { login } from "../services/auth";

const username = ref("demo");
const password = ref("demo");
const error = ref<string | null>(null);
const loading = ref(false);

async function submit(): Promise<void> {
  loading.value = true;
  error.value = null;
  try {
    await login({ username: username.value, password: password.value });
    window.location.href = "/documents";
  } catch (e) {
    error.value = e instanceof Error ? e.message : String(e);
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <div class="card">
    <h2 style="margin: 0 0 8px">登录</h2>
    <div class="hint">标准登录：调用底座 <span class="mono">/api/auth/login</span> 获取用户级 JWT。</div>

    <div style="height: 14px"></div>

    <div class="row">
      <div class="field">
        <div class="label">用户名</div>
        <input v-model="username" placeholder="username" autocomplete="username" />
      </div>
      <div class="field">
        <div class="label">密码</div>
        <input v-model="password" type="password" placeholder="password" autocomplete="current-password" />
      </div>
      <div style="align-self: flex-end">
        <button class="btn primary" :disabled="loading" @click="submit">
          {{ loading ? "登录中..." : "登录" }}
        </button>
      </div>
    </div>

    <div v-if="error" class="error" style="margin-top: 12px">{{ error }}</div>
  </div>
</template>


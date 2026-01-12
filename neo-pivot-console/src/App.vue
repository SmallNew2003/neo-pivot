<script setup lang="ts">
import { computed } from "vue";
import { useRoute } from "vue-router";
import { clearAccessToken, getAccessToken } from "./services/auth";

const route = useRoute();
const isAuthed = computed(() => Boolean(getAccessToken()));

function logout(): void {
  clearAccessToken();
  window.location.href = "/login";
}
</script>

<template>
  <div class="layout">
    <header class="topbar">
      <div class="brand">neo-pivot console</div>
      <nav class="nav">
        <a class="link" href="/documents" :data-active="route.path === '/documents'">文档</a>
        <a class="link" href="/chat" :data-active="route.path === '/chat'">Chat</a>
      </nav>
      <div class="spacer"></div>
      <button v-if="isAuthed" class="btn ghost" @click="logout">退出</button>
    </header>
    <main class="content">
      <router-view />
    </main>
  </div>
</template>


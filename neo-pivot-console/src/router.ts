import { createRouter, createWebHistory } from "vue-router";
import LoginView from "./views/LoginView.vue";
import DocumentsView from "./views/DocumentsView.vue";
import ChatView from "./views/ChatView.vue";
import { getAccessToken } from "./services/auth";

/**
 * 路由定义。
 *
 * @remarks
 * - MVP 仅提供最小页面集合：登录 / 文档 / Chat 测试
 */
export const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: "/", redirect: "/documents" },
    { path: "/login", component: LoginView },
    { path: "/documents", component: DocumentsView, meta: { requiresAuth: true } },
    { path: "/chat", component: ChatView, meta: { requiresAuth: true } },
  ],
});

router.beforeEach((to) => {
  if (to.meta.requiresAuth && !getAccessToken()) {
    return "/login";
  }
  return true;
});


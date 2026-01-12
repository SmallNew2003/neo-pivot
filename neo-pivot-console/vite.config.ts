import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";

/**
 * Vite 配置（管理台）。
 *
 * @remarks
 * - API Base URL 通过环境变量 `VITE_API_BASE_URL` 配置
 */
export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
  },
});


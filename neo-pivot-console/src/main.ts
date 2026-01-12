import { createApp } from "vue";
import App from "./App.vue";
import { router } from "./router";
import "./styles.css";

/**
 * 管理台入口。
 *
 * @remarks
 * - 该前端用于验证底座能力（登录、上传、索引状态、问答 citations）
 */
createApp(App).use(router).mount("#app");


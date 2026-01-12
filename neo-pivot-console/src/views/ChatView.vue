<script setup lang="ts">
import { ref } from "vue";
import { apiFetch } from "../services/http";
import { getAccessToken } from "../services/auth";

type Citation = {
  documentId: string;
  chunkId: string;
  chunkIndex: number;
  contentSnippet: string;
};

type ChatResponse = {
  answer: string;
  citations: Citation[];
};

const question = ref("");
const answer = ref<string | null>(null);
const error = ref<string | null>(null);
const loading = ref(false);
const citations = ref<Citation[]>([]);

async function ask(): Promise<void> {
  loading.value = true;
  error.value = null;
  answer.value = null;
  citations.value = [];
  try {
    const accessToken = getAccessToken();
    if (!accessToken) {
      throw new Error("未登录");
    }
    const resp = await apiFetch<ChatResponse>("/api/chat", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ question: question.value, topK: 5 }),
      accessToken,
    });
    answer.value = resp.answer;
    citations.value = resp.citations ?? [];
  } catch (e) {
    error.value = e instanceof Error ? e.message : String(e);
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <div class="card">
    <h2 style="margin: 0 0 8px">Chat 测试</h2>
    <div class="hint">
      模式A：最终答案由底座生成，前端仅调用 <span class="mono">/api/chat</span> 并展示 citations。
    </div>

    <div style="height: 12px"></div>

    <div class="field" style="width: 100%">
      <div class="label">问题</div>
      <textarea v-model="question" placeholder="请输入问题..." />
    </div>

    <div style="height: 10px"></div>

    <div class="row">
      <button class="btn primary" :disabled="loading || !question.trim()" @click="ask">
        {{ loading ? "请求中..." : "提问" }}
      </button>
      <div class="hint">注意：该接口后端骨架阶段可能尚未实现。</div>
    </div>

    <div v-if="error" class="error" style="margin-top: 12px">{{ error }}</div>

    <div v-if="answer" style="margin-top: 16px">
      <div class="label">答案</div>
      <div class="card" style="margin-top: 6px">{{ answer }}</div>
    </div>

    <div v-if="citations.length" style="margin-top: 16px">
      <div class="label">引用（citations）</div>
      <div class="card" style="margin-top: 6px">
        <div v-for="c in citations" :key="c.chunkId" style="padding: 10px 0; border-bottom: 1px solid var(--border)">
          <div class="mono">
            doc={{ c.documentId }} chunk={{ c.chunkIndex }} id={{ c.chunkId }}
          </div>
          <div style="height: 6px"></div>
          <div>{{ c.contentSnippet }}</div>
        </div>
      </div>
    </div>
  </div>
</template>


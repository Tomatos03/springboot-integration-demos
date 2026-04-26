<script setup lang="ts">
import { useSearchStore } from '@/stores'

const store = useSearchStore()

const handleInput = (event: Event) => {
  const value = (event.target as HTMLInputElement).value
  store.keyword = value
  store.handleInput(value)
}

const handleKeyPress = (event: KeyboardEvent) => {
  if (event.key === 'Enter') {
    store.search()
  }
}

const selectSuggestion = (item: any) => {
  store.selectSuggestion(item)
}
</script>

<template>
  <div class="search-container">
    <div class="search-box">
      <h1>Elasticsearch 高亮搜索</h1>

      <div class="input-group">
        <div class="input-wrapper">
          <input
            v-model="store.keyword"
            type="text"
            class="search-input"
            placeholder="输入搜索关键词..."
            @input="handleInput"
            @keypress="handleKeyPress"
            @focus="store.showSuggList"
            @blur="store.hideSuggestions"
          />
          <div v-if="store.showSuggestions" class="suggestions-dropdown">
            <div
              v-for="item in store.suggestions"
              :key="item.text"
              class="suggestion-item"
              @mousedown.prevent="selectSuggestion(item)"
            >
              <span class="suggestion-text">{{ item.text }}</span>
              <span class="suggestion-type">{{ item.type }}</span>
            </div>
          </div>
        </div>
        <button class="search-btn" @click="store.search()" :disabled="store.loading">
          {{ store.loading ? '搜索中...' : '搜索' }}
        </button>
      </div>

      <div v-if="store.error" class="error-message">
        {{ store.error }}
      </div>

      <div v-if="store.total > 0" class="result-info">
        找到 <strong>{{ store.total }}</strong> 个结果
      </div>
    </div>

    <div class="results-container">
      <div v-if="store.loading" class="loading">
        <div class="spinner"></div>
        <p>搜索中...</p>
      </div>

      <div v-else-if="store.results.length === 0 && store.keyword" class="no-results">
        <p>未找到相关结果</p>
      </div>

      <div v-else class="results-list">
        <div v-for="result in store.results" :key="result.id" class="result-item">
          <h3 class="result-title">
            <span v-html="result.highlightedName"></span>
          </h3>

          <div class="result-meta">
            <span v-if="result.brand" class="meta-item">
              <strong>品牌:</strong> {{ result.brand }}
            </span>
            <span v-if="result.category" class="meta-item">
              <strong>分类:</strong> {{ result.category }}
            </span>
            <span v-if="result.price" class="meta-item price">
              <strong>价格:</strong> ¥{{ result.price }}
            </span>
          </div>

          <p class="result-description">
            <span v-html="result.highlightedDescription"></span>
          </p>
        </div>
      </div>
    </div>

    <div v-if="store.totalPages() > 1" class="pagination">
      <button
        v-if="store.currentPage > 1"
        class="page-btn"
        @click="store.goToPage(store.currentPage - 1)"
      >
        上一页
      </button>

      <div class="page-info">第 {{ store.currentPage }} / {{ store.totalPages() }} 页</div>

      <button
        v-if="store.currentPage < store.totalPages()"
        class="page-btn"
        @click="store.goToPage(store.currentPage + 1)"
      >
        下一页
      </button>
    </div>
  </div>
</template>

<style scoped>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

.search-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 40px 20px;
  font-family:
    -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

.search-box {
  max-width: 800px;
  margin: 0 auto 40px;
  background: white;
  border-radius: 12px;
  padding: 40px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

h1 {
  text-align: center;
  color: #333;
  margin-bottom: 30px;
  font-size: 32px;
}

.input-wrapper {
  position: relative;
  flex: 1;
  margin-bottom: 20px;
}

.search-input {
  width: 100%;
  padding: 12px 16px;
  font-size: 16px;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  outline: none;
  transition: border-color 0.3s;
}

.search-input:focus {
  border-color: #667eea;
}

.input-wrapper:has(.search-input:focus) .search-input {
  border-radius: 8px 8px 0 0;
}

.search-btn {
  padding: 12px 32px;
  font-size: 16px;
  font-weight: 600;
  color: white;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition:
    transform 0.2s,
    box-shadow 0.2s;
}

.search-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 10px 20px rgba(102, 126, 234, 0.3);
}

.search-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.suggestions-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background: white;
  border: 1px solid #e0e0e0;
  border-top: none;
  border-radius: 0 0 8px 8px;
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.15);
  max-height: 300px;
  overflow-y: auto;
  z-index: 1000;
}

.suggestion-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.suggestion-item:hover {
  background-color: #f5f5f5;
}

.suggestion-text {
  color: #333;
  font-size: 14px;
}

.suggestion-type {
  color: #999;
  font-size: 12px;
  background: #f0f0f0;
  padding: 2px 8px;
  border-radius: 4px;
}

.error-message {
  color: #d32f2f;
  background: #ffebee;
  padding: 12px;
  border-radius: 8px;
  margin-bottom: 15px;
  border-left: 4px solid #d32f2f;
}

.result-info {
  color: #666;
  text-align: center;
  font-size: 14px;
}

.result-info strong {
  color: #667eea;
  font-size: 16px;
}

.results-container {
  max-width: 800px;
  margin: 0 auto 40px;
}

.loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.1);
}

.spinner {
  width: 50px;
  height: 50px;
  border: 4px solid rgba(102, 126, 234, 0.2);
  border-top-color: #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 20px;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.loading p {
  color: #666;
  font-size: 16px;
}

.no-results {
  background: white;
  border-radius: 12px;
  padding: 60px 20px;
  text-align: center;
  color: #999;
  font-size: 18px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.1);
}

.results-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.result-item {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition:
    box-shadow 0.3s,
    transform 0.3s;
}

.result-item:hover {
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
}

.result-title {
  font-size: 18px;
  color: #333;
  margin-bottom: 12px;
  line-height: 1.5;
}

.result-title :deep(em) {
  background-color: #fff59d;
  color: #d32f2f;
  font-style: normal;
  font-weight: 600;
  padding: 2px 4px;
  border-radius: 2px;
}

.result-meta {
  display: flex;
  gap: 20px;
  margin-bottom: 12px;
  flex-wrap: wrap;
  font-size: 14px;
  color: #666;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
}

.meta-item strong {
  color: #333;
}

.price {
  color: #d32f2f;
  font-weight: 600;
  font-size: 16px;
}

.result-description {
  color: #666;
  font-size: 14px;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.result-description :deep(em) {
  background-color: #fff59d;
  color: #d32f2f;
  font-style: normal;
  font-weight: 600;
  padding: 2px 4px;
  border-radius: 2px;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 15px;
  margin-top: 40px;
}

.page-btn {
  padding: 10px 20px;
  font-size: 14px;
  font-weight: 600;
  color: white;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition:
    transform 0.2s,
    box-shadow 0.2s;
}

.page-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 20px rgba(102, 126, 234, 0.3);
}

.page-info {
  color: white;
  font-weight: 600;
  font-size: 14px;
  min-width: 120px;
  text-align: center;
}

@media (max-width: 768px) {
  .search-container {
    padding: 20px 10px;
  }

  .search-box {
    padding: 20px;
  }

  h1 {
    font-size: 24px;
    margin-bottom: 20px;
  }

  .input-wrapper {
    margin-bottom: 10px;
  }

  .search-btn {
    width: 100%;
  }

  .result-meta {
    flex-direction: column;
    gap: 8px;
  }
}
</style>

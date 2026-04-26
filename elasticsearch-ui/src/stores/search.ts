import { ref } from 'vue'
import { defineStore } from 'pinia'
import { highlightSearch, fetchSuggestions } from '@/api'
import type { ProductView, AutocompleteItem } from '@/types/api'

export const useSearchStore = defineStore('search', () => {
  const keyword = ref('')
  const results = ref<ProductView[]>([])
  const loading = ref(false)
  const error = ref('')
  const total = ref(0)
  const currentPage = ref(1)
  const pageSize = ref(10)

  const suggestions = ref<AutocompleteItem[]>([])
  const showSuggestions = ref(false)
  const suggestionsLoading = ref(false)
  let debounceTimer: ReturnType<typeof setTimeout> | null = null

  const totalPages = () => Math.ceil(total.value / pageSize.value)

  const fetchSuggestData = async (value: string) => {
    if (!value.trim()) {
      suggestions.value = []
      showSuggestions.value = false
      return
    }

    suggestionsLoading.value = true
    try {
      const response = await fetchSuggestions(value, 10)
      suggestions.value = response.items
      showSuggestions.value = suggestions.value.length > 0
    } catch {
      suggestions.value = []
    } finally {
      suggestionsLoading.value = false
    }
  }

  const handleInput = (value: string) => {
    if (debounceTimer) {
      clearTimeout(debounceTimer)
    }

    debounceTimer = setTimeout(() => {
      fetchSuggestData(value)
    }, 300)
  }

  const selectSuggestion = (item: AutocompleteItem) => {
    keyword.value = item.text
    showSuggestions.value = false
    search()
  }

  const hideSuggestions = () => {
    setTimeout(() => {
      showSuggestions.value = false
    }, 200)
  }

  const showSuggList = () => {
    if (suggestions.value.length > 0) {
      showSuggestions.value = true
    }
  }

  const search = async (page: number = 1) => {
    if (!keyword.value.trim()) {
      error.value = '请输入搜索关键词'
      return
    }

    loading.value = true
    error.value = ''
    results.value = []
    showSuggestions.value = false

    try {
      const response = await highlightSearch(keyword.value, page, pageSize.value)
      results.value = response.list
      total.value = response.total
      currentPage.value = page
    } catch (err) {
      error.value = err instanceof Error ? err.message : '搜索出错，请稍后重试'
    } finally {
      loading.value = false
    }
  }

  const goToPage = (page: number) => {
    search(page)
  }

  return {
    keyword,
    results,
    loading,
    error,
    total,
    currentPage,
    pageSize,
    totalPages,
    suggestions,
    showSuggestions,
    suggestionsLoading,
    handleInput,
    selectSuggestion,
    hideSuggestions,
    showSuggList,
    search,
    goToPage,
  }
})

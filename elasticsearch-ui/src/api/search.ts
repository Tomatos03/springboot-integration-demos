import http from '@/utils/request'
import type { ProductView, HighlightSearchResponse, HighlightSearchRequest } from '@/types/api'

export async function highlightSearch(
  keyword: string,
  page: number = 1,
  size: number = 10,
): Promise<HighlightSearchResponse> {
  const request: HighlightSearchRequest = {
    keyword,
    fields: ['name', 'description'],
    preTags: '<em>',
    postTags: '</em>',
    size,
  }

  const response = await http.post<HighlightSearchResponse>(
    `/api/es/advanced/highlight-search?page=${page}`,
    request,
  )

  return response
}

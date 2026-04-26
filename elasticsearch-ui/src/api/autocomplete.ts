import http from '@/utils/request'
import type { AutocompleteResponse, AutocompleteRequest } from '@/types/api'

export async function fetchSuggestions(
  keyword: string,
  limit: number = 10,
): Promise<AutocompleteResponse> {
  const request: AutocompleteRequest = { keyword, limit }

  const response = await http.get<AutocompleteResponse>('/api/es/client/autocomplete', request)

  return response
}

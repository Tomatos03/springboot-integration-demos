export interface ProductView {
  id: string
  name: string
  description: string
  price: number
  brand: string
  category: string
  sales?: number
  stock?: number
  tags?: string[]
  highlightedName: string
  highlightedDescription: string
  createTime?: string
}

export interface HighlightSearchResponse {
  total: number
  page: number
  size: number
  list: ProductView[]
}

export interface HighlightSearchRequest {
  keyword: string
  fields?: string[]
  preTags?: string
  postTags?: string
  size?: number
}

export interface AutocompleteItem {
  text: string
  type: string
}

export interface AutocompleteResponse {
  items: AutocompleteItem[]
}

export interface AutocompleteRequest {
  keyword: string
  limit?: number
}

export interface ApiResponse<T> {
  message: string
  code: number
  data: T
}

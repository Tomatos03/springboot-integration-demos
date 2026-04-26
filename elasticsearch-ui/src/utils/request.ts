import axios, {
  type AxiosInstance,
  type InternalAxiosRequestConfig,
  type AxiosResponse,
  AxiosError,
  type AxiosRequestConfig,
} from 'axios'

// 请求成功拦截器
async function onRequestFulfilled(
  config: InternalAxiosRequestConfig,
): Promise<InternalAxiosRequestConfig> {
  return config
}

// 请求失败拦截器
async function onRequestRejected(error: AxiosError): Promise<never> {
  return Promise.reject(error)
}

// 响应成功拦截器：统一处理业务错误
async function onResponseFulfilled(response: AxiosResponse): Promise<AxiosResponse> {
  return response
}

// 响应失败拦截器：统一处理 HTTP 网络错误
async function onResponseRejected(error: AxiosError): Promise<never> {
  return Promise.reject(error)
}

class Http {
  private axios: AxiosInstance

  constructor() {
    this.axios = axios.create({
      baseURL: 'http://localhost:8083',
      timeout: 3000,
      headers: {
        'Content-Type': 'application/json',
      },
    })

    this.axios.interceptors.request.use(onRequestFulfilled, onRequestRejected)
    this.axios.interceptors.response.use(onResponseFulfilled, onResponseRejected)
  }

  async get<T = unknown>(
    url: string,
    params?: Record<string, unknown> | unknown,
    config?: AxiosRequestConfig,
  ): Promise<T> {
    const res = await this.axios.get<ResponseData<T>>(url, { params, ...config })
    return this.handleResponse(res.data)
  }

  async post<T = unknown>(
    url: string,
    data?: Record<string, unknown> | unknown,
    config?: AxiosRequestConfig,
  ): Promise<T> {
    const res = await this.axios.post<ResponseData<T>>(url, data, config)
    return this.handleResponse(res.data)
  }

  async put<T = unknown>(
    url: string,
    data?: Record<string, unknown> | unknown,
    config?: AxiosRequestConfig,
  ): Promise<T> {
    const res = await this.axios.put<ResponseData<T>>(url, data, config)
    return this.handleResponse(res.data)
  }

  async delete<T = unknown>(
    url: string,
    params?: Record<string, unknown>,
    config?: AxiosRequestConfig,
  ): Promise<T> {
    const res = await this.axios.delete<ResponseData<T>>(url, { params, ...config })
    return this.handleResponse(res.data)
  }

  /**
   * 统一处理业务响应
   * - code === 0 / 200：业务成功，直接返回 data
   * - code !== 0 / 200：业务失败，抛出业务异常
   * - 网络/HTTP 异常由 axios 的 onResponseRejected 处理
   */
  private handleResponse<T>(response: ResponseData<T>): T {
    if (response.code === 0 || response.code === 200) {
      return response.data as T
    }
    throw new BusinessError(response.message || '请求失败', response.code)
  }
}

interface ResponseData<T> {
  message: string
  code: number
  data: T
}

/**
 * 业务异常类
 * - message: 用户友好的错误信息
 * - code: 业务错误码
 */
export class BusinessError extends Error {
  public code: number

  constructor(message: string, code: number) {
    super(message)
    this.name = 'BusinessError'
    this.code = code
  }
}

const http = new Http()

export default http

import createClient from 'openapi-fetch'
import type { paths } from '@/types/api'

export const api = createClient<paths>({
  baseUrl: import.meta.env.VITE_API_BASE_URL, // 前回の .env の値
})

export const getToken = () => sessionStorage.getItem('token')

export const setToken = (t: string) => sessionStorage.setItem('token', t)

export const clearToken = () => sessionStorage.removeItem('token')

api.use({
  onRequest({ request }) {
    const t = getToken()
    if (t) request.headers.set('Authorization', `Bearer ${t}`)
  return request
  },
  onResponse({ response }) {
    if (response.status === 401 && !response.url.endsWith('/auth/login')) {
      clearToken()
      window.location.href = '/'
    }
  },
})
import createClient from 'openapi-fetch'
import type { paths } from '@/types/api'

export const api = createClient<paths>({
  baseUrl: import.meta.env.VITE_API_BASE_URL, // 前回の .env の値
})
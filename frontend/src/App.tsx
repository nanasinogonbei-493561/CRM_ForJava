import './App.css'
import Login from "./Login.tsx"
import { api } from '@/lib/apiClient';

const { data, error } = await api.GET('/users/{user_id}', {
  params: { path: { user_id: 1 } },
})
// data は User 型として推論される

export default function App () {

}

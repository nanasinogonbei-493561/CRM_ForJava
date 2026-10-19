import { Routes, Route, useLocation } from 'react-router-dom'
import { Header } from './components/Header'
import Login from './views/Signin/Login'
import Top from './views/Top'
import UserForm from './views/register/UserForm'

export default function App() {
  const { pathname } = useLocation()

  return (
    <>
      {/* ユーザー登録画面ではヘッダー自体を出さない(戻る導線はフォームのキャンセルボタン) */}
      {pathname !== '/users/new' && (
        // ログイン画面ではログインボタンを出さない
        <Header showAuth={pathname !== '/login'} />
      )}
      <Routes>
        <Route path="/" element={<Top />} />
        <Route path="/login" element={<Login />} />
        <Route path="/users/new" element={<UserForm />} />
      </Routes>
    </>
  )
}

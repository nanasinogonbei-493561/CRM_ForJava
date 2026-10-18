import { Routes, Route } from 'react-router-dom'
import Login from './views/Signin/Login'
import Top from './views/Top'
import UserForm from './views/register/UserForm'

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<Login />} />
      <Route path="/Top" element={<Top />} />
      <Route path="/users/new" element={<UserForm />} />
    </Routes>
  )
}
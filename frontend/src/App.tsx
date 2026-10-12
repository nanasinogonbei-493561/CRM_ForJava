import { Routes, Route } from 'react-router-dom'
import Login from './views/Login'
import Top from './views/Top'

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<Login />} />
      <Route path="/Top" element={<Top />} />
    </Routes>
  )
}
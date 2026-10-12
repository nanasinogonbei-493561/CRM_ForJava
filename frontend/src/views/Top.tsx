//Top.tsx
import { useNavigate } from "react-router-dom";
import { api, clearToken } from '@/lib/apiClient';

export default function Top() {
    const navigate = useNavigate();
    const logout = async () => {
        try {
            await api.POST('/auth/logout')
        } finally {
            clearToken()
            navigate('/')
        }
    }
    return (
        <div className="container">
        <h1>Top Page</h1>
        <button onClick={logout}>ログアウト</button>
        </div>
    );
}
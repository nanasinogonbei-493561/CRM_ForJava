import { Link, useNavigate } from 'react-router-dom'
import { FiSearch } from 'react-icons/fi'
import { Button } from './Button'
import { api, clearToken, getToken } from '@/lib/apiClient'

// ---- データ（ここを書き換えるだけでメニューが変わる） ----
const NAV_ITEMS: string[] = [
  '?',
  '??',
  '???',
  '????',
]

// ---- コンポーネント ----
type HeaderProps = {
  showAuth?: boolean
}

export function Header({ showAuth = true }: HeaderProps) {
  const navigate = useNavigate()
  // App が遷移のたびに再描画されるので、ここで毎回読めばログイン状態に追従する
  const loggedIn = getToken() !== null

  const logout = async () => {
    try {
      await api.POST('/auth/logout')
    } finally {
      // JWT はステートレスなので、サーバーの応答に関係なくクライアント側で必ず破棄する
      clearToken()
      navigate('/login')
    }
  }

  return (
    <header className="relative z-10 bg-white shadow-[0_8px_24px_rgba(120,130,180,0.15)]">
      <div className="mx-auto max-w-site px-5 pt-6 pb-4">
        {/* 上段：会員ボタン（左右同幅の3列でロゴを中央固定） */}
        <div className="grid grid-cols-[auto_1fr] items-center gap-3 md:grid-cols-[1fr_auto_1fr] md:gap-0">
          <Link
            to="/"
            className="font-logo text-[28px] leading-none font-bold tracking-[0.3em] md:-mr-[0.3em] md:text-[46px]"
          >
            SAMPLE
            <span className="font-en text-lg font-normal tracking-normal md:text-[30px]">.jp</span>
          </Link>

          {showAuth ? (
            <div className="flex flex-col items-end gap-1.5 justify-self-end md:gap-3">
              <div className="flex items-center gap-2 md:gap-4">
                {loggedIn ? (
                  <>
                    {/* ユーザー登録は ADMIN のみ(バックエンドが 403 で弾く)。権限の最終判断はサーバー */}
                    <Button as={Link} to="/users/new" variant="fill">
                      ユーザー登録
                    </Button>
                    <span className="hidden text-[22px] font-light text-accent md:inline">/</span>
                    <Button type="button" variant="outline" onClick={logout}>
                      ログアウト
                    </Button>
                  </>
                ) : (
                  <Button as={Link} to="/login" variant="outline">
                    ログイン
                  </Button>
                )}
              </div>
            </div>
          ) : (
            <div className="hidden md:block" /> // 空の右列でロゴの中央を維持
          )}
        </div>

        {/* 下段：グローバルナビ（スマホは横スクロール） */}
        <nav aria-label="グローバルナビゲーション">
          <ul className="mt-7 flex items-center gap-6 overflow-x-auto pb-1.5 whitespace-nowrap md:justify-between md:gap-0 md:overflow-visible md:pb-0">
            {NAV_ITEMS.map((item) => (
              <li key={item}>
                <a
                  href="#"
                  className="relative pb-1 font-en text-[15px] font-bold after:absolute after:bottom-0 after:left-0 after:h-0.5 after:w-full after:origin-left after:scale-x-0 after:bg-accent after:transition-transform after:duration-250 hover:after:scale-x-100"
                >
                  {item}
                </a>
              </li>
            ))}
            <li>
              <a href="#" aria-label="検索" className="text-lg">
                <FiSearch />
              </a>
            </li>
          </ul>
        </nav>
      </div>
    </header>
  )
}

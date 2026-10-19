import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import type { SubmitHandler } from 'react-hook-form'
import { api, setToken } from '@/lib/apiClient'
import { Button } from '../../components/Button'
import { SectionTitle } from '../../components/SectionTitle'

type Inputs = {
  email: string
  password: string
}

const labelClass = "mb-3 block text-[15px] font-bold before:content-['■']"
const inputClass =
  'h-[42px] w-full border border-field px-3 text-base outline-none focus:border-accent focus:ring-[3px] focus:ring-accent/15'
// ↑ text-base = 16px。16px未満だとiPhoneで自動ズームされる

export default function Login() {
  const navigate = useNavigate()
  const [errorMsg, setErrorMsg] = useState('')

  const {
    register,
    handleSubmit,
    resetField,
    formState: { errors },
  } = useForm<Inputs>({ mode: 'onChange' })

  // 入力チェックは「未入力」と「形式」だけにとどめる。
  // パスワードの文字種・桁数はバックエンドの登録時ルールで、ログイン時に縛ると
  // ポリシー変更後に既存ユーザーが入れなくなる。照合はサーバーに任せる。
  const onSubmit: SubmitHandler<Inputs> = async (data) => {
    const { data: res, response } = await api.POST('/auth/login', { body: data })
    if (res) {
      setToken(res.token)
      navigate('/')
    } else if (response.status === 401) {
      // ロック中・存在しない email・パスワード誤りを区別せず同じ文言にする(アカウント列挙対策)
      setErrorMsg('メールアドレスまたはパスワードが正しくありません。もう一度入力してください。')
      resetField('password')
    } else {
      setErrorMsg('エラーが発生しました。')
    }
  }

  return (
    <main>
      <SectionTitle as="h1">ログイン</SectionTitle>

      <form onSubmit={handleSubmit(onSubmit)} noValidate className="mx-auto max-w-[760px] px-5 pb-20">
        <p role="alert" className="mb-6 min-h-[1em] text-center text-sm text-accent">
          {errorMsg}
        </p>

        <div>
          <label htmlFor="email" className={labelClass}>
            メールアドレス
          </label>
          <input
            id="email"
            type="email"
            autoComplete="email"
            aria-invalid={!!errors.email}
            className={inputClass}
            {...register('email', {
              required: 'メールアドレスを入力してください。',
              maxLength: { value: 255, message: '255文字以内で入力してください。' },
              pattern: { value: /^[^\s@]+@[^\s@]+$/, message: 'メールアドレスの形式が不正です。' },
            })}
          />
          <p className="mt-2 min-h-[1em] text-[13px] text-accent">{errors.email?.message}</p>
        </div>

        <div className="mt-8">
          <label htmlFor="password" className={labelClass}>
            パスワード
          </label>
          <input
            id="password"
            type="password"
            autoComplete="current-password"
            aria-invalid={!!errors.password}
            className={inputClass}
            {...register('password', { required: 'パスワードを入力してください。' })}
          />
          <p className="mt-2 min-h-[1em] text-[13px] text-accent">{errors.password?.message}</p>
        </div>

        <div className="mt-14 flex justify-center">
          <Button type="submit" size="lg">
            ログイン
          </Button>
        </div>
      </form>
    </main>
  )
}

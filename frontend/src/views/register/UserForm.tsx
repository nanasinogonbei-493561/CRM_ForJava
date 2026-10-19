import { useForm } from "react-hook-form";
import { useState } from "react"
import { useNavigate } from "react-router-dom"
import type { SubmitHandler } from 'react-hook-form';
import { api } from '@/lib/apiClient'
import { Button } from "../../components/Button";
import { SectionTitle } from "../../components/SectionTitle";

type Role = "SALES" | "SUPPORT" | "ADMIN";
type Inputs = {
    username: string;
    email: string;
    role: Role;
    rawPassword: string;
    confirmPassword: string; 
}

const labelClass = "mb-3 block text-[15px] font-bold before:content-['■']";
const inputClass =
  "h-[42px] w-full border border-field bg-white px-3 text-base outline-none focus:border-accent focus:ring-[3px] focus:ring-accent/15";
const errorClass = "mt-2 min-h-[1em] text-[13px] text-accent";

export default function UserForm() {
  const {
    register,
    handleSubmit,
    getValues,
    formState: { errors }
  } = useForm<Inputs>({
    mode: 'onChange',
  })
  const navigate = useNavigate();
  const [errorMsg, setErrorMsg] = useState("")
  const onSubmit: SubmitHandler<Inputs> = async (data) =>{
        const { confirmPassword, ...body } = data;
        const { response } = await api.POST('/users', { body });
        if (response.ok){  
            navigate('/');
        } else if (response.status === 403) {
            setErrorMsg('ユーザー登録の権限がありません。');
        } else if (response.status === 400) {
            setErrorMsg('入力内容を確認してください。'); 
        }
  };

  return (
    <main>
      <SectionTitle as="h1">ユーザー登録</SectionTitle>

      <form onSubmit={handleSubmit(onSubmit)} noValidate className="mx-auto max-w-[760px] px-5 pb-20">
        <p role="alert" className="mb-6 min-h-[1em] text-center text-sm text-accent">
          {errorMsg}
        </p>

        <div>
          <label htmlFor="username" className={labelClass}>ユーザ名</label>
          <input
            {...register("username", { required: true, minLength: 10, maxLength: 50 })}
            id="username"
            aria-invalid={!!errors.username}
            className={inputClass}
          />
          <p className={errorClass}>
            {errors.username && "ユーザー名は10〜50文字で入力してください。"}
          </p>
        </div>

        <div className="mt-8">
          <label htmlFor="email" className={labelClass}>メールアドレス</label>
          <input
            {...register("email", { required: true })}
            type="email"
            id="email"
            aria-invalid={!!errors.email}
            className={inputClass}
          />
          <p className={errorClass}>
            {errors.email && "正しいメールアドレスを入力してください"}
          </p>
        </div>

        <div className="mt-8">
          <label htmlFor="role" className={labelClass}>役職</label>
          <select
            {...register("role", { required: true })}
            id="role"
            aria-invalid={!!errors.role}
            className={inputClass}
          >
            <option value="">選択してください。</option>
            <option value="SALES">SALES</option>
            <option value="SUPPORT">SUPPORT</option>
            <option value="ADMIN">ADMIN</option>
          </select>
          <p className={errorClass}>{errors.role && "役職を選択してください"}</p>
        </div>

        <div className="mt-8">
          <label htmlFor="rawPassword" className={labelClass}>パスワード</label>
          <input
            {...register("rawPassword", { required: true, minLength: 8 })}
            type="password"
            id="rawPassword"
            autoComplete="new-password"
            aria-invalid={!!errors.rawPassword}
            className={inputClass}
          />
          <p className={errorClass}>
            {errors.rawPassword && "パスワードは8文字以上で入力してください"}
          </p>
        </div>

        <div className="mt-8">
          <label htmlFor="confirmPassword" className={labelClass}>パスワード確認</label>
          <input
            {...register("confirmPassword", {
              required: "パスワードを入力してください",
              minLength: 8,
              validate: (v) => v === getValues('rawPassword')
            })}
            type="password"
            id="confirmPassword"
            autoComplete="new-password"
            aria-invalid={!!errors.confirmPassword}
            className={inputClass}
          />
          <p className={errorClass}>
            {errors.confirmPassword && "パスワードが一致しません。"}
          </p>
        </div>

        <div className="mt-14 flex flex-col items-center gap-4 md:flex-row md:justify-center md:gap-6">
          <Button type="submit" size="lg">登録</Button>
          <Button type="button" size="lg" variant="outline" onClick={() => navigate('/')}>
            キャンセル
          </Button>
        </div>
      </form>
    </main>
  );
}

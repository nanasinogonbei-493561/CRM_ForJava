import { useForm } from "react-hook-form";
import { useState } from "react"
import { useNavigate } from "react-router-dom"
import type { SubmitHandler } from 'react-hook-form';
import { api } from '@/lib/apiClient'
import "./styles.css";

type Role = "SALES" | "SUPPORT" | "ADMIN";
type Inputs = {
    username: string;
    email: string;
    role: Role;
    rawPassword: string;
    confirmPassword: string; 
}

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
            navigate('/Top');
        } else if (response.status === 403) {
            setErrorMsg('ユーザー登録の権限がありません。');
        } else if (response.status === 400) {
            setErrorMsg('入力内容を確認してください。'); 
        }
  };

  return (
    <div className="App">
      <h2>ユーザー登録</h2>
      <form onSubmit={handleSubmit(onSubmit)}>
        <p className="errorMsg">{errorMsg}</p>
        <div className="signupInner">
          <div className="signupForm">
            <label htmlFor="username">ユーザ名</label>
            <input
              {...register("username", {
                required: true,
                minLength: 10,
                maxLength: 50,
              })}
              id="username"
              className="formArea"
            />
            <p className="errorText">
              {errors.username && "ユーザー名は10〜50文字で入力してください。"}
            </p>
          </div>
          <div className="signupForm">
            <label htmlFor="email">メールアドレス</label>
            <input
              {...register("email", {
                required: true,
              })}
              type="email"
              id="email"
              className="formArea"
            />
            <p className="errorText">
              {errors.email && "正しいメールアドレスを入力してください"}
            </p>
          </div>
          <div className="signupForm">
            <label htmlFor="role">役職</label>
            <select {...register("role", { required: true })} id="role">
              <option value="">選択してください。</option>
              <option value="SALES">SALES</option>
              <option value="SUPPORT">SUPPORT</option>
              <option value="ADMIN">ADMIN</option>
            </select>
            <p className="errorText">{errors.role && "役職を選択してください"}</p>
          </div>
          <div className="signupForm">
            <label htmlFor="rawPassword">パスワード</label>
            <input
              {...register("rawPassword", {
                required: true,
                minLength: 8,
              })}
              type="password"
              id="rawPassword"
              className="formArea"
            />
            <p className="errorText">
              {errors.rawPassword && "正しいパスワードを入力してください"}
            </p>
          </div>
          <div className="signupForm">
            <label htmlFor="confirmPassword">パスワード確認</label>
            <input
              {...register("confirmPassword", {
                required: "パスワードを入力してください",
                minLength: 8,
                validate: (v) => v === getValues('rawPassword')
              })}
              type="password"
              id="confirmPassword"
              className="formArea"
            />
            <p className="errorText">
              {errors.confirmPassword && "パスワードが一致しません。"}
            </p>
          </div>
        </div>
        <input type="submit" />
      </form>
    </div>
  );
}

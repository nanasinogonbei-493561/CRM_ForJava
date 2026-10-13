import { useState } from "react";
import { ErrorMessage } from '@hookform/error-message';
import { useNavigate } from "react-router-dom";
import { useForm } from 'react-hook-form';
import type { SubmitHandler } from 'react-hook-form';
import { api, setToken } from '@/lib/apiClient';
import './Login.css';

//型宣言
type Inputs = {
    email: string;
    password: string;
}

export default function Login() {
    const navigate = useNavigate();
    //errorMsg という名前のstate関数を宣言、初期値 null をセット
    const [errorMsg, setErrorMsg] = useState("")
    
    const {
        register,
        handleSubmit,
        reset,
        resetField,
        formState: { errors }
    } = useForm<Inputs>({
        mode: 'onChange',
    });

    //ログインボタンを押した際の処理
    const onSubmit: SubmitHandler<Inputs> = async (data) =>{
        const { data: res, response } = await api.POST('/auth/login', {body: data});
        if (res){  
            setToken(res.token);
            navigate('/Top');
        } else if (response.status === 401) {
            setErrorMsg('メールアドレスまたはパスワードが正しくありません。もう一度入力してください。');
            resetField('password');
        } else {
            setErrorMsg('エラーが発生しました。');  // 500 などの文例
        }
    };
    const clearForm = () => {
        reset();
        setErrorMsg("");
    }
    return (
        <div className="formContainer">
        <form onSubmit={handleSubmit(onSubmit)}>
            <h1>ログイン</h1>
            <hr />
            <div className='uiForm'>
            <p className="errorMsg">{errorMsg}</p>
                <div className='formField'>
                    <label htmlFor="email">Email</label>
                    <input 
                        id="email"
                        type="email" 
                        placeholder='email' 
                        {...register('email', { 
                            required: 'Emailを入力してください。', 
                            maxLength: {
                                value: 255,
                                message: '255文字以内で入力してください。'
                            },
                            pattern: { 
                                value: /^[^\s@]+@[^\s@]+$/, message: 'Emailの形式が不正です。'
                            },
                        })}
                    />
                </div>
                <ErrorMessage errors={errors} name="email" render={({message}) => <span>{message}</span>} />
                <div className='formField'>
                    <label htmlFor="password">パスワード</label>
                    <input 
                        id = "password"
                        type="password" 
                        placeholder='password' 
                        {...register('password', { 
                            required: 'パスワードを入力してください。', 
                        })} 
                    />
                </div>
                <ErrorMessage errors={errors} name="password" render={({message}) => <span>{message}</span>} />
                <div className="loginButton">
                    <button 
                        type = "submit"
                        className="submitButton"
                        >ログイン
                    </button>
                    <button 
                        type = "button"
                        className="clearButton" 
                        onClick={clearForm}
                        >クリア
                    </button>
                </div>
            </div>
        </form>
        </div>
  );
}
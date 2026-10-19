import { render, screen } from '@testing-library/react'; 
import userEvent from '@testing-library/user-event'; 
import { describe, expect, test, vi } from 'vitest';
import { MemoryRouter } from "react-router-dom";
import Login from "@/views/Signin/Login";
import { api } from "@/lib/apiClient";

describe('Login 記入', () => {
    test('email とパスワードを入力できる', async () => {
        const user = userEvent.setup()
        render(<MemoryRouter><Login /></MemoryRouter>)
        
        await user.type (
            await screen.getByLabelText('メールアドレス'),
            "tests@gmail.com"
        )
        await user.type (
            await screen.getByLabelText('パスワード'),
            'passwords'
        )
        expect(screen.getByLabelText('メールアドレス')).toHaveValue('tests@gmail.com');
        expect(screen.getByLabelText('パスワード')).toHaveValue('passwords');
    });
});

// api.POST を差し替える(vi.mock は import より先に巻き上げられる)
vi.mock('@/lib/apiClient', () => ({
    api: { POST: vi.fn() },
    setToken: vi.fn(),
}));

describe('Login ログイン失敗', () => {
    test('401 のときエラーメッセージが出て、パスワード欄が空になる', async () => {
        vi.mocked(api.POST).mockResolvedValue({
            data: undefined,
            response: { status: 401 },
        } as never);

        const user = userEvent.setup()
        render(<MemoryRouter><Login /></MemoryRouter>)
        await user.type(
            await screen.getByLabelText('メールアドレス'),
            'test@gmail.com'
        )
        await user.type(
            await screen.getByLabelText('パスワード'),
            ' '
        )
        await user.click(
            await screen.getByRole('button', { name: 'ログイン' })
        )

        expect(screen.getByLabelText('メールアドレス')).toHaveValue('test@gmail.com');
        expect(screen.getByLabelText('パスワード')).toHaveValue('');
        expect(await screen.findByText('メールアドレスまたはパスワードが正しくありません。もう一度入力してください。')).toBeInTheDocument()
    });
})
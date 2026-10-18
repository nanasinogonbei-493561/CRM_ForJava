import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, test, vi } from 'vitest';
import { MemoryRouter, Route, Routes } from "react-router-dom";
import UserForm from "@/views/register/UserForm";
import { api } from "@/lib/apiClient";

vi.mock('@/lib/apiClient', () => ({
    api: { POST: vi.fn() },
    setToken: vi.fn(),
}));

describe('register ユーザー登録', () => {
    test('ユーザー登録ができる', async () => {
        vi.mocked(api.POST).mockResolvedValue({
            response: { ok: true, status: 201 },
        } as never);

        const pw = 'passwords'
        const user = userEvent.setup()
        render(
            <MemoryRouter>
                <Routes>
                    <Route path="/" element={<UserForm />} />
                    <Route path="/Top" element={<p>Top画面</p>} />
                </Routes>
            </MemoryRouter>
        )

        await user.type(screen.getByLabelText('ユーザ名'), 'yamadazirou')
        await user.type(screen.getByLabelText('メールアドレス'), 'tests@gmail.com')
        await user.selectOptions(screen.getByLabelText('役職'), 'SALES')
        await user.type(screen.getByLabelText('パスワード', { exact: true }), pw)
        await user.type(screen.getByLabelText('パスワード確認'), pw)

        expect(screen.getByLabelText('ユーザ名')).toHaveValue('yamadazirou')
        expect(screen.getByLabelText('メールアドレス')).toHaveValue('tests@gmail.com')
        expect(screen.getByLabelText('役職')).toHaveValue('SALES')
        expect(screen.getByLabelText('パスワード', { exact: true })).toHaveValue(pw)
        expect(screen.getByLabelText('パスワード確認')).toHaveValue(pw)

        await user.click(screen.getByRole('button'))

        // confirmPassword は送信前に取り除かれる
        expect(api.POST).toHaveBeenCalledWith('/users', {
            body: {
                username: 'yamadazirou',
                email: 'tests@gmail.com',
                role: 'SALES',
                rawPassword: pw,
            },
        })
        expect(await screen.findByText('Top画面')).toBeInTheDocument()
    });
})

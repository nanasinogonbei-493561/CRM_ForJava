import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { useState } from "react";

// 選択肢を一箇所に固定し、選べるようにする。
const ROLES = ["SALES", "SUPPORT", "ADMIN"] as const;
type Role = (typeof ROLES)[number];



export default function UserForm() {
  return (
    <form className="flex flex-col gap-6 mx-auto max-w-130">
      <h2 className="font-bold">ユーザー登録フォーム</h2>

      <div className="flex flex-col gap-3">
        <Label htmlFor="name">名前</Label>
        <Input type="text" id="name" placeholder="例：山田太郎" />
      </div>

      <div className="flex flex-col gap-3">
        <Label htmlFor="email">メールアドレス</Label>
        <Input type="email" id="email" placeholder="例：example@example.com" />
      </div>

      <div className="flex flex-col gap-3">
        <Label htmlFor="role">権限</Label>
        <Input  id="role" placeholder="例：SALES" />
      </div>

      <div className="flex flex-col gap-3">
        <Label htmlFor="password">パスワード</Label>
        <Input type="text" id="password" placeholder="例：abcd1234" />
      </div>

      <Button>登録する</Button>
    </form>
  );
}
# 基本設計書 — 顧客管理CRM

| 項目 | 内容 |
|---|---|
| 文書バージョン | 0.1（ドラフト） |
| 作成日 | YYYY-MM-DD |
| 作成者 | （あなたの名前） |
| 関連文書 | 要件定義書 v0.1（RDD.md） |
| 更新履歴 | 0.1: 初版作成 |

> **この雛形の使い方**
> - 各章の冒頭にある「📝 書くこと」を読み、表やプレースホルダ `___` を埋めていきます。
> - 各章末尾の「← RDD」は、要件定義書のどこを材料にするかの対応です。迷ったらそこを開いてください。
> - 基本設計は「ユーザーから見える部分（外部設計）」を決める工程です。プログラムの内部処理は詳細設計に回します。
> - 章番号はこのまま目次になります。書く順番は自由ですが、4章（データ設計）が決まると他の章が書きやすくなります。

---

## 1. システム概要・全体構成

📝 **書くこと**：このシステムが何のためのものか（目的）と、全体像（3層構成の図）。

### 1.1 目的
`___`（要件定義書 1.2 の目的を1〜2文で要約。例：顧客情報と対応履歴を一元管理し、属人化を解消して対応品質と効率を上げる）

### 1.2 システム全体構成
本システムは以下の3層構成とする。

- フロントエンド：React + TypeScript（Vite / React Router / Tailwind CSS）
- バックエンド：Java + Spring Boot（Spring Web / Spring Data JPA / Spring Security）
- データベース：MySQL または PostgreSQL

```mermaid
flowchart LR
    B[ブラウザ<br/>React/TS] -->|HTTPS / REST| A[APサーバ<br/>Spring Boot]
    A -->|JPA| D[(DB<br/>MySQL/PostgreSQL)]
```

← **RDD**：1.2 目的 / 6.3 技術的制約

---

## 2. 画面設計

📝 **書くこと**：どんな画面があり（一覧）、どう行き来し（遷移図）、各画面に何が並ぶか（項目）。

### 2.1 画面一覧
構成（RDD 2.1）と機能一覧（RDD 4.1）を突き合わせて、画面を洗い出す。

| 画面ID | 画面名 | 対応機能ID | アクセス権限 |
|---|---|---|---|
| SCR-01 | ログイン | F-001 | 全員 |
| SCR-02 | 顧客一覧 | F-006, F-019, F-020, F-021, F-022 | 全員 |
| SCR-03 | 顧客詳細 | F-007, F-018 | 全員 |
| SCR-04 | 顧客登録・編集 | F-003, F-004 | 営業／管理者 |
| SCR-05 | `___` | `___` | `___` |
| ... | `___` | `___` | `___` |

### 2.2 画面遷移図
どの画面からどの画面へ移動できるかを矢印で示す。

```mermaid
flowchart TD
    L[SCR-01 ログイン] --> List[SCR-02 顧客一覧]
    List --> Detail[SCR-03 顧客詳細]
    List --> New[SCR-04 顧客登録・編集]
    Detail --> New
    Detail --> Inq[___ 問い合わせ詳細]
    %% ___ 残りの遷移を追記する
```

### 2.3 画面レイアウト・項目定義
画面ごとに、表示・入力項目を定義する（画面の数だけ繰り返す）。

#### SCR-02 顧客一覧
| 項目名 | 種別 | 必須 | 説明 |
|---|---|---|---|
| 検索キーワード | テキスト入力 | 任意 | 会社名・担当者名の部分一致（20文字以内） |
| ステータス絞込 | プルダウン | 任意 | 新規／対応中／保留／完了 |
| 顧客一覧テーブル | 表 | - | 会社名・担当者・最新ステータスを20件/ページで表示 |
| ページネーション | ボタン | - | 前へ／次へ |
| `___` | `___` | `___` | `___` |

#### SCR-03 顧客詳細
| 項目名 | 種別 | 必須 | 説明 |
|---|---|---|---|
| `___` | `___` | `___` | `___` |

← **RDD**：2.1 構成 / 4.1 機能一覧 / 4.2 機能詳細

---

## 3. 機能設計

📝 **書くこと**：各機能を「どの画面で・何を入力し・どう処理し・何を出すか」で表にする。RDD 4.2 の機能詳細をこの形に展開する。

| 機能ID | 機能名 | 画面ID | 入力 | 処理 | 出力 | 例外 |
|---|---|---|---|---|---|---|
| F-019 | キーワード検索 | SCR-02 | 会社名・担当者のキーワード | 部分一致検索／20件ずつ昇順 | 顧客一覧（会社名・担当者・ステータス） | 0件／未入力／20文字超 |
| F-005 | 顧客削除 | SCR-03 | 削除ボタン＋確認ダイアログ | 論理削除 | 「削除しました」表示 | キャンセル／二重削除／権限なし |
| F-003 | 顧客登録 | SCR-04 | `___` | `___` | `___` | `___` |
| ... | `___` | `___` | `___` | `___` | `___` | `___` |

← **RDD**：4.1 機能一覧 / 4.2 機能詳細

---

## 4. データベース設計（論理）  ※一緒に作成済み

📝 **書くこと**：エンティティ（テーブルの素）とその関連（ER図）、各テーブルの列定義。

### 4.1 ER図
RDD 1.3 用語定義・2.5 データスコープから抽出。`status` は「問い合わせ」の属性、タグは多対多のため中間テーブル `CUSTOMER_TAGS` を置く。

```mermaid
erDiagram
    USERS ||--o{ CUSTOMERS : 担当
    USERS ||--o{ INQUIRIES : 対応
    USERS ||--o{ RESPONSES : 記録
    CUSTOMERS ||--o{ INQUIRIES : 起票
    INQUIRIES ||--o{ RESPONSES : 履歴
    CUSTOMERS ||--o{ CUSTOMER_TAGS : 付与
    TAGS ||--o{ CUSTOMER_TAGS : 付与
    USERS {
        bigint id PK
        string name
        string email
        string role
    }
    CUSTOMERS {
        bigint id PK
        string company_name
        string contact_person
        bigint user_id FK
    }
    INQUIRIES {
        bigint id PK
        bigint customer_id FK
        bigint assignee_id FK
        string title
        string status
    }
    RESPONSES {
        bigint id PK
        bigint inquiry_id FK
        bigint user_id FK
        text content
        datetime created_at
    }
    TAGS {
        bigint id PK
        string name
    }
    CUSTOMER_TAGS {
        bigint customer_id FK
        bigint tag_id FK
    }
```

### 4.2 テーブル定義
ER図の各テーブルを、列ごとに定義する。下は記入例（CUSTOMERS）。残りのテーブルも同じ形式で `___` を埋める。

#### CUSTOMERS（顧客）
| 列名 | 型 | PK/FK | NOT NULL | 説明 |
|---|---|---|---|---|
| id | BIGINT | PK | ○ | 連番 |
| company_name | VARCHAR(100) | | ○ | 会社名 |
| contact_person | VARCHAR(50) | | | 担当者名 |
| user_id | BIGINT | FK→USERS.id | ○ | 担当営業 |
| is_deleted | BOOLEAN | | ○ | 論理削除フラグ（F-005対応） |
| created_at | DATETIME | | ○ | 登録日時 |

#### INQUIRIES（問い合わせ）
| 列名 | 型 | PK/FK | NOT NULL | 説明 |
|---|---|---|---|---|
| id | BIGINT | PK | ◯ | 連番 |
| customer_id | BIGINT | FK→CUSTOMERS.id | ◯ | 顧客ID |
| assignee_id | BIGINT | FK→USERS.id | ◯ | 担当者ID |
| title | VARCHAR(100) |  | ◯ |  | タイトル |
| status | VARCHAR(25) |  | ◯ | ステータス。DEFAULT「新規」 |
| is_deleted | BOOLEAN |  | ◯ | 問い合わせの情報を論理削除する |
| created_at | DATETIME |  | ◯ | 問い合わせの情報を登録する |

#### RESPONSES（対応履歴）
| 列名 | 型 | PK/FK | NOT NULL | 説明 |
|---|---|---|---|---|
| id | BIGINT | PK | ◯ | 対応履歴のID |
| inquiry_id | BIGINT | FK→INQUIRIES.id | ◯ | 問い合わせのID |
| user_id | BIGINT | FK→USERS.id | ◯ | 記録者ID |
| content | TEXT |  | ◯ | メモ |
| created_at | DATETIME |  | ◯ | 対応履歴の登録 |

#### USERS(ユーザー)
| 列名 | 型 | PK/FK | NOT NULL | 説明 |
|---|---|---|---|---|
| id | BIGINT | PK | ◯ | ユーザーID |
| name | VARCHAR(50) |  | ◯ | 営業担当者 |
| email | VARCHAR(255) |  | ◯ | 一意(UNIQUE)にしたい |
| role | VARCHAR(20) |  | ◯ | 営業/サポート/管理者の3択 |
| is_deleted | BOOLEAN |  | ◯ | 退職した担当者を論理削除扱いにする |
| password_hash | VARCHAR(255) |  | ◯ | 平文NG。 |
| locked | BOOLEAN |  | ◯ | 3回間違えたら、ロックする |
| failed_login_count | INT |  |  ◯ | DEFAULT 0。３回失敗でロックを実装するには、現在の失敗回数をカウントする必要がある |
| created_at | DATETIME |  | ◯ | CUSTOMERSにもあるため |

#### TAGS(タグ)
| 列名 | 型 | PK/FK | NOT NULL | 説明 |
|---|---|---|---|---|
| id | BIGINT | PK | ◯ | タグのID |
| name | VARCHAR(50) |  | ◯ | タグの名前。一意(UNIQUE)にしたい |

#### CUSTOMER_TAGS(顧客タグ)
| 列名 | 型 | PK/FK | NOT NULL | 説明 |
|---|---|---|---|---|
| customer_id | BIGINT | PK, FK→CUSTOMERS.id | ◯ | 顧客ID、複合PK |
| tag_id | BIGINT | PK, FK→TAGS.id | ◯ | タグのID、複合PK |

← **RDD**：1.3 用語定義 / 2.5 データスコープ

---

## 5. 外部インターフェース設計（REST API）

📝 **書くこと**：フロントとバックがやり取りするAPIを一覧にする。URL・メソッド・入出力をセットで。

| No | 機能 | メソッド | エンドポイント | 主なリクエスト | 主なレスポンス |
|---|---|---|---|---|---|
| 1 | ログイン | POST | /api/auth/login | email, password | トークン |
| 2 | ログアウト | POST | /api/auth/logout | トークン | ログアウト成功のメッセージ |
| 3 | 顧客一覧取得 | GET | /api/customers | page, keyword, status | 顧客配列＋件数 |
| 4 | 顧客登録 | POST | /api/customers | 顧客情報 | 作成された顧客 |
| 5 | 顧客詳細取得 | GET | /api/customers/{customerId} | (URLのid) | 顧客の詳細情報 |
| 6 | 顧客編集 | PUT | /api/customers/{customerId} | 担当者割当や変更などの顧客情報の変更を依頼 | 顧客情報を変更 |
| 7 | 顧客削除 | DELETE | /api/customers/{customerId} | 顧客情報の削除要請 | 顧客情報を削除 |
| 8 | 問い合わせ一覧取得 | GET | /api/customers/{customerId}/inquiries | 対応する顧客の問い合わせ履歴 | 問い合わせ配列 |
| 9 | 問い合わせ登録 | POST | /api/customers/{customerId}/inquiries | 対応する顧客の問い合わせ情報 | 作成された顧客問い合わせ |
| 10 | 問い合わせ詳細取得 | GET | /api/inquiries/{inquiryId} | (inquiriesのid) | 顧客問い合わせの詳細情報 |
| 11 | 問い合わせ編集 | PUT | /api/inquiries/{inquiryId} | 顧客問い合わせの変更を依頼 | 顧客問い合わせの変更 |
| 12 | 対応履歴詳細取得 | GET | /api/inquiries/{inquiryId}/responses/{responseId} | (responsesのid) | 対応履歴の詳細情報 |
| 13 | 対応履歴登録 | POST | /api/inquiries/{inquiryId}/responses | 対応履歴情報 | 作成された対応履歴 |
| 14 | 対応閲覧(一覧) | GET | /api/inquiries/{inquiryId}/responses | 問い合わせID | 対応履歴の配列 |
| 15 | 対応履歴編集 | PUT | /api/inquiries/{inquiryId}/responses/{responseId} | 対応履歴の変更を依頼 | 対応履歴を変更 |
| 16 | 対応履歴削除 | DELETE | /api/inquiries/{inquiryId}/responses/{responseId} | 対応履歴情報の削除要請 | 削除成功「削除しました」 |
| 17 | タグ一覧取得 | GET | /api/tags |  | タグの配列 |
| 18 | タグ編集 | PUT | /api/tags/{tagId} | タグ情報の変更要請 | タグ情報の変更 |
| 19 | タグ削除 | DELETE | /api/tags/{tagId} | タグ情報の削除要請 | タグ情報の削除 |
| 20 | タグ作成 | POST | /api/tags | タグ情報 | 作成されたタグ情報 |
| 21 | タグ解除 | DELETE | /api/customers/{customerId}/tags/{tagId} | 顧客のタグ解除要請 | 顧客のタグの解除 |
| 22 | ユーザー管理一覧 | GET | /api/users | page, keyword, ユーザー名 | ユーザーの配列 |
| 23 | ユーザー管理登録 | POST | /api/users | ユーザー情報 | 作成されたユーザー情報 |
| 24 | ユーザー管理変更 | PUT | /api/users/{userId} | 退職者含むユーザーID | ユーザーの変更 |
| 25 | ロック解除 | PUT | /api/users/{userId}/unlock | ユーザーID | ユーザーのロック解除 |


← **RDD**：追加要素「REST API設計」/ ページネーション・検索・ソート

---

## 6. 共通方式設計

📝 **書くこと**：全機能に共通する「やり方の方針」。各機能でいちいち書かずにここへまとめる。

- 認証・認可：
  1. 認証方式
  - JWT(トークン方式)、ブラックリスト(失効済みトークン一覧)を持つ(詳細設計にて)
  - 24時間で失効
  - HTTPS必須
  2. 認可
   - ロールは営業担当者/カスタマーサポート/管理者の３つ。
    - 権限(全アクター除く)
     1. 営業担当者: 顧客登録・編集(担当者割当含む)、問い合わせ登録、編集、対応登録、ステータス管理、タグ付与、画面内バッジ
     2. カスタマーサポート: 問い合わせ登録・編集、対応登録、ステータス管理、画面内バッジ
     3. 管理者: 顧客削除、タグ作成・編集・削除、ユーザー管理
   - ロールベースでAPI層を制御
  3.  パスワード・アカウントロック
   - ハッシュ化でbcrypt
   - 3回失敗でロック(USERSのfailed_login_count/locked)
   - 失敗時は「メールアドレスまたはパスワードが正しくありません。もう一度入力してください。」
   - 成功時は、「ログインしました。」
   - ロック解除は管理者のみ
- 例外処理：
 1. レスポンス形式(全APIのエラーは下記JSONに統一)
  ```json
  {                                                                             
    "timestamp": "2026-06-14T10:00:00Z",
    "status": 400,                                                              
    "error": "Bad Request",                                                   
    "message": "会社名は必須です",                                              
    "path": "/api/customers"      
  }
  ```
 2. ステータスコード
  | ステータスコード | エラー詳細 |
  |--|--|
  | 400 | バリデーションエラー(必須なし、桁数超過) |
  | 401 | 未認証(トークン無し/失効、ログイン失敗) |
  | 403 | 権限なし |
  | 404 | 対象なし(存在しないID) |
  | 409 | 競合(二重削除、メール重複) |
  | 500 | 想定外エラー |
 3. メッセージ方針
   - ユーザー向け: 内部情報(SQL・スタックトレース・どの項目か等)は出さず、平易な文言にする。
   - ログ向け: スタックトレース等の詳細を記録する(→ログ項目と連携)
   - 各コードの文例は下記:
     - 400: 必須項目に入力エラーが発生しました。
     - 401: ログイン失敗しました。
     - 403: 管理者にお問い合わせください。
     - 404: 一覧画面に戻る。
     - 409: 競合してます。
     - 500: エラーが発生しました。
- ログ：
  1. 何を記録するか
    - アクセスログ: 
<!-- TODO(human): ログの方針を記述する。(1)何を記録するか（アクセス/エラー/監査ログ）、(2)形式（構造化JSON）、(3)ログレベルの使い分け、(4)保管期間5年、を記述する。パスワード・トークンはログに出さないことに注意。 -->
- 入力バリデーション：`___`（追加要素「入力バリデーション」：必須・桁数・文字種の方針）

← **RDD**：5.5 セキュリティ / 追加要素

---

## 7. 非機能設計

📝 **書くこと**：RDD 5章の非機能要件を「どう実現するか」に翻訳する。

| 区分 | 要件（RDD 5章） | 実現方式 |
|---|---|---|
| 性能 | 検索・画面表示3秒以内 | `___`（例：検索列にインデックス、ページネーション） |
| 可用性 | 稼働率99.5%、復旧2時間以内 | `___` |
| 拡張性 | 同時17人→30人 | `___` |
| 運用・保守 | 日次バックアップ、ログ5年 | `___` |
| セキュリティ | HTTPS、ロック、論理削除 | `___` |

← **RDD**：5章すべて

---

## 付録

- 関連文書：要件定義書（RDD.md）
- 参考：IPA 非機能要求グレード
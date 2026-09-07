// 暫定の手書きスタブ。
// backend にまだ springdoc が入っておらず /v3/api-docs が存在しないため、
// `npm run gen:api` による自動生成が動かない。導入後はこのファイルを生成物で置き換える。
// 形は openapi-typescript の出力に合わせてある（paths / components / operations の 3 本立て）。

export interface paths {
  "/users/{user_id}": {
    get: operations["get_user"];
  };
}

export interface components {
  schemas: {
    User: {
      id: number;
      name: string;
      email: string;
    };
  };
}

export interface operations {
  get_user: {
    parameters: {
      path: { user_id: number };
    };
    responses: {
      200: {
        content: { "application/json": components["schemas"]["User"] };
      };
      404: {
        content: never;
      };
    };
  };
}

// 暫定の手書きスタブ。
// backend にまだ springdoc が入っておらず /v3/api-docs が存在しないため、
// `npm run gen:api` による自動生成が動かない。導入後はこのファイルを生成物で置き換える。
// 形は openapi-typescript の出力に合わせてある（paths / components / operations の 3 本立て）。

export interface paths {
  "/users/{user_id}": {
    get: operations["get_user"];
  };
  "/auth/login": {
    post: operations["login"];
  };
  "/auth/logout": {
    post: operations["logout"];
  };
  "/users": {
    post: operations["register"];
  };
}

export interface components {
  schemas: {
    // backend の UserResponse に対応。passwordHash は含まれない
    User: {
      id: number;
      username: string;
      email: string;
      role: "SALES" | "SUPPORT" | "ADMIN";
      locked: boolean;
      createdAt: string;
    };
    // backend の LoginResponse に対応。expiresAt は ISO-8601(Instant)
    LoginResponse: {
      token: string;
      tokenType: string;
      expiresAt: string;
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

  login: {
    requestBody: {
      content: {
        "application/json": { 
          email: string;
          password: string;
        };
      };
    };
    responses: {
      200: {
        content: {
          "application/json": components["schemas"]["LoginResponse"];
        };
      };
      401: {
        content: never
      };
    };
  };

  logout: {
    responses: {
      200: {content: never};
    };
  };

  register: {
    requestBody: {
      content: {
        "application/json": {
          username: string;
          email: string;
          role: "SALES" | "SUPPORT" | "ADMIN";
          rawPassword: string;
        };
      };
    };
    responses: {
      201: {
        content: {
          "application/json": components["schemas"]["User"];
        };
      };
      400: {
        content: never
      };
      401: {
        content: never
      };
      403: {
        content: never
      };
    };
  };
}

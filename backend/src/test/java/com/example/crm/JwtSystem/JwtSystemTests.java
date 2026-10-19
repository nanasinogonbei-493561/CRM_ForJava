package com.example.crm.JwtSystem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
// 注意: Boot 4.1 でパッケージが移動している。
// Boot 3.x の org.springframework.boot.test.autoconfigure.web.servlet ではない。
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.example.crm.Entity.UserEntity;
import com.example.crm.Enum.Role;
import com.example.crm.Repository.UserRepository;
import com.example.crm.Service.UserService;
import com.jayway.jsonpath.JsonPath;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * JWT 認証の結合テスト。
 *
 * 【@WebMvcTest ではなく @SpringBootTest を使う理由】
 * JwtAuthenticationFilter は毎リクエスト DB から UserDetails を読み直す設計になっている。
 * Repository をモックするとその性質がテストできなくなるため、H2 の実 DB を使う。
 *
 * 【@AutoConfigureMockMvc だけで Security が効く理由】
 * spring-boot-security-test の SecurityMockMvcAutoConfiguration が、
 * MockMvc に Security フィルタチェーンを自動で組み込む。
 * webAppContextSetup(...).apply(springSecurity()) を手書きする必要はない。
 *
 * 【@Transactional】
 * 各テスト後にロールバックされるので、テスト間でユーザーが残らない。
 * MockMvc はテストと同じスレッドでフィルタチェーンを回すため、テスト側で書いた
 * 未コミットの変更もリクエスト側から見える（④のロック検証はこの性質に乗っている）。
 * ArchitectureTests は DoNotIncludeTests でテストを解析対象外にしているため、
 * 「@Transactional は Service だけ」のルールには抵触しない。
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class JwtSystemTests {

    private static final String EMAIL = "jwt-test@example.com";
    private static final String RAW_PASSWORD = "rawPass123";
    // UserEntity の @Size(min = 10) を満たす必要がある（短いと flush 時に例外）
    private static final String USERNAME = "JWTテスト用ユーザー";

    /** ヘッダ.ペイロード.署名 の3分割構造。Base64URL なので使える文字は [A-Za-z0-9_-] */
    private static final String JWT_STRUCTURE = "[\\w-]+\\.[\\w-]+\\.[\\w-]+";

    /**
     * ③で使う「サーバが知らない鍵」。
     * jjwt は鍵長からアルゴリズムを決めるので、サーバ側の鍵（application.properties の
     * jwt.secret = 64バイト → HS512）と同じ長さにしておく。長さがズレると
     * 「署名が違う」ではなく「アルゴリズムに対して鍵が短い」で落ち、検証したい理由が変わってしまう。
     */
    private static final SecretKey 攻撃者の鍵 = Keys.hmacShaKeyFor(
            "attacker-key-64bytes-0123456789012345678901234567890123456789xy"
                    .getBytes(StandardCharsets.UTF_8));

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    // register() 経由で作る。BCrypt ハッシュ化まで本番と同じ経路を通るので、
    // ログインで生パスワードを送れる。
    @BeforeEach
    void 検証用のユーザーを1人用意する() {
        userService.register(USERNAME, EMAIL, Role.SALES, RAW_PASSWORD);
    }

    // ------------------------------------------------------------------
    // ① トークン発行
    // ------------------------------------------------------------------
    @Test
    void ログインに成功するとJWTが発行される() throws Exception {
        String body = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "%s", "password": "%s"}
                                """.formatted(EMAIL, RAW_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", matchesPattern(JWT_STRUCTURE)))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresAt").exists())
                .andReturn().getResponse().getContentAsString();

        // expiresAt は実行時刻で変わるので固定値では書けない。「未来であること」を確かめる。
        String expiresAt = JsonPath.read(body, "$.expiresAt");
        assertThat(Instant.parse(expiresAt)).isAfter(Instant.now());

        // 認証成功イベント → AuthenticationEventListener → resetLoginFailures が走った証拠。
        assertThat(userRepository.findByEmail(EMAIL).orElseThrow().getFailedLoginCount()).isZero();
    }

    // ------------------------------------------------------------------
    // ② 有効トークンなら通る / トークンが無ければ通らない
    // ------------------------------------------------------------------
    @Test
    void 有効なトークンを付ければ保護されたAPIにアクセスできる() throws Exception {
        String token = ログインしてトークンを取得する();

        mockMvc.perform(get("/users")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void トークンが無ければ保護されたAPIは401を返す() throws Exception {
        // httpBasic を外し HttpStatusEntryPoint(UNAUTHORIZED) を入れた結果、
        // 未認証は 403 ではなく 401 になる。この1行が「決定0」をコードに固定する。
        mockMvc.perform(get("/users"))
                .andExpect(status().isUnauthorized());
    }

    // ------------------------------------------------------------------
    // ③ 署名が違うトークンは弾かれる
    // ------------------------------------------------------------------
    @Test
    void 別の鍵で署名されたトークンは401になる() throws Exception {
        // 構造も subject も期限も「正しい」。違うのは署名だけ。
        String 偽トークン = Jwts.builder()
                .subject(EMAIL)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(攻撃者の鍵)
                .compact();

        mockMvc.perform(get("/users")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + 偽トークン))
                .andExpect(status().isUnauthorized());
    }

    // ------------------------------------------------------------------
    // ④ ロック済みユーザーの「署名的には完全に有効な」トークンは弾かれる
    // ------------------------------------------------------------------
    @Test
    void ロックされたユーザーの有効なトークンは401になる() throws Exception {
        // Given: まだ正常な状態でトークンを取る。順序が重要で、先にロックすると
        //        ログイン自体が LockedException で失敗し、別のことを試すテストになってしまう。
        String token = ログインしてトークンを取得する();

        // ロックの作り方は (a) 直接 setLocked を選択。
        // 「3回失敗でロックされる」ことは UserEntity 側で確かめる話なので、
        // ここでは「ロック済みなら弾く」1点だけを主張したい。
        // orElseThrow() なのは、ユーザーが居なければ前提が崩れており
        // 静かに素通りされるより即座に失敗した方がよいから。
        UserEntity user = userRepository.findByEmail(EMAIL).orElseThrow();
        user.setLocked(true);
        // @Transactional 内の管理状態 Entity なので save() は不要。
        // フィルタ側の findByEmail 実行前に Hibernate が自動 flush する。

        // When / Then: 署名も期限も完全に有効なトークン。にもかかわらず弾かれる。
        mockMvc.perform(get("/users")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    /**
     * ログインして token だけ取り出す。②③④で使い回す。
     * 「テストの前提を作る手続き」であって検証ではないので、
     * ここでの assert は最小限（200 でなければ後続が無意味になるのでそれだけ確認）にする。
     */
    private String ログインしてトークンを取得する() throws Exception {
        String body = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "%s", "password": "%s"}
                                """.formatted(EMAIL, RAW_PASSWORD)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        return JsonPath.read(body, "$.token");
    }
}

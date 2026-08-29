package com.example.crm.Architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

/**
 * アーキテクチャテスト。
 *
 * Spring は起動しない。target/classes のバイトコードを読んで依存グラフを作り、
 * 「設計上の約束」がコードで破られていないかを検査する。
 *
 * importOptions で本番コードだけを対象にする(テストコードは解析しない)。
 *
 * すべてのルールに .because() を付けている。失敗時のメッセージに
 * 「なぜこの約束があるのか」が出るので、テストがそのまま設計文書になる。
 */
@AnalyzeClasses(
        packages = "com.example.crm",
        importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTests {

    /** このプロジェクトは jakarta 版を使っているが、Spring 版が紛れ込んでも検知したい。 */
    private static final String JAKARTA_TRANSACTIONAL = "jakarta.transaction.Transactional";
    private static final String SPRING_TRANSACTIONAL = "org.springframework.transaction.annotation.Transactional";

    // ---------------------------------------------------------
    // 1. 層の依存方向(旧 repositoryShouldNotDependOnUpperLayers /
    //    apiShouldNotDependOnRepository をこの1本に統合)
    // ---------------------------------------------------------

    /**
     * Api → Service → Repository の一方向だけを許す。
     * 個別の noClasses() ルールを並べるより、層の全体像が1か所で読める。
     *
     * ここでは .layer("名前").definedBy("パッケージ") で名前とパッケージを紐付けたので、
     * 以降の .whereLayer(...) では「層名」だけで参照できる。
     * (noClasses() 系のルールにはこの紐付けが無いので、常に生のパッケージパターンを書く)
     *
     * Config を層として宣言しているのは、Spring の設定・セキュリティ部品が
     * Service や Repository を直接触っており、それを「無いこと」にしないため。
     */
    @ArchTest
    static final ArchRule layerDependenciesAreRespected =
            layeredArchitecture()
                    .consideringAllDependencies()

                    .layer("Api").definedBy("..Api..")
                    .layer("Service").definedBy("..Service..")
                    .layer("Repository").definedBy("..Repository..")
                    .layer("Config").definedBy("..Config..")

                    .whereLayer("Api").mayNotBeAccessedByAnyLayer()

                    // mayOnlyBeAccessedByLayers(...) の引数は「この層を呼んでよい側」の一覧。
                    // 自分自身は書かない(同一層内のアクセスは常に許可される)。
                    // Config が入っているのは AdminBootstrap / AuthenticationEventListener が
                    // UserService を使っており、これは Service 経由なので問題ないため。
                    .whereLayer("Service").mayOnlyBeAccessedByLayers("Api", "Config")

                    // Repository を呼んでよいのは Service だけ。Config を意図的に含めていない。
                    // このため現在 3 件の違反が残り、このルールは赤いままになる:
                    //   - AdminBootstrap (3件) ... 起動時の管理者登録で UserRepository を直接使用
                    // 赤を「未解決の設計負債」として残し、解消の起点にする。
                    //
                    // このルールが最初に炙り出した LoginFailureHandler (4件) は削除済み。
                    // formLogin が未配線で一度も呼ばれないデッドコードであり、かつ
                    // AuthenticationEventListener が同じ責務を認証方式に依存しない形で
                    // 既に実装していたため(2026-08-29)。
                    .whereLayer("Repository").mayOnlyBeAccessedByLayers("Service")

                    .because("依存は Api → Service → Repository の一方向。逆流すると層を単独で差し替え・テストできなくなる。");

    // ---------------------------------------------------------
    // 2. 層の内部ルール
    // ---------------------------------------------------------

    /**
     * Repository は Spring Data のインタフェースとして定義する。
     * 実装クラスを手書きし始めた瞬間に気づけるようにしておく。
     */
    @ArchTest
    static final ArchRule repositoriesShouldBeInterfaces =
            classes()
                    .that().resideInAPackage("..Repository..")
                    .should().beInterfaces()
                    .because("Repository は Spring Data が実装を生成する。手書き実装が現れたら設計判断が変わった合図。");

    /**
     * Entity は「業務ルールを持つドメインモデル」であって、HTTP の都合を知ってはいけない。
     * Entity が DTO を参照し始めると、API の形を変えるたびにドメインが揺れる。
     */
    @ArchTest
    static final ArchRule entityShouldNotDependOnWebLayer =
            noClasses()
                    .that().resideInAPackage("..Entity..")
                    .should().dependOnClassesThat().resideInAPackage("..Web..")
                    .because("Entity は業務ルールの置き場。HTTP 表現(DTO)を知ると、API 変更がドメインに波及する。");

    /**
     * トランザクション境界は Service に置く。
     * Api に付けると境界が HTTP の形に引きずられ、Repository に付けると
     * 「複数の保存をまとめて1トランザクション」が表現できなくなる。
     */
    @ArchTest
    static final ArchRule transactionalMethodsShouldResideInService =
            methods()
                    .that().areAnnotatedWith(JAKARTA_TRANSACTIONAL)
                    .or().areAnnotatedWith(SPRING_TRANSACTIONAL)
                    .should().beDeclaredInClassesThat().resideInAPackage("..Service..")
                    .because("トランザクション境界は Service が持つ。ここが業務的に意味のある1単位の作業だから。");

    /** クラス単位で @Transactional を付けて境界を持ち出すのも同じ理由で禁止。 */
    @ArchTest
    static final ArchRule transactionalClassesShouldResideInService =
            noClasses()
                    .that().resideOutsideOfPackage("..Service..")
                    .should().beAnnotatedWith(JAKARTA_TRANSACTIONAL)
                    .orShould().beAnnotatedWith(SPRING_TRANSACTIONAL)
                    .because("トランザクション境界は Service が持つ。クラス単位でも外に漏らさない。");

    // ---------------------------------------------------------
    // 3. パッケージ全体の健全性
    // ---------------------------------------------------------

    /**
     * com.example.crm の直下パッケージ(Api / Service / Repository / Entity / ...)を
     * ひとつの「スライス」とみなし、その間に循環依存がないことを検査する。
     *
     * 層のルールが名指しの2者関係を守るのに対し、こちらは
     * A → B → C → A のような、遠回りで生まれる循環を捕まえる。
     */
    @ArchTest
    static final ArchRule packagesShouldBeFreeOfCycles =
            slices()
                    .matching("com.example.crm.(*)..")
                    .should().beFreeOfCycles()
                    .because("循環したパッケージは一緒にしかビルド・理解・テストできない。分割した意味が消える。");
}

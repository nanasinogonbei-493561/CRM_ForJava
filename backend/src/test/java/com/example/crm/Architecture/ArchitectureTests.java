package com.example.crm.Architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * アーキテクチャテスト。
 *
 * Spring は起動しない。target/classes のバイトコードを読んで依存グラフを作り、
 * 「設計上の約束」がコードで破られていないかを検査する。
 *
 * importOptions で本番コードだけを対象にする(テストコードは解析しない)。
 */
@AnalyzeClasses(
        packages = "com.example.crm",
        importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTests {

    /**
     * 下位層は上位層を知らない。
     * Repository が Api/Service を参照し始めると依存が双方向になり、
     * Repository だけを差し替える・テストするのが不可能になる。
     */
    @ArchTest
    static final ArchRule repositoryShouldNotDependOnUpperLayers =
            noClasses()
                    .that().resideInAPackage("..Repository..")
                    .should().dependOnClassesThat().resideInAnyPackage("..Api..", "..Service..");

    /**
     * Repository は Spring Data のインタフェースとして定義する。
     * 実装クラスを手書きし始めた瞬間に気づけるようにしておく。
     */
    @ArchTest
    static final ArchRule repositoriesShouldBeInterfaces =
            classes()
                    .that().resideInAPackage("..Repository..")
                    .should().beInterfaces();

    
    @ArchTest
    static final ArchRule apiShouldNotDependOnRepository =
           noClasses().that().resideInAPackage("..Api..")
               .should().dependOnClassesThat().resideInAPackage("..Repository..").because("Api は Service 経由でのみ永続化層に触れる。");
}

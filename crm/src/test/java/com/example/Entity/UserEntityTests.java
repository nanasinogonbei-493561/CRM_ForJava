package com.example.Entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest; // ★Boot 4系のパッケージ
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager; // ★同上
import org.springframework.dao.DataIntegrityViolationException;

import com.example.Enum.Role;
import com.example.Repository.UserRepository;

import jakarta.validation.ConstraintViolationException;


@DataJpaTest
class UserEntityTests {

    @Autowired
    TestEntityManager em;

    @Autowired
    UserRepository userRepository;

    /** @Size(min=10) を満たす12文字の名前でテスト用ユーザーを作る */
    private User newUser(String email) {
        return new User("テスト用ユーザー山田太郎", email, Role.SALES, "$2a$10$dummyHash");
    }

    @Test
    void 保存するとidとcreated_atが自動で入る() {
        User saved = userRepository.saveAndFlush(newUser("taro@example.com"));
        em.clear(); // 1次キャッシュを消して、本当にDBから読み直す

        User found = userRepository.findById(saved.getId()).orElseThrow();

        assertThat(found.getId()).isNotNull();        // @GeneratedValue(IDENTITY)
        assertThat(found.getCreatedAt()).isNotNull(); // @CreationTimestamp(source = DB)
    }

    @Test
    void findByEmailで取得できる() {
        userRepository.saveAndFlush(newUser("hanako@example.com"));
        em.clear();

        assertThat(userRepository.findByEmail("hanako@example.com")).isPresent();
        assertThat(userRepository.findByEmail("nobody@example.com")).isEmpty();
    }

    @Test
    void emailの一意制約が効く() {
        userRepository.saveAndFlush(newUser("dup@example.com"));

        assertThatThrownBy(() -> userRepository.saveAndFlush(newUser("dup@example.com")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void deleteは物理削除ではなく論理削除になる() {
        User saved = userRepository.saveAndFlush(newUser("delete@example.com"));
        Long id = saved.getId();

        userRepository.delete(saved); // ここで @SQLDelete の UPDATE 文が発行される
        em.flush();
        em.clear();

        // @SQLRestriction("is_deleted = false") によりJPA経由では見えなくなる
        assertThat(userRepository.findById(id)).isEmpty();

        // ただし行は物理的に残っていて is_deleted = true のはず（生SQLで直接確認）
        Number count = (Number) em.getEntityManager()
                .createNativeQuery(
                    "SELECT COUNT(*) FROM users WHERE id = :id AND is_deleted = true")
                .setParameter("id", id)
                .getSingleResult();
        assertThat(count.longValue()).isEqualTo(1L);
    }

    @Test
    void nameが10文字未満だと保存できない() {
        User shortName = new User("山田太郎", "yamada@example.com", Role.SALES, "hash");

        // @Size(min=10) がflush（INSERT直前）のタイミングで検証される
        assertThatThrownBy(() -> userRepository.saveAndFlush(shortName))
                .isInstanceOf(ConstraintViolationException.class);
    }
}
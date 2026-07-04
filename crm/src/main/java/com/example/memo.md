Service、Entity、Repositoryによる責務の分離

```java
// 【Config層】道具を1個だけ用意（前回の①がここ）
@Configuration
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // ←strength省略=10が標準
    }
}
```
```java
// 【Entity層】入れ物を持つだけ。暗号化処理は一切なし
@Entity
public class User {
    @Id private Long id;
    private String passwordHash;  // 入れるだけ
    // ...
}
```
```java
// 【Service層】ここで実際にencode()して保存する
@Service
public class UserService {
    private final PasswordEncoder passwordEncoder; // Configで作った道具
    private final UserRepository userRepository;

    public void register(String name, String rawPassword) {
        User user = new User();
        user.setName(name);
        user.setPasswordHash(passwordEncoder.encode(rawPassword)); // ←ここ
        userRepository.save(user); // Repository経由でDBへ
    }
}
```

2026/07/04/17:12
バグ修正しながら責務の分離をしていたが、Service層のコンストラクタで手こずっている。
```UserService.java
public void register(String name, String email, Role role, String rawPassword) {
        User user = new User(name, email, role);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        userRepository.save(user);
    }
```

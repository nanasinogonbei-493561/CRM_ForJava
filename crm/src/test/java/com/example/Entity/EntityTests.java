package com.example.Entity;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class EntityTests {
    @Test
    void whenCreatingEntity_ThenCreatedOnIsSet() {
        session = sessionFactory.openSession();
        session.beginTransaction();
        Users users = new Users();

        session.save(users);
        session.getTransaction()
          .commit();
        session.close();

        assertNotNull(users.getCreatedOn());
    }
}

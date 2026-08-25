package com.example.crm.Api;

import com.example.crm.Service.UserService;
import com.example.crm.Web.DTO.UserRequest;
import com.example.crm.Web.DTO.UserResponse;
import com.example.crm.Web.DTO.UserUpdateRequest;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.RestController;

import com.example.crm.Repository.UserRepository;

import java.util.List;
import com.example.crm.Entity.UserEntity;
import com.example.crm.Exception.UserEntityNotFoundException;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;
import org.springframework.web.bind.annotation.PutMapping;



@RestController
public class UserApi {
    private final UserService userService;
    private final UserRepository repository;

    UserApi(UserRepository repository, UserService userService) {
        this.repository = repository;
        this.userService = userService;
    }

    @GetMapping("/users")
    List<UserResponse> all() {
        return repository.findAll().stream()
            .map(UserResponse::from) // ここで paswordHashが落ちる
            .toList();
    }

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    UserResponse newUser(@Valid @RequestBody UserRequest req) {
        UserEntity saved = userService.register(
            req.username(), req.email(), req.role(), req.rawPassword());
        return UserResponse.from(saved);
    }
    
    @GetMapping("/users/{id}")
    EntityModel<UserResponse> one(@PathVariable Long id) {

        UserEntity userEntity = repository.findById(id)
            .orElseThrow(() -> new UserEntityNotFoundException(id));
        
        return EntityModel.of(UserResponse.from(userEntity),
            linkTo(methodOn(UserApi.class).one(id)).withSelfRel(),
            linkTo(methodOn(UserApi.class).all()).withRel("users")
        );
    }

    @PutMapping("/users/{id}")
    UserResponse replaceUserResponse(@Valid @RequestBody UserUpdateRequest newUserUpdateRequest, @PathVariable Long id) {

        return repository.findById(id)
            .map(user -> {
                user.setUsername(newUserUpdateRequest.username());
                user.setEmail(newUserUpdateRequest.email());
                return UserResponse.from(repository.save(user));
            })
            .orElseThrow(() -> 
                new UserEntityNotFoundException(id)
            );
    }

    @DeleteMapping("/users/{id}")
    ResponseEntity<Void> deleteUserEntity(@PathVariable Long id) {
        if (!repository.existsById(id)) throw new UserEntityNotFoundException(id);
        repository.deleteById(id);
        return ResponseEntity.noContent().build(); // 204 が REST の作法。
    }
}

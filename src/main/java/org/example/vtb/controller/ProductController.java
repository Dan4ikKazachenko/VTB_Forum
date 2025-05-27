package org.example.vtb.controller;

import lombok.RequiredArgsConstructor;
import org.example.vtb.dto.ProductRequest;
import org.example.vtb.entity.Message;
import org.example.vtb.entity.Product;
import org.example.vtb.entity.User;
import org.example.vtb.repository.CategoryRepository;
import org.example.vtb.repository.MessageRepository;
import org.example.vtb.repository.ProductRepository;
import org.example.vtb.security.UserDetailsImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final MessageRepository messageRepository;

    @GetMapping
    public ResponseEntity<List<Product>> getAll() {
        return ResponseEntity.ok(productRepository.findAll());
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<Product>> getByCategory(@PathVariable UUID categoryId) {
        return ResponseEntity.ok(productRepository.findByCategoryId(categoryId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(
                productRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("Продукт не найден"))
        );
    }
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}/users")
    public ResponseEntity<List<User>> getUsersWhoMessaged(
            @PathVariable UUID id
    ) {
        List<User> users = messageRepository.findDistinctUsersByProduct(id);
        return ResponseEntity.ok(users);
    }



    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Product> createProduct(@RequestBody ProductRequest request,
                                                 @AuthenticationPrincipal UserDetailsImpl userDetails) {
        var category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Категория не найдена"));

        var product = Product.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .category(category)
                .createdBy(userDetails.getUser())
                .build();

        return ResponseEntity.ok(productRepository.save(product));
    }
}

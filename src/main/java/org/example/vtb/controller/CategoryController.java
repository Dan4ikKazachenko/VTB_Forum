package org.example.vtb.controller;

import lombok.RequiredArgsConstructor;
import org.example.vtb.entity.Category;
import org.example.vtb.repository.CategoryRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID; 

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryRepository categoryRepository;

    @GetMapping
    public ResponseEntity<List<Category>> getAllCategories() {
        return ResponseEntity.ok(categoryRepository.findAll());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')") 
    public ResponseEntity<Category> createCategory(@RequestBody Category category) {
        return ResponseEntity.ok(categoryRepository.save(category));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')") 
    public ResponseEntity<Void> deleteCategory(@PathVariable("id") UUID id) {
        categoryRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

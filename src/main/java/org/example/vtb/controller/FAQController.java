package org.example.vtb.controller;

import lombok.RequiredArgsConstructor;
import org.example.vtb.dto.FAQDto;
import org.example.vtb.service.FAQService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/faq")
@RequiredArgsConstructor
public class FAQController {

    private final FAQService faqService;

    @GetMapping
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<FAQDto>> getAllFAQs() {
        return ResponseEntity.ok(faqService.getAllFAQs());
    }

    @GetMapping("admin/{id}")
    public ResponseEntity<FAQDto> getFAQById(@PathVariable UUID id) {
        return ResponseEntity.ok(faqService.getFAQById(id));
    }

    @PostMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FAQDto> createFAQ(@RequestBody FAQDto faqDto) {
        return ResponseEntity.ok(faqService.createFAQ(faqDto));
    }

    @PutMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FAQDto> updateFAQ(@PathVariable UUID id, @RequestBody FAQDto faqDto) {
        return ResponseEntity.ok(faqService.updateFAQ(id, faqDto));
    }

    @DeleteMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteFAQ(@PathVariable UUID id) {
        faqService.deleteFAQ(id);
        return ResponseEntity.ok().build();
    }
}
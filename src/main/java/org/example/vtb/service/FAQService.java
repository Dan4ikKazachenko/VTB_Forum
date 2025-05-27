package org.example.vtb.service;

import lombok.RequiredArgsConstructor;
import org.example.vtb.dto.FAQDto;
import org.example.vtb.entity.FAQ;
import org.example.vtb.repository.FAQRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FAQService {

    private final FAQRepository faqRepository;

    @Transactional(readOnly = true)
    public List<FAQDto> getAllFAQs() {
        return faqRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public FAQDto getFAQById(UUID id) {
        FAQ faq = faqRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("FAQ not found with id: " + id));
        return mapToDto(faq);
    }

    @Transactional
    public FAQDto createFAQ(FAQDto faqDto) {
        FAQ faq = mapToEntity(faqDto);
        FAQ savedFAQ = faqRepository.save(faq);
        return mapToDto(savedFAQ);
    }

    @Transactional
    public FAQDto updateFAQ(UUID id, FAQDto faqDto) {
        FAQ existingFAQ = faqRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("FAQ not found with id: " + id));
        
        existingFAQ.setTitle(faqDto.getTitle());
        existingFAQ.setDescription(faqDto.getDescription());
        
        FAQ updatedFAQ = faqRepository.save(existingFAQ);
        return mapToDto(updatedFAQ);
    }

    @Transactional
    public void deleteFAQ(UUID id) {
        if (!faqRepository.existsById(id)) {
            throw new RuntimeException("FAQ not found with id: " + id);
        }
        faqRepository.deleteById(id);
    }

    private FAQDto mapToDto(FAQ faq) {
        return FAQDto.builder()
                .id(faq.getId())
                .title(faq.getTitle())
                .description(faq.getDescription())
                .build();
    }

    private FAQ mapToEntity(FAQDto faqDto) {
        return FAQ.builder()
                .id(faqDto.getId())
                .title(faqDto.getTitle())
                .description(faqDto.getDescription())
                .build();
    }
} 
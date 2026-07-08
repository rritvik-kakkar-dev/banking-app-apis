package com.banking.banking_app_apis.bootstrap;

import com.banking.banking_app_apis.budget.entity.Category;
import com.banking.banking_app_apis.budget.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataInitializer implements ApplicationRunner {

    @Autowired
    private CategoryRepository categoryRepository;

    private static final List<String[]> DEFAULT_CATEGORIES = List.of(
            new String[]{"Food",          "🍔", "#FF6B6B"},
            new String[]{"Rent",          "🏠", "#4ECDC4"},
            new String[]{"Transport",     "🚗", "#45B7D1"},
            new String[]{"Entertainment", "🎬", "#96CEB4"},
            new String[]{"Healthcare",    "💊", "#FFEAA7"},
            new String[]{"Utilities",     "💡", "#DDA0DD"},
            new String[]{"Shopping",      "🛍️", "#98D8C8"},
            new String[]{"Education",     "📚", "#F7DC6F"},
            new String[]{"Travel",        "✈️", "#BB8FCE"},
            new String[]{"Other",         "📦", "#AEB6BF"}
    );

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (categoryRepository.findByIsGlobalTrue().isEmpty()) {
            DEFAULT_CATEGORIES.stream()
                    .map(c -> Category.builder()
                            .name(c[0])
                            .icon(c[1])
                            .color(c[2])
                            .isGlobal(true)
                            .createdBy(null)
                            .build())
                    .forEach(categoryRepository::save);

            System.out.println("Default categories seeded successfully.");
        }
    }
}

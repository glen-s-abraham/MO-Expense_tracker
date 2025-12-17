package com.mushroom.expense.controller;

import com.mushroom.expense.entity.SubCategory;
import com.mushroom.expense.service.CategoryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class ApiRestController {

    private final CategoryService categoryService;

    public ApiRestController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/api/subcategories")
    public String getSubCategories(@RequestParam("category") Long categoryId, Model model) {
        List<SubCategory> subCategories = categoryService.findSubCategoriesByCategoryId(categoryId);
        model.addAttribute("subCategories", subCategories);
        return "fragments/subcategory_options :: options";
    }
}

package com.mushroom.expense.controller;

import com.mushroom.expense.entity.Category;
import com.mushroom.expense.entity.SubCategory;
import com.mushroom.expense.entity.User;
import com.mushroom.expense.service.CategoryService;
import com.mushroom.expense.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final CategoryService categoryService;

    public AdminController(UserService userService, CategoryService categoryService) {
        this.userService = userService;
        this.categoryService = categoryService;
    }

    // --- User Management ---

    @GetMapping("/users")
    public String listUsers(Model model) {
        model.addAttribute("users", userService.findAllUsers());
        return "admin/users";
    }

    @GetMapping("/users/new")
    public String newUserForm(Model model) {
        model.addAttribute("user", new User());
        return "admin/user_form";
    }

    @PostMapping("/users")
    public String saveUser(@ModelAttribute User user) {
        userService.saveUser(user);
        return "redirect:/admin/users";
    }

    @GetMapping("/users/edit/{id}")
    public String editUserForm(@PathVariable Long id, Model model) {
        model.addAttribute("user", userService.findById(id).orElseThrow());
        return "admin/user_form";
    }

    @GetMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return "redirect:/admin/users";
    }

    // --- Category Management ---

    @GetMapping("/categories")
    public String listCategories(Model model) {
        model.addAttribute("categories", categoryService.findAllCategories());
        return "admin/categories";
    }

    @GetMapping("/categories/new")
    public String newCategoryForm(Model model) {
        model.addAttribute("category", new Category());
        return "admin/category_form";
    }

    @PostMapping("/categories")
    public String saveCategory(@ModelAttribute Category category) {
        categoryService.saveCategory(category);
        return "redirect:/admin/categories";
    }

    @GetMapping("/categories/edit/{id}")
    public String editCategoryForm(@PathVariable Long id, Model model) {
        model.addAttribute("category", categoryService.findCategoryById(id).orElseThrow());
        return "admin/category_form";
    }

    @GetMapping("/categories/delete/{id}")
    public String deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return "redirect:/admin/categories";
    }

    // --- SubCategory Management ---

    @GetMapping("/subcategories")
    public String listSubCategories(@RequestParam(required = false) Long categoryId, Model model) {
        if (categoryId != null) {
            model.addAttribute("subCategories", categoryService.findSubCategoriesByCategoryId(categoryId));
            model.addAttribute("selectedCategoryId", categoryId);
        } else {
            // Show all or empty? Let's show all if possible, but list might be huge.
            // For now, let's just show empty or require selection.
            // Or better, fetch all categories to filter.
        }
        model.addAttribute("categories", categoryService.findAllCategories());
        return "admin/subcategories";
    }

    @GetMapping("/subcategories/new")
    public String newSubCategoryForm(Model model) {
        model.addAttribute("subCategory", new SubCategory());
        model.addAttribute("categories", categoryService.findAllCategories());
        return "admin/subcategory_form";
    }

    @PostMapping("/subcategories")
    public String saveSubCategory(@ModelAttribute SubCategory subCategory) {
        categoryService.saveSubCategory(subCategory);
        return "redirect:/admin/subcategories?categoryId=" + subCategory.getCategory().getId();
    }

    @GetMapping("/subcategories/edit/{id}")
    public String editSubCategoryForm(@PathVariable Long id, Model model) {
        SubCategory subCategory = categoryService.findSubCategoryById(id).orElseThrow();
        model.addAttribute("subCategory", subCategory);
        model.addAttribute("categories", categoryService.findAllCategories());
        return "admin/subcategory_form";
    }

    @GetMapping("/subcategories/delete/{id}")
    public String deleteSubCategory(@PathVariable Long id) {
        SubCategory subCategory = categoryService.findSubCategoryById(id).orElseThrow();
        Long categoryId = subCategory.getCategory().getId();
        categoryService.deleteSubCategory(id);
        return "redirect:/admin/subcategories?categoryId=" + categoryId;
    }
}

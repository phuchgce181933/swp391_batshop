package com.ra.batshop.controller;

import com.ra.batshop.model.ContactSupport;
import com.ra.batshop.repository.ContactRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class ContactController {

    // Đã cập nhật tên Repository tại đây
    @Autowired
    private ContactRepository contactRepository;

    @GetMapping("/contact")
    public String contactPage(Model model) {
        model.addAttribute("contactSupport", new ContactSupport());
        return "user/contact";
    }

    @PostMapping("/contact")
    public String submitContact(@ModelAttribute ContactSupport contactSupport) {
        // Gọi hàm save bằng tên biến mới
        contactRepository.save(contactSupport);

        return "redirect:/contact?success=true";
    }
}
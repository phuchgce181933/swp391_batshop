package com.ra.batshop.controller;

import com.ra.batshop.model.ContactSupport;
import com.ra.batshop.model.Enum.ContactStatus;
import com.ra.batshop.repository.ContactRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
public class ContactController {

    @Autowired
    private ContactRepository contactRepository;

    // ==========================================
    // KHU VỰC DÀNH CHO NGƯỜI DÙNG (USER)
    // ==========================================

    @GetMapping("/contact")
    public String contactPage(Model model) {
        model.addAttribute("contactSupport", new ContactSupport());
        return "user/contact";
    }

    @PostMapping("/contact")
    public String submitContact(@Valid @ModelAttribute ContactSupport contactSupport,
                                BindingResult bindingResult,
                                @RequestParam(value = "file", required = false) MultipartFile file,
                                Model model) {
        if (bindingResult.hasErrors()) {
            return "user/contact";
        }

        // Mặc định gán trạng thái Chưa đọc khi khách hàng vừa gửi form
        contactSupport.setStatus(ContactStatus.UNREAD);

        // (Khu vực dự trữ): Logic xử lý lưu file vật lý sẽ được code ở đây

        contactRepository.save(contactSupport);
        return "redirect:/contact?success=true";
    }


    // ==========================================
    // KHU VỰC DÀNH CHO QUẢN TRỊ VIÊN (ADMIN)
    // ==========================================

    // 1. Hiển thị danh sách liên hệ (Sử dụng chung Layout Admin)
    @GetMapping("/admin/contacts")
    public String listContacts(Model model) {
        // Lấy toàn bộ danh sách liên hệ từ Database
        List<ContactSupport> contacts = contactRepository.findAll();

        model.addAttribute("contacts", contacts);

        // Truyền đường dẫn file HTML mảnh ghép của list contact
        model.addAttribute("content", "admin/contact/list");

        // Trả về layout tổng
        return "admin/layout";
    }

    // 2. Cập nhật trạng thái nhanh từ thẻ <select> ngoài danh sách
    @PostMapping("/admin/contacts/update-status/{id}")
    public String updateStatus(@PathVariable Integer id, @RequestParam("status") ContactStatus status) {
        ContactSupport contact = contactRepository.findById(id).orElse(null);
        if (contact != null) {
            contact.setStatus(status);
            contactRepository.save(contact);
        }
        return "redirect:/admin/contacts";
    }

    // 3. Xem chi tiết một liên hệ cụ thể (Sử dụng chung Layout Admin)
    @GetMapping("/admin/contacts/detail/{id}")
    public String contactDetail(@PathVariable Integer id, Model model) {
        ContactSupport contact = contactRepository.findById(id).orElse(null);
        if (contact == null) {
            return "redirect:/admin/contacts"; // Nếu id không tồn tại thì quay lại danh sách
        }
        model.addAttribute("contact", contact);

        // Truyền đường dẫn file HTML mảnh ghép của detail contact
        model.addAttribute("content", "admin/contact/detail");

        // Trả về layout tổng
        return "admin/layout";
    }

    // 4. Lưu ghi chú của Admin và Gửi Email phản hồi
    @PostMapping("/admin/contacts/reply/{id}")
    public String replyContact(@PathVariable Integer id,
                               @RequestParam("adminNote") String adminNote,
                               @RequestParam(value = "emailReply", required = false) String emailReply) {
        ContactSupport contact = contactRepository.findById(id).orElse(null);
        if (contact != null) {
            // Lưu lại ghi chú nội bộ của nhân viên
            contact.setAdminNote(adminNote);

            // Nếu admin có nhập nội dung vào ô Email
            if (emailReply != null && !emailReply.trim().isEmpty()) {

                // TODO: Gọi hàm JavaMailSender để gửi Email thực tế tại đây
                // emailService.sendEmail(contact.getEmail(), "Phản hồi hỗ trợ từ BatShop", emailReply);

                // Sau khi phản hồi email xong, tự động đánh dấu là Đã giải quyết
                contact.setStatus(ContactStatus.RESOLVED);
            } else {
                // Nếu chỉ lưu ghi chú mà không gửi email, tự động chuyển thành Đang xử lý
                contact.setStatus(ContactStatus.PROCESSING);
            }

            contactRepository.save(contact);
        }
        return "redirect:/admin/contacts/detail/" + id + "?success=true";
    }
}
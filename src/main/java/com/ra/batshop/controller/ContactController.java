package com.ra.batshop.controller;

import com.ra.batshop.model.ContactSupport;
import com.ra.batshop.model.Enum.ContactStatus;
import com.ra.batshop.repository.ContactRepository;
import com.ra.batshop.service.EmailService; // Import EmailService của bạn
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import java.util.List;

@Controller
public class ContactController {

    @Autowired
    private ContactRepository contactRepository;

    // Tiêm (Inject) EmailService vào Controller
    @Autowired
    private EmailService emailService;

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

        // ========================================================
        // LOGIC XỬ LÝ UPLOAD FILE
        // ========================================================
        if (file != null && !file.isEmpty()) {
            try {
                // 1. Lấy tên file gốc
                String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());

                // 2. Tạo tên file duy nhất (Dùng UUID để tránh trùng tên file khi 2 người tải lên cùng tên ảnh)
                String uniqueFilename = UUID.randomUUID().toString() + "_" + originalFilename;

                // 3. Đường dẫn lưu thư mục "uploads" ở thư mục gốc của project
                Path uploadPath = Paths.get("uploads/");

                // 4. Nếu thư mục chưa tồn tại thì tự động tạo mới
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                // 5. Lưu file vào ổ cứng
                Path filePath = uploadPath.resolve(uniqueFilename);
                Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                // 6. Lưu đường dẫn vào Database (Đường dẫn này khớp với WebConfig của bạn)
                contactSupport.setAttachmentUrl("/uploads/" + uniqueFilename);

            } catch (IOException e) {
                e.printStackTrace();
                // Nếu lưu file thất bại, bạn có thể báo lỗi hoặc vẫn cho phép gửi form nhưng không có file
                System.out.println("Lỗi lưu file đính kèm: " + e.getMessage());
            }
        }

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

                // GỌI HÀM GỬI EMAIL THỰC TẾ
                String subject = "BatShop - Phản hồi yêu cầu hỗ trợ #" + contact.getId();
                emailService.sendEmail(contact.getEmail(), subject, emailReply);

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
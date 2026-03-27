package com.ra.batshop.controller;

import com.ra.batshop.model.ContactSupport;
import com.ra.batshop.model.Enum.ContactStatus;
import com.ra.batshop.repository.ContactRepository;
import com.ra.batshop.service.EmailService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
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
import java.time.LocalDate;
import java.util.UUID;

import java.util.List;

@Controller
public class ContactController {

    @ModelAttribute
    public void addActiveMenu(Model model) {
        model.addAttribute("activeMenu", "contactsp");
    }

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
        return "user/contact/contact";
    }

    @PostMapping("/contact")
    public String submitContact(@Valid @ModelAttribute ContactSupport contactSupport,
                                BindingResult bindingResult,
                                @RequestParam(value = "file", required = false) MultipartFile file,
                                Model model) {
        if (bindingResult.hasErrors()) {
            return "user/contact/contact";
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
// 1. Hiển thị danh sách liên hệ (Kèm Tìm kiếm, Lọc, Phân trang & Đếm tin chưa đọc)
    @GetMapping("/admin/contacts")
    public String listContacts(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "topic", required = false) String topic,
            @RequestParam(value = "status", required = false) ContactStatus status,
            @RequestParam(value = "date", required = false) LocalDate date,
            @RequestParam(value = "page", defaultValue = "0") int page, // Trang hiện tại (mặc định 0)
            Model model, HttpSession session) {

        if (session.getAttribute("user") == null) {
            return "redirect:/login";
        }

        // Tạo Pageable: Mỗi trang 10 phần tử, sắp xếp theo createdAt giảm dần (Mới nhất lên đầu)
        int pageSize = 10;
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("createdAt").descending());

        // Lấy danh sách có phân trang
        Page<ContactSupport> contactPage = contactRepository.searchAndFilter(keyword, topic, status, date, pageable);

        // Đếm số lượng thư Chưa đọc
        long unreadCount = contactRepository.countByStatus(ContactStatus.UNREAD);

        // Truyền dữ liệu sang View
        model.addAttribute("contactPage", contactPage);
        model.addAttribute("contacts", contactPage.getContent()); // Danh sách để lặp trong <table>
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", contactPage.getTotalPages());
        model.addAttribute("unreadCount", unreadCount);

        model.addAttribute("content", "admin/contact/list");

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

    @PostMapping("/admin/contacts/process/{id}")
    public String processContact(@PathVariable Integer id,
                                 @RequestParam(value = "adminNote", required = false) String adminNote,
                                 @RequestParam(value = "emailReply", required = false) String emailReply,
                                 @RequestParam("action") String action) {
        ContactSupport contact = contactRepository.findById(id).orElse(null);
        if (contact != null) {

            switch (action) {
                case "saveNote":
                    if (adminNote == null || adminNote.trim().isEmpty()) {
                        return "redirect:/admin/contacts/detail/" + id + "?error=emptyNote";
                    }
                    contact.setAdminNote(adminNote);
                    if (contact.getStatus() == ContactStatus.UNREAD) {
                        contact.setStatus(ContactStatus.PROCESSING);
                    }
                    contactRepository.save(contact);
                    return "redirect:/admin/contacts/detail/" + id + "?success=noteSaved";

                case "reject": // NÚT BỎ QUA (Chỉ đổi trạng thái, không gửi mail)
                    if (adminNote != null && !adminNote.trim().isEmpty()) {
                        contact.setAdminNote(adminNote);
                    }
                    contact.setStatus(ContactStatus.REJECTED);
                    contactRepository.save(contact);
                    return "redirect:/admin/contacts/detail/" + id + "?success=rejected";

                case "sendEmail": // NÚT GỬI EMAIL
                    if (adminNote != null && !adminNote.trim().isEmpty()) {
                        contact.setAdminNote(adminNote);
                    }
                    if (emailReply == null || emailReply.trim().isEmpty()) {
                        return "redirect:/admin/contacts/detail/" + id + "?error=emptyEmail";
                    }

                    String subject = "BatShop - Phản hồi yêu cầu hỗ trợ #" + contact.getId();
                    emailService.sendEmail(contact.getEmail(), subject, emailReply);

                    // LƯU LỊCH SỬ EMAIL
                    if (contact.getReplyHistory() == null) {
                        contact.setReplyHistory(new java.util.ArrayList<>());
                    }
                    com.ra.batshop.model.ContactReply reply = new com.ra.batshop.model.ContactReply();
                    reply.setMessage(emailReply);
                    reply.setContactSupport(contact);
                    contact.getReplyHistory().add(reply);

                    contact.setStatus(ContactStatus.RESOLVED);
                    contactRepository.save(contact);
                    return "redirect:/admin/contacts/detail/" + id + "?success=emailSent";
            }
        }
        return "redirect:/admin/contacts";
    }
}
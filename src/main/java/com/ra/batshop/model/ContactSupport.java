package com.ra.batshop.model;

import com.ra.batshop.model.Enum.ContactStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
public class ContactSupport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "Vui lòng nhập họ và tên")
    private String name;

    @Pattern(regexp = "^(0|\\+84)(\\s|\\.)?((3[2-9])|(5[689])|(7[06-9])|(8[1-689])|(9[0-46-9]))(\\d)(\\s|\\.)?(\\d{3})(\\s|\\.)?(\\d{3})$", message = "Số điện thoại không hợp lệ")
    private String phone;

    @NotBlank(message = "Vui lòng nhập email")
    @Email(message = "Email không đúng định dạng")
    private String email;

    @NotBlank(message = "Vui lòng chọn chủ đề liên hệ")
    private String topic; // Thêm trường chủ đề

    @NotBlank(message = "Vui lòng nhập nội dung")
    @Column(columnDefinition = "TEXT")
    private String message;

    private String attachmentUrl; // Thêm trường lưu đường dẫn ảnh/file

    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(50)") // Ép kiểu dữ liệu trong DB thành chuỗi linh hoạt
    private ContactStatus status = ContactStatus.UNREAD; // Mặc định khi khách gửi là Chưa đọc

    @Column(columnDefinition = "TEXT")
    private String adminNote; // Ghi chú nội bộ dành cho Admin

    @OneToMany(mappedBy = "contactSupport", cascade = CascadeType.ALL)
    @OrderBy("createdAt DESC") // Sắp xếp email mới gửi lên đầu
    private List<ContactReply> replyHistory;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
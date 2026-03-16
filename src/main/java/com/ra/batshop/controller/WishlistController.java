package com.ra.batshop.controller;

import com.ra.batshop.model.Category;
import com.ra.batshop.model.Product;
import com.ra.batshop.model.User;
import com.ra.batshop.model.Wishlist;
import com.ra.batshop.repository.CategoryRepository;
import com.ra.batshop.repository.ProductRepository;
import com.ra.batshop.repository.WishlistRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.stream.Collectors;

@Controller // Đánh dấu đây là lớp điều hướng (Controller) xử lý các yêu cầu từ trình duyệt
@RequestMapping("/wishlist") // Tất cả các đường dẫn trong class này đều bắt đầu bằng /wishlist
public class WishlistController {

    @Autowired // Tự động kết nối với Repository để thao tác database Wishlist
    private WishlistRepository wishlistRepository;

    @Autowired // Kết nối với Repository để lấy thông tin sản phẩm
    private ProductRepository productRepository;

    @Autowired // Kết nối với Repository để lấy danh mục sản phẩm (Category)
    private CategoryRepository categoryRepository;

    // --- HÀM THÊM SẢN PHẨM VÀO DANH SÁCH YÊU THÍCH ---
    @GetMapping("/add/{productId}") // Nhận ID sản phẩm từ đường dẫn (ví dụ: /wishlist/add/5)
    public String addToWishlist(@PathVariable Integer productId, HttpSession session) {
        // 1. Kiểm tra xem người dùng đã đăng nhập chưa từ Session
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login"; // Nếu chưa đăng nhập, đá về trang Login

        // 2. Tìm sản phẩm trong DB theo ID truyền vào
        Product product = productRepository.findById(productId).orElse(null);

        if (product != null) {
            // 3. Kiểm tra xem sản phẩm này đã có trong danh sách yêu thích của User này chưa (tránh trùng lặp)
            if (wishlistRepository.findByUserAndProduct(user, product).isEmpty()) {
                // 4. Nếu chưa có, tạo mới một đối tượng Wishlist và gán User + Product
                Wishlist wish = new Wishlist();
                wish.setUser(user);
                wish.setProduct(product);
                // 5. Lưu vào Database
                wishlistRepository.save(wish);
            }
        }
        // Thêm xong thì chuyển hướng về trang hiển thị danh sách yêu thích
        return "redirect:/wishlist/list";
    }

    // --- HÀM HIỂN THỊ DANH SÁCH YÊU THÍCH ---
    @GetMapping("/list")
    public String showWishlist(HttpSession session,
                               Model model,
                               @RequestParam(name = "categoryId", required = false) Integer categoryId) {
        // 1. Kiểm tra đăng nhập
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        // 2. Lấy tất cả các sản phẩm yêu thích của người dùng này từ DB
        List<Wishlist> items = wishlistRepository.findByUser(user);

        // 3. LOGIC LỌC (FILTER): Nếu người dùng chọn một danh mục cụ thể trên giao diện
        if (categoryId != null) {
            items = items.stream()
                    .filter(item -> item.getProduct().getCategory() != null &&
                            item.getProduct().getCategory().getId().equals(categoryId))
                    .collect(Collectors.toList()); // Chỉ giữ lại các sản phẩm thuộc danh mục đã chọn
        }

        // 4. Gửi dữ liệu ra file HTML (View)
        model.addAttribute("wishlistItems", items); // Gửi danh sách đã lọc (nếu có)
        model.addAttribute("categories", categoryRepository.findAll()); // Gửi tất cả danh mục để hiện ở Dropdown lọc
        model.addAttribute("selectedCategory", categoryId); // Gửi lại ID đã chọn để đánh dấu trên giao diện

        return "profile/wishlist"; // Trả về trang giao diện wishlist.html
    }

    // --- HÀM XÓA SẢN PHẨM KHỎI DANH SÁCH YÊU THÍCH ---
    @GetMapping("/delete/{id}") // Nhận ID của dòng Wishlist cần xóa
    public String deleteFromWishlist(@PathVariable("id") Long id) {
        // Gọi lệnh xóa trực tiếp trong DB theo ID
        wishlistRepository.deleteById(id);
        // Xóa xong quay lại trang danh sách để cập nhật giao diện
        return "redirect:/wishlist/list";
    }
}
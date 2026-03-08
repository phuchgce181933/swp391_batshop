package com.ra.batshop.controller;

import com.ra.batshop.model.Comment;
import com.ra.batshop.model.Product;
import com.ra.batshop.repository.CommentRepository;
import com.ra.batshop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
@RequestMapping("/product")
public class CommentController {

    private final CommentRepository commentRepository;
    private final ProductRepository productRepository;


    // thêm comment
    @PostMapping("/comment")
    public String addComment(

            @RequestParam Integer productId,
            @RequestParam String name,
            @RequestParam String message
    ){

        Product product =
                productRepository
                        .findById(productId)
                        .orElseThrow();

        Comment comment = new Comment();

        comment.setName(name);
        comment.setMessage(message);
        comment.setProduct(product);
        comment.setCreatedAt(LocalDateTime.now());

        commentRepository.save(comment);

        return "redirect:/product/detail/" + productId;
    }



    // reply comment
    @PostMapping("/comment/reply")
    public String replyComment(

            @RequestParam Integer productId,
            @RequestParam Integer parentId,
            @RequestParam String name,
            @RequestParam String message

    ){

        Product product = productRepository.findById(productId).orElseThrow();

        Comment parent = commentRepository.findById(parentId).orElseThrow();

        Comment reply = new Comment();

        reply.setName(name);
        reply.setMessage(message);
        reply.setProduct(product);
        reply.setParent(parent);
        reply.setCreatedAt(LocalDateTime.now());

        commentRepository.save(reply);

        return "redirect:/product/detail/" + productId;
    }

}
package dev.overwave.whereeat.controller;

import dev.overwave.whereeat.core.post.Post;
import dev.overwave.whereeat.core.post.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class PostController {

    private final PostRepository postRepository;

    @GetMapping("/posts/")
    public Page<Post> getPosts(@PageableDefault Pageable pageable) {
        return postRepository.findAll(pageable);
    }
}
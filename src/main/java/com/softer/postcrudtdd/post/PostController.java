package com.softer.postcrudtdd.post;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostRepository postRepository;

    public PostController(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    @GetMapping
    List<Post> findAll() {
        return postRepository.findAll();
    }

    @GetMapping("/{id}")
    Optional<Post> findById(@PathVariable Integer id) {
        return postRepository.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    Post create(@RequestBody @Valid Post post) {
        return postRepository.save(post);
    }

    @PutMapping("/{id}")
    Post update(@PathVariable Integer id, @RequestBody Post post) {
        Optional<Post> existing = postRepository.findById(id);
        if (existing.isPresent()) {
            Post updated = new Post(
                    existing.get().id(),
                    existing.get().userId(),
                    post.title(),
                    post.body(),
                    existing.get().version()
            );
            return postRepository.save(updated);
        } else {
            throw new PostNotFoundException();
        }
    }
}

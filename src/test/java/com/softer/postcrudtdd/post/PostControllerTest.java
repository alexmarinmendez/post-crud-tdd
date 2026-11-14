package com.softer.postcrudtdd.post;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PostController.class)
@AutoConfigureMockMvc
public class PostControllerTest {

    @Autowired
    MockMvc mockMvc;
    @MockitoBean
    PostRepository postRepository;

    List<Post> posts = new ArrayList<>();

    @BeforeEach
    void setUp() {
        posts = List.of(
                new Post(1, 1, "Post Title 1", "Post Body 1", null),
                new Post(2, 1, "Post Title 2", "Post Body 2", null)
        );
    }

    // --------------------------- Unit Tests ------------------------------
    @Test
    void shouldFindAllPosts() throws Exception {
        String jsonResponse = """
                [
                { "id": 1, "userId": 1, "title": "Post Title 1", "body": "Post Body 1", "version": null },
                { "id": 2, "userId": 1, "title": "Post Title 2", "body": "Post Body 2", "version": null }
                ]
                """;
        when(postRepository.findAll()).thenReturn(posts);
        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isOk())
                .andExpect(content().json(jsonResponse));
    }

    @Test
    void shouldFindPostWhenGivenValidID() throws Exception {
        var post = posts.getFirst();
        var jsonResponse = """
                { "id": %d, "userId": %d, "title": "%s", "body": "%s", "version": null }
                """.formatted(post.id(), post.userId(), post.title(), post.body());
        when(postRepository.findById(1)).thenReturn(Optional.of(posts.getFirst()));
        mockMvc.perform(get("/api/posts/1"))
                .andExpect(status().isOk())
                .andExpect(content().json(jsonResponse));
    }

    @Test
    void shouldNotFindPostWhenGivenInvalidID() throws Exception {
        when(postRepository.findById(999)).thenThrow(PostNotFoundException.class);
        mockMvc.perform(get("/api/posts/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateNewPostWhenPostDataIsValid() throws Exception {
        var post = new Post(3, 1, "Post Title 3", "Post Body 3", null);
        when(postRepository.save(post)).thenReturn(post);
        var jsonResponse = """
                { "id": %d, "userId": %d, "title": "%s", "body": "%s", "version": null }
                """.formatted(post.id(), post.userId(), post.title(), post.body());
        mockMvc.perform(post("/api/posts").contentType("application/json").content(jsonResponse))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldNotCreateNewPostWhenPostDataIsInvalid() throws Exception {
        var post = new Post(4, 1, "", "", null);
        when(postRepository.save(post)).thenReturn(post);
        var jsonResponse = """
                { "id": %d, "userId": %d, "title": "%s", "body": "%s", "version": null }
                """.formatted(post.id(), post.userId(), post.title(), post.body());
        mockMvc.perform(post("/api/posts").contentType("application/json").content(jsonResponse))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldUpdatePostWhenGivenValidPostData() throws Exception {
        Post updated = new Post(1, 1, "Updated Title", "Updated Body", 1);
        when(postRepository.findById(1)).thenReturn(Optional.of(updated));
        when(postRepository.save(updated)).thenReturn(updated);
        var jsonResponse = """
                { "id": %d, "userId": %d, "title": "%s", "body": "%s", "version": %d }
                """.formatted(updated.id(), updated.userId(), updated.title(), updated.body(), updated.version());
        mockMvc.perform(put("/api/post/1")
                .contentType("application/json")
                .content(jsonResponse)).andExpect((status().isOk()));
    }
}

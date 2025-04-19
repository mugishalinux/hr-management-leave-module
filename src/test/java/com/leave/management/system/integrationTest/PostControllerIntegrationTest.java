package com.leave.management.system.integrationTest;

import com.leave.management.system.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PostControllerIntegrationTest {
    @LocalServerPort
    private int port;
    @Autowired
    private TestRestTemplate restTemplate;
    private static HttpHeaders headers;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private UserRepository userRepository;

    @BeforeClass
    public static void init() {
        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
    }

    private String createURLWithPort(String path) {
        return "http://localhost:" + port + "/api/post" + path;
    }

    @Test
    public void testGetAllPost() {
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        ResponseEntity<List<Post>> response = restTemplate.exchange(
                createURLWithPort(""), HttpMethod.GET, entity, new ParameterizedTypeReference<>() {
                });
        List<Post> postList = response.getBody();
        assert postList != null;
        assertEquals(response.getStatusCodeValue(), 200);
        assertEquals(postList.size(), postRepository.findAll().size());
    }

    @Test
    public void testDeletePostById() {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(createURLWithPort("/delete"))
                .queryParam("id", "0000763a-92bf-41a3-9a10-c6be3c906b9d");
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
                uriBuilder.toUriString(), HttpMethod.DELETE, entity, String.class);
        String result = response.getBody();
        assertEquals(response.getStatusCodeValue(), 200);
        assertNotNull(result);
        assertEquals(result, "true");
    }

    @Test
    public void testCreatePost() throws JsonProcessingException {
        Post post = new Post("In Test", "creating post using Test method");
        HttpEntity<Post> entity = new HttpEntity<>(post, headers);
        ResponseEntity<PostResponse> response = restTemplate.exchange(
                createURLWithPort(""), HttpMethod.POST, entity, PostResponse.class);

        assertEquals(response.getStatusCodeValue(), 201);
        PostResponse postRes = response.getBody();
        assertNotNull(postRes.getId());
        assertEquals(postRes.getTitle(), "In Test");
    }
}

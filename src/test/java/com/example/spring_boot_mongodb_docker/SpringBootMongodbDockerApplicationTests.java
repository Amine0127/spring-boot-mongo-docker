package com.example.spring_boot_mongodb_docker;

import com.example.spring_boot_mongodb_docker.model.User;
import com.example.spring_boot_mongodb_docker.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SpringBootMongodbDockerApplicationTests {

	@LocalServerPort
	private int port;

	@Autowired
	private TestRestTemplate restTemplate;

	@Autowired
	private UserRepository userRepository;

	@Test
	void contextLoads() {
	}

	@Test
	public void testRegisterUser() {
		// Clear any existing users with the same username
		userRepository.findByUsername("testuser").ifPresent(user -> userRepository.delete(user));

		// Create a new user with username, password, and email
		User user = new User("testuser", "password", "test@example.com");

		HttpHeaders headers = new HttpHeaders();
		headers.set("Content-Type", "application/json");

		HttpEntity<User> request = new HttpEntity<>(user, headers);

		ResponseEntity<String> response = restTemplate.postForEntity(
				"http://localhost:" + port + "/api/auth/register",
				request,
				String.class);

		assertEquals(201, response.getStatusCodeValue());
		assertTrue(response.getBody().contains("token"));
	}
}

package com.purple;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import software.amazon.awssdk.services.s3.S3Client;

@ActiveProfiles("test")
@SpringBootTest(properties = "SPRING_PROFILES_ACTIVE=test")
class PurpleNexusServerApplicationTests {

	@Autowired
	private S3Client s3Client;

	@Test
	void createsS3ClientFromApplicationConfiguration() {
		assertThat(s3Client).isNotNull();
	}
}
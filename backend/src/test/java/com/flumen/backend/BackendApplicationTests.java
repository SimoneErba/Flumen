package com.flumen.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.flumen.backend.services.ClickHouseService;
import com.flumen.backend.services.OrientDBService;

@SpringBootTest
class BackendApplicationTests {

	@MockBean
    private OrientDBService orientDBService; 

	@MockBean
    private ClickHouseService clickhouseService; 

	@Test
	void contextLoads() {
	}

}

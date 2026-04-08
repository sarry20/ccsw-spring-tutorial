package com.ccsw.tutorial.loan;

import com.ccsw.tutorial.client.model.ClientDto;
import com.ccsw.tutorial.common.pagination.PageableRequest;
import com.ccsw.tutorial.config.ResponsePage;
import com.ccsw.tutorial.game.model.GameDto;
import com.ccsw.tutorial.loan.model.LoanDto;
import com.ccsw.tutorial.loan.model.LoanSearchDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class LoanIT {

    public static final String LOCALHOST = "http://localhost:";
    public static final String SERVICE_PATH = "/loan";

    private static final String TITLE_PARAM = "title";
    private static final String CLIENT_PARAM = "client";
    private static final String DATE_PARAM = "date";

    private static final int TOTAL_LOANS = 4;

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    ParameterizedTypeReference<List<LoanDto>> responseTypeList = new ParameterizedTypeReference<>() {
    };

    ParameterizedTypeReference<ResponsePage<LoanDto>> responseTypePage = new ParameterizedTypeReference<>() {
    };

    @Test
    public void findAllShouldReturnAllLoans() {

        ResponseEntity<List<LoanDto>> response = restTemplate.exchange(LOCALHOST + port + SERVICE_PATH, HttpMethod.GET, null, responseTypeList);

        assertNotNull(response);
        assertEquals(TOTAL_LOANS, response.getBody().size());
    }

    @Test
    public void findPageWithDateShouldReturnMatchingLoans() {

        LoanSearchDto searchDto = new LoanSearchDto();
        searchDto.setPageable(new PageableRequest(0, 10));

        Map<String, Object> params = new HashMap<>();
        params.put(TITLE_PARAM, null);
        params.put(CLIENT_PARAM, null);
        params.put(DATE_PARAM, "2024-01-10T12:00:00");

        ResponseEntity<ResponsePage<LoanDto>> response = restTemplate.exchange(getUrlWithParams(), HttpMethod.POST, new HttpEntity<>(searchDto), responseTypePage, params);

        assertNotNull(response);
        assertEquals(TOTAL_LOANS, response.getBody().getTotalElements());
        assertEquals(TOTAL_LOANS, response.getBody().getContent().size());
    }

    @Test
    public void saveWithoutIdShouldCreateNewLoan() {

        LoanDto dto = buildLoanDto(5L, 5L, "2024-02-01T10:00:00Z", "2024-02-10T10:00:00Z");

        restTemplate.exchange(LOCALHOST + port + SERVICE_PATH, HttpMethod.PUT, new HttpEntity<>(dto), Void.class);

        ResponseEntity<List<LoanDto>> response = restTemplate.exchange(LOCALHOST + port + SERVICE_PATH, HttpMethod.GET, null, responseTypeList);

        assertNotNull(response);
        assertEquals(TOTAL_LOANS + 1, response.getBody().size());
    }

    @Test
    public void saveWithOverlappingGameShouldReturnBadRequest() {

        LoanDto dto = buildLoanDto(1L, 5L, "2024-01-10T10:00:00Z", "2024-01-12T10:00:00Z");

        ResponseEntity<Map> response = restTemplate.exchange(LOCALHOST + port + SERVICE_PATH, HttpMethod.PUT, new HttpEntity<>(dto), Map.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Bad Request", response.getBody().get("error"));
    }

    @Test
    public void deleteWithExistsIdShouldDeleteLoan() {

        restTemplate.exchange(LOCALHOST + port + SERVICE_PATH + "/1", HttpMethod.DELETE, null, Void.class);

        ResponseEntity<List<LoanDto>> response = restTemplate.exchange(LOCALHOST + port + SERVICE_PATH, HttpMethod.GET, null, responseTypeList);

        assertNotNull(response);
        assertEquals(TOTAL_LOANS - 1, response.getBody().size());
    }

    private String getUrlWithParams() {
        return UriComponentsBuilder.fromHttpUrl(LOCALHOST + port + SERVICE_PATH)
                .queryParam(TITLE_PARAM, "{" + TITLE_PARAM + "}")
                .queryParam(CLIENT_PARAM, "{" + CLIENT_PARAM + "}")
                .queryParam(DATE_PARAM, "{" + DATE_PARAM + "}")
                .encode()
                .toUriString();
    }

    private LoanDto buildLoanDto(Long gameId, Long clientId, String startDate, String endDate) {

        LoanDto dto = new LoanDto();

        GameDto gameDto = new GameDto();
        gameDto.setId(gameId);

        ClientDto clientDto = new ClientDto();
        clientDto.setId(clientId);

        dto.setGame(gameDto);
        dto.setClient(clientDto);
        dto.setStartDate(java.util.Date.from(Instant.parse(startDate)));
        dto.setEndDate(java.util.Date.from(Instant.parse(endDate)));

        return dto;
    }
}


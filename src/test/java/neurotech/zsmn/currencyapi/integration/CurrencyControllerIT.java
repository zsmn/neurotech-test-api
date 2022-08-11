package neurotech.zsmn.currencyapi.integration;

import lombok.extern.log4j.Log4j2;
import neurotech.zsmn.currencyapi.domain.Currency;
import neurotech.zsmn.currencyapi.repository.CurrencyRepository;
import neurotech.zsmn.currencyapi.util.CurrencyCreator;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@Log4j2
public class CurrencyControllerIT {
    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private CurrencyRepository currencyRepository;

    @LocalServerPort
    private int port;

    @Test
    @DisplayName("latestCurrency return latest Currency when successful")
    void latestCurrency_ReturnsLatestCurrencyAndOkStatus_WhenSuccessful(){
        // Save test to repository
        currencyRepository.save(CurrencyCreator.createCurrencyToSave());

        // Make request using rest template
        Currency expectedCurrency = CurrencyCreator.createValidCurrency();
        ResponseEntity<Currency> lastCurrency = testRestTemplate.exchange("/currency/latest", HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Currency>() {
                });

        // Make assertions
        Assertions.assertThat(lastCurrency.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(lastCurrency.getBody()).isNotNull();
        Assertions.assertThat(lastCurrency.getBody()).isEqualTo(expectedCurrency);
    }

    @Test
    @DisplayName("latestCurrency return null when no latest currency is available")
    void latestCurrency_ReturnsNullAndNoContentStatus_WhenNoLatestCurrencyIsAvailable(){
        // Get response
        ResponseEntity<Currency> lastCurrency = testRestTemplate.exchange("/currency/latest", HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Currency>() {
                });

        // Make assertions
        Assertions.assertThat(lastCurrency.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        Assertions.assertThat(lastCurrency.getBody()).isNull();
    }

    @Test
    @DisplayName("currencyByInterval return list of Currency when successful")
    void currencyByInterval_ReturnsListOfCurrencyAndOk_WhenSuccessful() {
        // Create 3 currencies with different dates
        List<Currency> savedCurrenciesList = new ArrayList<>();
        List<Date> dateList = List.of(Date.valueOf("2022-08-07"), Date.valueOf("2022-08-08"),Date.valueOf("2022-08-09"));
        for(int i = 0; i < 3; i++){
            Currency currency = CurrencyCreator.createCurrencyToSave();
            currency.setDate(dateList.get(i));
            Currency savedCurrency = currencyRepository.save(currency);
            savedCurrenciesList.add(savedCurrency);
        }

        // Make request
        ResponseEntity<List<Currency>> currencyByInterval = testRestTemplate.exchange("/currency/interval?endDate=2022-08-09", HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Currency>>() {
                });

        // Make assertions
        Assertions.assertThat(currencyByInterval.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(currencyByInterval.getBody()).isNotNull();
        Assertions.assertThat(currencyByInterval.getBody())
                .isNotEmpty()
                .hasSize(savedCurrenciesList.size());
        Assertions.assertThat(currencyByInterval.getBody()).isEqualTo(savedCurrenciesList);
    }

    @Test
    @DisplayName("Return empty list when a given date does not contains any Currency instances")
    void currencyByInterval_ReturnsEmptyList_WhenNoCurrenciesOnTheInterval() {
        // Save a Currency outside the requested range
        Currency currency = CurrencyCreator.createCurrencyToSave();
        currency.setDate(Date.valueOf("2022-08-08"));
        currencyRepository.save(currency);

        // Make request
        ResponseEntity<List<Currency>> currencyByInterval = testRestTemplate.exchange("/currency/interval?startDate=2000-10-02&endDate=2000-10-03", HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Currency>>() {
                });

        // Make assertions
        Assertions.assertThat(currencyByInterval.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(currencyByInterval.getBody()).isNotNull().isEmpty();
    }

}

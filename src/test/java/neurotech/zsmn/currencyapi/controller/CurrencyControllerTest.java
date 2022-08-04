package neurotech.zsmn.currencyapi.controller;

import neurotech.zsmn.currencyapi.domain.Currency;
import neurotech.zsmn.currencyapi.service.CurrencyService;
import neurotech.zsmn.currencyapi.util.CurrencyCreator;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ExtendWith(SpringExtension.class)
class CurrencyControllerTest {
    @InjectMocks
    private CurrencyController currencyController;

    @Mock
    public CurrencyService currencyServiceMock;

    @BeforeEach
    void setup() {
        // Setup mock for getLatestCurrency()
        Currency validCurrency = CurrencyCreator.createValidCurrency();
        BDDMockito.when(currencyServiceMock.getLatestCurrencyOrThrowNoContentException())
                .thenReturn(validCurrency);

        // Setup mock for getCurrencyByInterval
        List<Currency> validCurrencyList = new ArrayList<>(List.of(CurrencyCreator.createValidCurrency()));
        BDDMockito.when(currencyServiceMock.getCurrencyByInterval(
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
                .thenReturn(validCurrencyList);
    }

    @Test
    @DisplayName("latestCurrency return latest Currency when successful")
    void latestCurrency_ReturnsLatestCurrency_WhenSuccessful(){
        Currency expectedCurrency = CurrencyCreator.createValidCurrency();
        ResponseEntity<Currency> lastCurrency = currencyController.getLatestCurrency();

        // Make assertions
        Assertions.assertThat(lastCurrency.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(lastCurrency.getBody()).isNotNull();
        Assertions.assertThat(lastCurrency.getBody()).isEqualTo(expectedCurrency);
    }

    @Test
    @DisplayName("latestCurrency return null when no latest currency is available")
    void latestCurrency_ReturnsNull_WhenNoLatestCurrencyIsAvailable(){
        // Setup mockito
        BDDMockito.when(currencyServiceMock.getLatestCurrencyOrThrowNoContentException())
                .thenReturn(null);

        // Get response
        ResponseEntity<Currency> lastCurrency = currencyController.getLatestCurrency();

        // Make assertions
        Assertions.assertThat(lastCurrency.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(lastCurrency.getBody()).isNull();
    }

    @Test
    @DisplayName("currencyByInterval return list of Currency when successful")
    void currencyByInterval_ReturnsListOfCurrency_WhenSuccessful() {
        List<Currency> validCurrencyList = new ArrayList<>(List.of(CurrencyCreator.createValidCurrency()));
        ResponseEntity<List<Currency>> currencyByInterval = currencyController.getCurrencyByInterval(
                ("2000-10-02"),
                ("2000-10-03"));

        // Make assertions
        Assertions.assertThat(currencyByInterval.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(currencyByInterval.getBody()).isNotNull();
        Assertions.assertThat(currencyByInterval.getBody())
                .isNotEmpty()
                .hasSize(1);
        Assertions.assertThat(currencyByInterval.getBody()).isEqualTo(validCurrencyList);
    }

    @Test
    @DisplayName("Return empty list when a given date does not contains any Currency instances")
    void currencyByInterval_ReturnsEmptyList_WhenNoCurrenciesOnTheInterval() {
        BDDMockito.when(currencyServiceMock.getCurrencyByInterval(
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
                .thenReturn(Collections.emptyList());

        ResponseEntity<List<Currency>> currencyByInterval = currencyController.getCurrencyByInterval(
                ("2000-10-02"),
                ("2000-10-03"));

        Assertions.assertThat(currencyByInterval.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(currencyByInterval.getBody()).isNotNull().isEmpty();
    }
}
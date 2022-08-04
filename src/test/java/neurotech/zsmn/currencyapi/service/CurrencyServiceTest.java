package neurotech.zsmn.currencyapi.service;

import neurotech.zsmn.currencyapi.domain.Currency;
import neurotech.zsmn.currencyapi.repository.CurrencyRepository;
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
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ExtendWith(SpringExtension.class)
class CurrencyServiceTest {
    @InjectMocks
    private CurrencyService currencyService;

    @Mock
    public CurrencyRepository currencyRepositoryMock;

    @BeforeEach
    void setup() {
        // Setup mock for findTop1ByOrderByIdDesc()
        Currency validCurrency = CurrencyCreator.createValidCurrency();
        BDDMockito.when(currencyRepositoryMock.findTop1ByOrderByIdDesc())
                .thenReturn(validCurrency);

        // Setup mock for save()
        BDDMockito.when(currencyRepositoryMock.save(ArgumentMatchers.any(Currency.class)))
                .thenReturn(validCurrency);

        // Setup mock for getCurrencyByInterval()
        List<Currency> validCurrencyList = new ArrayList<>(List.of(CurrencyCreator.createValidCurrency()));
        BDDMockito.when(currencyRepositoryMock.findByDateLessThanEqualAndDateGreaterThanEqual(
                ArgumentMatchers.any(Date.class), ArgumentMatchers.any(Date.class)))
                .thenReturn(validCurrencyList);
    }

    @Test
    @DisplayName("insertCurrency saves a given currency when successful")
    void insertCurrency_ReturnsValidCurrency_WhenSuccessful() {
        Currency expectedCurrency = CurrencyCreator.createValidCurrency();

        // Save
        Currency savedCurrency = currencyService.insertCurrency(CurrencyCreator.createCurrencyToSave());

        // Make assertions
        Assertions.assertThat(savedCurrency).isNotNull();
        Assertions.assertThat(savedCurrency).isEqualTo(expectedCurrency);
    }

    @Test
    @DisplayName("getLatestCurrencyOrThrowNoContentException return latest Currency when successful")
    void getLatestCurrencyOrThrowNoContentException_ReturnsLatestCurrency_WhenSuccessful(){
        Currency expectedCurrency = CurrencyCreator.createValidCurrency();
        Currency lastCurrency = currencyService.getLatestCurrencyOrThrowNoContentException();

        // Make assertions
        Assertions.assertThat(lastCurrency).isNotNull();
        Assertions.assertThat(lastCurrency).isEqualTo(expectedCurrency);
    }

    @Test
    @DisplayName("getLatestCurrencyOrThrowNoContentException return null when no latest currency is available")
    void getLatestCurrencyOrThrowNoContentException_ThrowNoContentException_WhenNoLatestCurrencyIsAvailable(){
        // Setup mockito
        BDDMockito.when(currencyRepositoryMock.findTop1ByOrderByIdDesc())
                .thenReturn(null);

        // Check if throw exception
        Assertions.assertThatExceptionOfType(ResponseStatusException.class)
                .isThrownBy(() -> currencyService.getLatestCurrencyOrThrowNoContentException())
                .withMessageContaining("No currencies are available.");
    }

    @Test
    @DisplayName("findByDateLessThanEqualAndDateGreaterThanEqual return list of Currency when successful")
    void findByDateLessThanEqualAndDateGreaterThanEqual_ReturnsListOfCurrency_WhenSuccessful() {
        List<Currency> validCurrencyList = new ArrayList<>(List.of(CurrencyCreator.createValidCurrency()));
        List<Currency> currencyByInterval = currencyService.getCurrencyByInterval(
                ("2000-10-02"),
                ("2000-10-03"));

        // Make assertions
        Assertions.assertThat(currencyByInterval).isNotNull();
        Assertions.assertThat(currencyByInterval)
                .isNotEmpty()
                .hasSize(1);
        Assertions.assertThat(currencyByInterval).isEqualTo(validCurrencyList);
    }

    @Test
    @DisplayName("currencyByInterval return empty list when have no currencies on the interval")
    void findByDateLessThanEqualAndDateGreaterThanEqual_ReturnsEmptyList_WhenNoCurrenciesOnTheInterval() {
        BDDMockito.when(currencyRepositoryMock.findByDateLessThanEqualAndDateGreaterThanEqual(
                ArgumentMatchers.any(Date.class), ArgumentMatchers.any(Date.class)))
                .thenReturn(Collections.emptyList());

        List<Currency> currencyByInterval = currencyService.getCurrencyByInterval(
                ("2000-10-02"),
                ("2000-10-03"));

        Assertions.assertThat(currencyByInterval).isNotNull().isEmpty();
    }

    @Test
    @DisplayName("currencyByInterval throw bad argument exception when given a invalid date")
    void findByDateLessThanEqualAndDateGreaterThanEqual_ThrowBadArgumentException_WhenGivenInvalidDate() {
        // Check if throw exception
        Assertions.assertThatExceptionOfType(ResponseStatusException.class)
                .isThrownBy(() -> currencyService.getCurrencyByInterval(("12-12-12"), ("14-14-14")))
                .withMessageContaining("Invalid date format.");
    }
}
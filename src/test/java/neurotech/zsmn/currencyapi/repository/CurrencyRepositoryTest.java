package neurotech.zsmn.currencyapi.repository;

import neurotech.zsmn.currencyapi.domain.Currency;
import neurotech.zsmn.currencyapi.util.CurrencyCreator;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

@DataJpaTest
class CurrencyRepositoryTest {
    @Autowired
    private CurrencyRepository currencyRepository;

    @Test
    @DisplayName("Save the created currency on the database when successful")
    void save_PersistCurrency_WhenSuccessful() {
        Currency currency = CurrencyCreator.createCurrencyToSave();
        Currency savedCurrency = currencyRepository.save(currency);

        Assertions.assertThat(savedCurrency).isNotNull();
        Assertions.assertThat(savedCurrency.getId()).isNotNull();
        Assertions.assertThat(savedCurrency.getBase()).isEqualTo(currency.getBase());
        Assertions.assertThat(savedCurrency.getDate()).isEqualTo(currency.getDate());
        Assertions.assertThat(savedCurrency.getRates()).isEqualTo(currency.getRates());
    }

    @Test
    @DisplayName("Find the latest currency returns null when no latest currency is available.")
    void findLatestCurrency_ReturnsNull_WhenNoLatestCurrency() {
        // Get the latest currency
        Currency latestCurrency = currencyRepository.findTop1ByOrderByIdDesc();

        // Make assertion
        Assertions.assertThat(latestCurrency).isNull();
    }

    @Test
    @DisplayName("Find the latest currency added to the database when successful")
    void findLatestCurrency_ReturnsCurrency_WhenSuccessful() {
        // Save 10 created currencies
        List<Currency> savedCurrencies = new ArrayList<>();
        for(int i = 0; i < 10; i++) {
            Currency currency = CurrencyCreator.createCurrencyToSave();
            Currency savedCurrency = currencyRepository.save(currency);
            savedCurrencies.add(savedCurrency);
        }

        // Get the latest currency
        Currency latestCurrency = currencyRepository.findTop1ByOrderByIdDesc();

        // Get latest saved currency
        Currency latestSavedCurrency = savedCurrencies.get(savedCurrencies.size() - 1);

        // Make assertions
        Assertions.assertThat(latestCurrency).isNotNull();
        Assertions.assertThat(latestCurrency.getId()).isEqualTo(latestSavedCurrency.getId());
        Assertions.assertThat(latestCurrency.getBase()).isEqualTo(latestSavedCurrency.getBase());
        Assertions.assertThat(latestCurrency.getDate()).isEqualTo(latestSavedCurrency.getDate());
        Assertions.assertThat(latestCurrency.getRates()).isEqualTo(latestSavedCurrency.getRates());
    }

    @Test
    @DisplayName("Find the currencies that belongs to a given date interval returns a empty list when the interval is" +
            "not available in the database")
    void findByDateInterval_ReturnsEmptyList_WhenNoCurrenciesInTheGivenInterval() {
        // Create 3 currencies with different dates
        List<Date> dateList = List.of(Date.valueOf("2022-08-07"), Date.valueOf("2022-08-08"),Date.valueOf("2022-08-09"));
        for(int i = 0; i < 3; i++){
            Currency currency = CurrencyCreator.createCurrencyToSave();
            currency.setDate(dateList.get(i));
            currencyRepository.save(currency);
        }

        // Make request using different dates from the previous created ones
        List<Currency> currenciesByDate = currencyRepository.findByDateLessThanEqualAndDateGreaterThanEqual(
                Date.valueOf("2000-10-02"),
                Date.valueOf("2022-08-06"));

        // Make assertions
        Assertions.assertThat(currenciesByDate).isNotNull();
        Assertions.assertThat(currenciesByDate).isEmpty();
    }

    @Test
    @DisplayName("Find the currencies that belongs to a given date interval when successful")
    void findByDateInterval_ReturnsListOfCurrency_WhenSuccessful() {
        // Create 3 currencies with different dates
        List<Currency> savedCurrenciesList = new ArrayList<>();
        List<Date> dateList = List.of(Date.valueOf("2022-08-07"), Date.valueOf("2022-08-08"),Date.valueOf("2022-08-09"));
        for(int i = 0; i < 3; i++){
            Currency currency = CurrencyCreator.createCurrencyToSave();
            currency.setDate(dateList.get(i));
            Currency savedCurrency = currencyRepository.save(currency);
            savedCurrenciesList.add(savedCurrency);
        }

        // Make request only sending a endDate (the code will return all currencies until the given endDate)
        // Note: we are sending startDate as "1970-01-01" because this is the start sent in the controller
        List<Currency> currenciesByDefaultDate = currencyRepository.findByDateLessThanEqualAndDateGreaterThanEqual(
                dateList.get(dateList.size() - 1), Date.valueOf("1970-01-01"));

        // Make assertions
        Assertions.assertThat(currenciesByDefaultDate).isNotNull();
        Assertions.assertThat(currenciesByDefaultDate).hasSize(savedCurrenciesList.size());

        // Create list of possible requests with indexes, taken 2-by-2
        List<List<Integer>> tests = List.of(List.of(0, 1), List.of(1, 2), List.of(0, 2));
        for (List<Integer> test : tests) {
            int startIndex = test.get(0);
            int endIndex = test.get(1);
            List<Currency> currenciesByDate = currencyRepository.findByDateLessThanEqualAndDateGreaterThanEqual(
                    dateList.get(endIndex),
                    dateList.get(startIndex));

            // Assert that the request is not empty
            Assertions.assertThat(currenciesByDate).isNotEmpty();

            // Assert the size of the list (needs to be the difference of the indexes + 1)
            Assertions.assertThat(currenciesByDate).hasSize(endIndex - startIndex + 1);

            // Check if matches with the saved dates
            int startListIndex = 0;
            for (int j = startIndex; j < endIndex; j++) {
                // Get the saved currency in the list and the response currency from the request from DB
                Currency savedCurrency = savedCurrenciesList.get(j);
                Currency responseCurrency = currenciesByDate.get(startListIndex);

                // Make assertions
                Assertions.assertThat(responseCurrency).isNotNull();
                Assertions.assertThat(responseCurrency.getId()).isEqualTo(savedCurrency.getId());
                Assertions.assertThat(responseCurrency.getBase()).isEqualTo(savedCurrency.getBase());
                Assertions.assertThat(responseCurrency.getDate()).isEqualTo(savedCurrency.getDate());
                Assertions.assertThat(responseCurrency.getRates()).isEqualTo(savedCurrency.getRates());

                // Pass to the next
                startListIndex += 1;
            }
        }
    }
}
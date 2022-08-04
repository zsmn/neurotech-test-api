package neurotech.zsmn.currencyapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import neurotech.zsmn.currencyapi.domain.Currency;
import neurotech.zsmn.currencyapi.repository.CurrencyRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Date;
import java.util.List;

@Log4j2
@Service
@RequiredArgsConstructor
public class CurrencyService {
    private final CurrencyRepository currencyRepository;

    public Currency insertCurrency(Currency currency) {
        return currencyRepository.save(currency);
    }
    public Currency getLatestCurrencyOrThrowNoContentException() {
        Currency latestCurrency = currencyRepository.findTop1ByOrderByIdDesc();

        if(latestCurrency == null) {
            throw new ResponseStatusException(HttpStatus.NO_CONTENT, "No currencies are available.");
        }

        return latestCurrency;
    }

    public List<Currency> getCurrencyByInterval(String startDate, String endDate) {
        try {
            Date.valueOf(startDate);
            Date.valueOf(endDate);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid date format.");
        }

        return currencyRepository.findByDateLessThanEqualAndDateGreaterThanEqual(Date.valueOf(endDate), Date.valueOf(startDate));
    }
}

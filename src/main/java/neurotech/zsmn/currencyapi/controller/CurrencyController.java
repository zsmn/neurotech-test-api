package neurotech.zsmn.currencyapi.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import neurotech.zsmn.currencyapi.domain.Currency;
import neurotech.zsmn.currencyapi.service.CurrencyService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("currency")
@Log4j2
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:8081")
public class CurrencyController {
    public final CurrencyService currencyService;

    @GetMapping(path = "latest")
    public ResponseEntity<Currency> getLatestCurrency() {
        return ResponseEntity.ok(currencyService.getLatestCurrencyOrThrowNoContentException());
    }

    @GetMapping(path = "interval")
    public ResponseEntity<List<Currency>> getCurrencyByInterval(@RequestParam(defaultValue = "1970-01-01", name = "startDate") String startDate, @RequestParam("endDate") String endDate) {
        return ResponseEntity.ok(currencyService.getCurrencyByInterval(startDate, endDate));
    }
}

package neurotech.zsmn.currencyapi.producer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.extern.log4j.Log4j2;
import neurotech.zsmn.currencyapi.domain.Currency;
import neurotech.zsmn.currencyapi.producer.configuration.CurrencyProducerConfiguration;
import neurotech.zsmn.currencyapi.service.CurrencyService;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Map;

@Log4j2
@Component
@Profile("!test")
public class CurrencyProducerClient implements DisposableBean, Runnable {
    @Autowired
    private CurrencyService currencyService;
    private Thread thread;

    private Boolean destroyThread;

    CurrencyProducerClient(){
        this.thread = new Thread(this);
        this.thread.start();
        destroyThread = false;
    }

    private void registerValuesFromYear() {
        RestTemplate template = new RestTemplate();

        Date now = Date.valueOf(LocalDate.now());
        for (int i = 0; i < CurrencyProducerConfiguration.initializePeriodFromToday; i++) {
            // Get now date
            Date oneYearLater = Date.valueOf(now.toLocalDate().minusYears(1));

            UriComponents uri = UriComponentsBuilder.newInstance()
                    .scheme("https")
                    .host("api.exchangerate.host")
                    .path("timeseries")
                    .queryParam("base", "BRL")
                    .queryParam("symbols", "USD")
                    .queryParam("start_date", oneYearLater.toString())
                    .queryParam("end_date", now.toString())
                    .build();

            // make an HTTP GET request with headers
            ResponseEntity<CurrencyProducerResponse> currencyByInterval = template.exchange(uri.toUriString(),
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<CurrencyProducerResponse>() {
                    });

            // Produce currencies from the given interval
            String base = currencyByInterval.getBody().getBase();
            Map<String, Map<String, Float>> responseRates = currencyByInterval.getBody().getRates();

            // Register on database
            for(String date : responseRates.keySet()) {
                Currency currency = Currency.builder()
                        .date(Date.valueOf(date))
                        .rates(responseRates.get(date))
                        .base(base)
                        .build();

                currencyService.insertCurrency(currency);
            }

            now = oneYearLater;
        }
    }

    public void run() {
        if(CurrencyProducerConfiguration.initializeDatabase) {
            registerValuesFromYear();
        }

        while (!destroyThread) {
            try {
                RestTemplate template = new RestTemplate();

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);

                UriComponents uri = UriComponentsBuilder.newInstance()
                        .scheme("https")
                        .host("api.exchangerate.host")
                        .path("latest")
                        .queryParam("base", "BRL")
                        .queryParam("symbols", "USD")
                        .build();

                // make an HTTP GET request with headers
                ResponseEntity<Currency> latestCurrency = template.exchange(uri.toUriString(),
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<Currency>() {
                        });

                log.info(latestCurrency.getBody());
                currencyService.insertCurrency(latestCurrency.getBody());

                Thread.sleep(CurrencyProducerConfiguration.checkPeriod * 24 * 60 * 60 * 1000);
            }
            catch (InterruptedException e) {
                break;
            }
        }
    }

    @Override
    public void destroy(){
        destroyThread = true;
    }
}

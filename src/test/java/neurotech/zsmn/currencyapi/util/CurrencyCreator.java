package neurotech.zsmn.currencyapi.util;

import neurotech.zsmn.currencyapi.domain.Currency;

import java.sql.Date;
import java.util.Map;

public class CurrencyCreator {

    public static Currency createCurrencyToSave() {
        return Currency.builder()
                .base("BRL")
                .date(Date.valueOf("2000-10-02"))
                .rates(Map.ofEntries(Map.entry("USD", 1.0f)))
                .build();
    }

    public static Currency createValidCurrency() {
        return Currency.builder()
                .id(1L)
                .base("BRL")
                .date(Date.valueOf("2000-10-02"))
                .rates(Map.ofEntries(Map.entry("USD", 1.0f)))
                .build();
    }
}

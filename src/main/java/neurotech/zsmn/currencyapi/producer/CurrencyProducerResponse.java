package neurotech.zsmn.currencyapi.producer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CurrencyProducerResponse {
    String base;
    Map<String, Map<String, Float>> rates;
}

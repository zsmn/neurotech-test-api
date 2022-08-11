package neurotech.zsmn.currencyapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@SpringBootApplication
public class CurrencyApiApplication {

	public static void main(String[] args) {
		// Start SpringBoot application
		SpringApplication.run(CurrencyApiApplication.class, args);
	}

}

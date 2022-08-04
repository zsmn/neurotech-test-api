package neurotech.zsmn.currencyapi.repository;

import neurotech.zsmn.currencyapi.domain.Currency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface CurrencyRepository extends JpaRepository<Currency, Long> {
    Currency findTop1ByOrderByIdDesc();
    List<Currency> findByDateLessThanEqualAndDateGreaterThanEqual(Date endDate, Date startDate);
}

package ru.yandex.tonychem.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.tonychem.dto.PaymentInfo;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class VacationPayServiceImpl implements VacationPayService {
    private final HolidayInformationProvider holidayInformationProvider;
    private static final BigDecimal AVERAGE_DAYS_IN_MONTH = BigDecimal.valueOf(29.3);
    private static final BigDecimal MONTH_IN_YEAR = BigDecimal.valueOf(12);

    @Override
    public PaymentInfo calculatePayment(BigDecimal averageAnnualSalary, LocalDate vacationStart, LocalDate vacationEnd) {
        int stateHolidaysCount = holidayInformationProvider.countStateHolidaysInRange(vacationStart, vacationEnd);
        int payedDaysCount = (int) ChronoUnit.DAYS.between(vacationStart, vacationEnd.plusDays(1)) - stateHolidaysCount;

        if (payedDaysCount <= 0) {
            return new PaymentInfo(new BigDecimal(BigInteger.ZERO, 2));
        }

        BigDecimal vacationLeavePayment = averageAnnualSalary.divide(AVERAGE_DAYS_IN_MONTH.multiply(MONTH_IN_YEAR), 2,
                        RoundingMode.HALF_DOWN)
                .multiply(BigDecimal.valueOf(payedDaysCount));

        return new PaymentInfo(vacationLeavePayment);
    }
}

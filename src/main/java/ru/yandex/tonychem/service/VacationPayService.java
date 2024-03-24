package ru.yandex.tonychem.service;

import ru.yandex.tonychem.dto.PaymentInfo;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface VacationPayService {
    PaymentInfo calculatePayment(BigDecimal averageAnnualSalary, LocalDate vacationStart, LocalDate vacationEnd);
}

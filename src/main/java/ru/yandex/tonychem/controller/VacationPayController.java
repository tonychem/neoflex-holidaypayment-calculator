package ru.yandex.tonychem.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.tonychem.exception.ApiExceptionMessage;
import ru.yandex.tonychem.service.VacationPayService;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(consumes = APPLICATION_JSON_VALUE,
        produces = APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class VacationPayController {
    private static final String REQUEST_PARAM_DATE_PATTERN = "yyyy-MM-dd";
    private final VacationPayService vacationPayService;

    @GetMapping("/calculate")
    public ResponseEntity<?> calculateVacationPay(@RequestParam(value = "annualAverage", required = true) BigDecimal average,

                                                  @RequestParam(value = "from", required = true)
                                                  @DateTimeFormat(pattern = REQUEST_PARAM_DATE_PATTERN) LocalDate dateFromIncl,

                                                  @RequestParam(value = "to", required = true)
                                                  @DateTimeFormat(pattern = REQUEST_PARAM_DATE_PATTERN) LocalDate dateToIncl) {

        if (average.compareTo(BigDecimal.ZERO) <= 0 || dateFromIncl.isAfter(dateToIncl)
                || dateFromIncl.isEqual(dateToIncl)) {
            return new ResponseEntity<>(new ApiExceptionMessage("Ошибка валидации входных данных"), HttpStatus.BAD_REQUEST);
        }

        return ResponseEntity.ok(vacationPayService.calculatePayment(average, dateFromIncl, dateToIncl));
    }
}

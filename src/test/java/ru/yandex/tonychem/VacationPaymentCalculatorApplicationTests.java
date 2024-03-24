package ru.yandex.tonychem;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureMockMvc
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class VacationPaymentCalculatorApplicationTests {
    private final MockMvc mvc;

    @Test
    @DisplayName("Выбрасывает BAD_REQUEST, если дата конца отпуска раньше, чем дата начала")
    void shouldThrowBadRequestOnIncorrectHolidayDates() throws Exception {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.minusDays(20);
        String annualAverage = String.valueOf(300_000);

        mvc.perform(get("/calculate")
                        .param("annualAverage", annualAverage)
                        .param("from", startDate.toString())
                        .param("to", endDate.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Выбрасывает BAD_REQUEST, если среднегодовая сумма 0 и меньше")
    void shouldThrowBadRequestOnIncorrectAmount() throws Exception {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(20);
        String annualAverage = String.valueOf(0);

        mvc.perform(get("/calculate")
                        .param("annualAverage", annualAverage)
                        .param("from", startDate.toString())
                        .param("to", endDate.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest(name = "{index} - {4}")
    @MethodSource("datesWithoutStateHolidaysProvider")
    @DisplayName("Если в отпускных отсутствуют праздничные дни, то должен учитывать всю неделю полностью с учетом выходных")
    void shouldCountAllDaysInWeekWithoutStateHolidays(LocalDate from, LocalDate to, int annualAverage, BigDecimal expectedPayment,
                                                      String message) throws Exception {
        mvc.perform(get("/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("annualAverage", String.valueOf(annualAverage))
                        .param("from", from.toString())
                        .param("to", to.toString()))
                .andExpect(jsonPath("$.payment", is(expectedPayment.doubleValue())));
    }

    @ParameterizedTest(name = "{index} - {4}")
    @MethodSource("datesWithStateHolidaysProvider")
    @DisplayName("Если в отпускных присутствуют праздничные дни, то должен вычитать количество праздничных дней из оплачиваемого отпуска")
    void shouldCountDaysInWeekWithStateHolidays(LocalDate from, LocalDate to, int annualAverage, BigDecimal expectedPayment,
                                                String message) throws Exception {
        mvc.perform(get("/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("annualAverage", String.valueOf(annualAverage))
                        .param("from", from.toString())
                        .param("to", to.toString()))
                .andExpect(jsonPath("$.payment", is(expectedPayment.doubleValue())));
    }


    private static Stream<Arguments> datesWithoutStateHolidaysProvider() {
        return Stream.of(
                Arguments.of(LocalDate.of(2024, 2, 12), LocalDate.of(2024, 2, 18),
                        200_000,
                        BigDecimal.valueOf(3981.81),
                        "7 оплачиваемых отпускных"),
                Arguments.of(LocalDate.of(2024, 3, 15), LocalDate.of(2024, 3, 20),
                        200_000,
                        BigDecimal.valueOf(3412.98),
                        "6 оплачиваемых отпускных")
        );
    }

    private static Stream<Arguments> datesWithStateHolidaysProvider() {
        return Stream.of(
                Arguments.of(LocalDate.of(2024, 4, 29), LocalDate.of(2024, 5, 5),
                        200_000,
                        BigDecimal.valueOf(3412.98),
                        "C 1 мая => 6 оплачиваемых отпускных"),
                Arguments.of(LocalDate.of(2024, 3, 4), LocalDate.of(2024, 3, 10),
                        200_000,
                        BigDecimal.valueOf(3412.98),
                        "С 8 марта => 6 оплачиваемых отпускных"),
                Arguments.of(
                        LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 8),
                        200_000,
                        BigDecimal.ZERO,
                        "Неоплачиваемые отпускные в новый год => 0 дней"
                )
        );
    }
}

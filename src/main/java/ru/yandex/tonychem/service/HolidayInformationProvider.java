package ru.yandex.tonychem.service;

import java.time.LocalDate;

public interface HolidayInformationProvider {
    int countStateHolidaysInRange(LocalDate from, LocalDate to);
}

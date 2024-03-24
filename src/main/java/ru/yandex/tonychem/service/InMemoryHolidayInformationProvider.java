package ru.yandex.tonychem.service;

import java.time.LocalDate;
import java.util.Set;
import java.util.TreeSet;

public class InMemoryHolidayInformationProvider implements HolidayInformationProvider {
    private final TreeSet<LocalDate> stateHolidaysSet;

    public InMemoryHolidayInformationProvider(TreeSet<LocalDate> stateHolidaysSet) {
        this.stateHolidaysSet = stateHolidaysSet;
    }

    @Override
    public int countStateHolidaysInRange(LocalDate from, LocalDate to) {
        Set<LocalDate> stateHolidaysInRangeSet = stateHolidaysSet.subSet(from, true, to, true);
        return stateHolidaysInRangeSet.size();
    }
}

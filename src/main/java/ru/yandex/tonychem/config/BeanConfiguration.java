package ru.yandex.tonychem.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import ru.yandex.tonychem.service.HolidayInformationProvider;
import ru.yandex.tonychem.service.InMemoryHolidayInformationProvider;

import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Configuration
public class BeanConfiguration {

    @Value("classpath:holidays/*")
    private Resource[] resources;

    @Bean
    public HolidayInformationProvider holidayProvider() throws IOException {
        TreeSet<LocalDate> stateHolidaysDateSet = new TreeSet<>();

        for (Resource resource : resources) {
            stateHolidaysDateSet.addAll(readStateHolidaysFromResource(resource));
        }

        return new InMemoryHolidayInformationProvider(stateHolidaysDateSet);
    }

    private Set<LocalDate> readStateHolidaysFromResource(Resource resource) throws IOException {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return Files.readAllLines(resource.getFile().toPath()).stream()
                .map(string -> LocalDate.parse(string, dateTimeFormatter))
                .collect(Collectors.toSet());
    }
}

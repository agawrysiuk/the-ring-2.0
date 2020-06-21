package pl.agawrysiuk.service.saver;

import java.time.LocalDateTime;

public abstract class Saver {

    String createSqlFilePrefix() {
        LocalDateTime now = LocalDateTime.now();
        return "V1_"
                + now.getYear()
                + addLeadingZerosIfNeeded(String.valueOf(now.getMonthValue()))
                + addLeadingZerosIfNeeded(String.valueOf(now.getDayOfMonth()))
                + addLeadingZerosIfNeeded(String.valueOf(now.getHour()))
                + addLeadingZerosIfNeeded(String.valueOf(now.getMinute()))
                + addLeadingZerosIfNeeded(String.valueOf(now.getSecond()));
    }

    private String addLeadingZerosIfNeeded(String string) {
        return string.length() == 1 ? "0".concat(string) : string;
    }
}

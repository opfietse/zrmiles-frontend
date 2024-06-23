package net.opfietse.zrmiles.model;

import java.time.LocalDate;

public record Miles(
    Integer id,
    Integer motorcycleId,
    LocalDate milesDate,
    Integer odometerReading,
    Integer correctionMiles,
    String userComment
) implements Comparable<Miles> {

    @Override
    public int compareTo(Miles o) {
        return odometerReading.compareTo(o.odometerReading);
    }
}

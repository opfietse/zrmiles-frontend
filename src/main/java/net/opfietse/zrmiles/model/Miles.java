package net.opfietse.zrmiles.model;

import java.time.LocalDate;

public record Miles(
    Integer id,
    Integer motorcycleId,
    LocalDate milesDate,
    Integer odometerReading,
    Integer correctionMiles,
    String userComment
) {

}

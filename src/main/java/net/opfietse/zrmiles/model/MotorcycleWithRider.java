package net.opfietse.zrmiles.model;

public record MotorcycleWithRider(
    Integer id,
    Integer riderId,
    String make,
    String model,
    Integer year,
    Short distanceUnit,
    String riderName
) {
}

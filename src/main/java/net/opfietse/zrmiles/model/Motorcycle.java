package net.opfietse.zrmiles.model;

public record Motorcycle(
    Integer id,
    Integer riderId,
    String make,
    String model,
    Integer year,
    Short distanceUnit
) {
}

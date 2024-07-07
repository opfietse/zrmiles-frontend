package net.opfietse.zrmiles.model;

import java.util.List;

public record StatisticsRecord(String name, int riderId, int mileage, List<MilesPlusName> mileages) {
}

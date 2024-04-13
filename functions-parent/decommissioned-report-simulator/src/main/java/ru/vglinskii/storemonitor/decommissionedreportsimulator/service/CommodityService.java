package ru.vglinskii.storemonitor.decommissionedreportsimulator.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import ru.vglinskii.storemonitor.decommissionedreportsimulator.model.Commodity;

@AllArgsConstructor
public class CommodityService {
    private List<String> commoditiesNames;

    public CommodityService() {
        this(
                List.of(
                        "Грудка куриная ТРОЕКУРОВО",
                        "Сосиски СИБКОЛБАСЫ Молочные 1-ый сорт",
                        "Колбаса вареная СИБКОЛБАСЫ Докторская",
                        "Томаты тепличные",
                        "Набор свежей зелени укроп и петрушка",
                        "Молоко пастеризованное ДОМИК В ДЕРЕВНЕ 2.5%",
                        "Сыр БРЕСТ-ЛИТОВСК Сливочный 50%"
                )
        );
    }

    public List<Commodity> getCommoditiesForDecommission() {
        int decommissionedCommoditiesCount = (int) (Math.random() * 10);
        List<Commodity> commodities = new ArrayList<>(decommissionedCommoditiesCount);

        for (int i = 0; i < decommissionedCommoditiesCount; i++) {
            commodities.add(generateRandomCommodity());
        }

        return commodities;
    }

    private Commodity generateRandomCommodity() {
        return Commodity.builder()
                .id(UUID.randomUUID())
                .name(commoditiesNames.get((int) (Math.random() * commoditiesNames.size())))
                .build();
    }
}

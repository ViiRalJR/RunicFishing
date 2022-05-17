package me.viiral.runicfishing.enchants;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EnchantCost {

    private final int points;
    private final int money;

    public EnchantCost(int points, int money) {
        this.points = points;
        this.money = money;
    }

}

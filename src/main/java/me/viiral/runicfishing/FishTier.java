package me.viiral.runicfishing;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FishTier {

    private final String name;
    private final int points;
    private final int money;

    public FishTier(String name, int points, int money){
        this.name = name;
        this.points = points;
        this.money = money;
    }
}

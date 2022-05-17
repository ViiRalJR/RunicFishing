package me.viiral.runicfishing;

import com.google.common.util.concurrent.AtomicDouble;
import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.atomic.AtomicInteger;

@Getter
@Setter
public class PlayerFishStats {

    private final AtomicInteger amount;
    private final AtomicDouble points;

    public PlayerFishStats(int amount, double points){
        this.amount = new AtomicInteger(amount);
        this.points = new AtomicDouble(points);
    }

    public void updateValues(int amount, double points){
        this.amount.addAndGet(amount);
        this.points.addAndGet(points);
    }

}

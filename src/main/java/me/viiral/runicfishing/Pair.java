package me.viiral.runicfishing;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class Pair<L, R> {

    @Getter private final L left;

    @Getter private final R right;

}

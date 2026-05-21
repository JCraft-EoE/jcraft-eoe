package net.arna.jcraft.api.attack.core;

import com.mojang.datafixers.util.*;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class MoveActionType<T extends MoveAction<? extends T, ?>> {
    public abstract Codec<T> getCodec();

    protected RecordCodecBuilder<T, RunMoment> runMoment() {
        return RunMoment.CODEC.orElse(RunMoment.ON_STRIKE).forGetter(MoveAction::getRunMoment);
    }

    protected <R extends MoveAction<?, ?>> Function<RunMoment, R> apply(Supplier<R> supplier) {
        return runMoment -> {
            R r = supplier.get();
            r.setRunMoment(runMoment);
            return r;
        };
    }

    protected <R extends MoveAction<?, ?>, T1> BiFunction<RunMoment, T1, R> apply(Function<T1, R> func) {
        return (runMoment, t1) -> {
            R r = func.apply(t1);
            r.setRunMoment(runMoment);
            return r;
        };
    }

    protected <R extends MoveAction<?, ?>, T1, T2> Function3<RunMoment, T1, T2, R> apply(BiFunction<T1, T2, R> func) {
        return (runMoment, t1, t2) -> {
            R r = func.apply(t1, t2);
            r.setRunMoment(runMoment);
            return r;
        };
    }

    protected <R extends MoveAction<?, ?>, T1, T2, T3> Function4<RunMoment, T1, T2, T3, R> apply(Function3<T1, T2, T3, R> func) {
        return (runMoment, t1, t2, t3) -> {
            R r = func.apply(t1, t2, t3);
            r.setRunMoment(runMoment);
            return r;
        };
    }

    protected <R extends MoveAction<?, ?>, T1, T2, T3, T4> Function5<RunMoment, T1, T2, T3, T4, R> apply(Function4<T1, T2, T3, T4, R> func) {
        return (runMoment, t1, t2, t3, t4) -> {
            R r = func.apply(t1, t2, t3, t4);
            r.setRunMoment(runMoment);
            return r;
        };
    }

    protected <R extends MoveAction<?, ?>, T1, T2, T3, T4, T5> Function6<RunMoment, T1, T2, T3, T4, T5, R>
    apply(Function5<T1, T2, T3, T4, T5, R> func) {
        return (runMoment, t1, t2, t3, t4, t5) -> {
            R r = func.apply(t1, t2, t3, t4, t5);
            r.setRunMoment(runMoment);
            return r;
        };
    }

    protected <R extends MoveAction<?, ?>, T1, T2, T3, T4, T5, T6> Function7<RunMoment, T1, T2, T3, T4, T5, T6, R>
    apply(Function6<T1, T2, T3, T4, T5, T6, R> func) {
        return (runMoment, t1, t2, t3, t4, t5, t6) -> {
            R r = func.apply(t1, t2, t3, t4, t5, t6);
            r.setRunMoment(runMoment);
            return r;
        };
    }

    protected <R extends MoveAction<?, ?>, T1, T2, T3, T4, T5, T6, T7> Function8<RunMoment, T1, T2, T3, T4, T5, T6, T7, R>
    apply(Function7<T1, T2, T3, T4, T5, T6, T7, R> func) {
        return (runMoment, t1, t2, t3, t4, t5, t6, t7) -> {
            R r = func.apply(t1, t2, t3, t4, t5, t6, t7);
            r.setRunMoment(runMoment);
            return r;
        };
    }
}

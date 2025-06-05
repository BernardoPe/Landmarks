package pt.isel.cn.landmarks.domain;

import java.util.function.Consumer;

public class Either <L, R> {
    private final L left;
    private final R right;
    private final boolean isLeft;

    private Either(L left, R right, boolean isLeft) {
        this.left = left;
        this.right = right;
        this.isLeft = isLeft;
    }

    public static <L, R> Either<L, R> left(L left) {
        return new Either<>(left, null, true);
    }

    public static <L, R> Either<L, R> right(R right) {
        return new Either<>(null, right, false);
    }

    public boolean isLeft() {
        return isLeft;
    }

    public boolean isRight() {
        return !isLeft;
    }

    public void ifLeft(Consumer<? super L> consumer) {
        if (isLeft) {
            consumer.accept(left);
        }
    }

    public void ifRight(Consumer<? super R> consumer) {
        if (!isLeft) {
            consumer.accept(right);
        }
    }

    public L getLeft() {
        if (isLeft) {
            return left;
        } else {
            throw new IllegalStateException("This Either instance contains a right value, not a left.");
        }
    }

    public R getRight() {
        if (!isLeft) {
            return right;
        } else {
            throw new IllegalStateException("This Either instance contains a left value, not a right.");
        }
    }
}

package org.seasar.doma.wrapper;

import jp.co.camnet.doma.scala.ScalaNumbers;
import org.seasar.doma.DomaNullPointerException;
import scala.math.BigDecimal;
import scala.math.BigInt;

import java.math.BigInteger;


public class BigDecimalScalaWrapper extends AbstractWrapper<BigDecimal> implements
        NumberWrapper<BigDecimal> {

    /**
     * インスタンスを構築します。
     */
    public BigDecimalScalaWrapper() {
        super(BigDecimal.class);
    }

    /**
     * 値を指定してインスタンスを構築します。
     *
     * @param value 値
     */
    public BigDecimalScalaWrapper(BigDecimal value) {
        super(BigDecimal.class, value);
    }

    @Override
    public void set(Number v) {
        if (v instanceof java.math.BigDecimal) {
            super.set(new BigDecimal((java.math.BigDecimal) v));
        } else if (v instanceof BigInteger) {
            super.set(new BigDecimal(new java.math.BigDecimal((java.math.BigInteger) v)));
        } else if (v instanceof BigDecimal) {
            super.set((BigDecimal) v);
        } else if (v instanceof BigInt) {
            BigInt b = (BigInt) v;
            super.set(new BigDecimal(new java.math.BigDecimal(b.bigInteger())));
        } else {
            super.set(new BigDecimal(new java.math.BigDecimal(v.doubleValue())));
        }
    }

    @Override
    public void increment() {
        BigDecimal value = doGet();
        if (value != null) {
            doSet(value.$plus(ScalaNumbers.BigDecimalOne));
        }
    }

    @Override
    public void decrement() {
        BigDecimal value = doGet();
        if (value != null) {
            doSet(value.$minus(ScalaNumbers.BigDecimalOne));
        }
    }

    @Override
    public <R, P, Q, TH extends Throwable> R accept(
            WrapperVisitor<R, P, Q, TH> visitor, P p, Q q) throws TH {
        if (visitor == null) {
            throw new DomaNullPointerException("visitor");
        }
        return visitor.visitBigDecimalScalaWrapper(this, p, q);
    }
}

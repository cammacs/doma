package org.seasar.doma.wrapper;

import org.seasar.doma.DomaNullPointerException;

public class SalaClassWrapper extends AbstractWrapper<?> {

    /**
     * インスタンスを構築します。
     */
    public SalaClassWrapper() {
        super(Object.class);
    }

    /**
     * 値を指定してインスタンスを構築します。
     *
     * @param value 値
     */
    public SalaClassWrapper(Object value) {
        super(Object.class, value);
    }

    @Override
    public <R, P, Q, TH extends Throwable> R accept(
            WrapperVisitor<R, P, Q, TH> visitor, P p, Q q) throws TH {
        if (visitor == null) {
            throw new DomaNullPointerException("visitor");
        }
        return visitor.vit(this, p, q);
    }


}

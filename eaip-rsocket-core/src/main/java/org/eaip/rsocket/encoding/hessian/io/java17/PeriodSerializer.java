package org.eaip.rsocket.encoding.hessian.io.java17;

import com.caucho.hessian.io.AbstractHessianOutput;
import com.caucho.hessian.io.AbstractSerializer;

import java.io.IOException;
import java.time.Period;

public class PeriodSerializer extends AbstractSerializer {

    @Override
    public void writeObject(Object obj, AbstractHessianOutput out) throws IOException {
        if (obj == null) {
            out.writeNull();
        } else {
            out.writeObject(new PeriodHandle((Period) obj));
        }
    }
}

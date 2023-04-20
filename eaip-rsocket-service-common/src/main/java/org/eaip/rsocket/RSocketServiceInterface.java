package org.eaip.rsocket;

import java.lang.annotation.*;

/**
 * RSocket Service interface annotation, and indicate interface as RSocket Service
 *
 * @author CuiCHangHe
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RSocketServiceInterface {
}

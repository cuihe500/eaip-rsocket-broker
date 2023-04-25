package org.eaip.spring.boot.rsocket.responder.invocation;

import org.eaip.spring.boot.rsocket.RSocketProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * RSocket Service Annotation Processor test
 *
 * @author CuiChangHe
 */
public class RSocketServiceAnnotationProcessorTest {
    private RSocketServiceAnnotationProcessor processor;

    @BeforeAll
    public void setUp() {
        processor = new RSocketServiceAnnotationProcessor(new RSocketProperties());
    }

    @Test
    public void testScanRSocketServiceAnnotation() {
        org.eaip.spring.boot.rsocket.responder.invocation.ReactiveTestService reactiveTestService = new ReactiveTestServiceImpl();
        processor.scanRSocketServiceAnnotation(reactiveTestService, "reactiveTestService");
        Assertions.assertTrue(processor.contains(ReactiveTestService.class.getCanonicalName()));
        Assertions.assertNotNull(processor.getInvokeMethod(ReactiveTestService.class.getCanonicalName(), "findNickById"));

    }
}

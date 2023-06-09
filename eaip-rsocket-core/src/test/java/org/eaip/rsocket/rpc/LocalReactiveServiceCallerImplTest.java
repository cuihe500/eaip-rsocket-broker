package org.eaip.rsocket.rpc;

import org.eaip.rsocket.rpc.definition.OperationParameter;
import org.eaip.rsocket.rpc.definition.ReactiveOperation;
import org.eaip.rsocket.rpc.definition.ReactiveServiceInterface;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * LocalReactiveServiceCallerImpl test
 *
 * @author CuiCHangHe
 */
public class LocalReactiveServiceCallerImplTest {

    private LocalReactiveServiceCallerImpl serviceCaller = new LocalReactiveServiceCallerImpl();

    @Test
    public void testAddServiceInterface() {
        ReactiveServiceInterface serviceInterface = serviceCaller.createReactiveServiceInterface("group1", "1.0.0",
                "org.eaip.rsocket.rpc.DemoReactiveService", DemoReactiveService.class);
        System.out.println("=======Reactive Service Interface=====");
        System.out.println(serviceInterface.getNamespace());
        System.out.println(serviceInterface.getName());
        System.out.println("===============Operations=====================");
        List<ReactiveOperation> operations = serviceInterface.getOperations();
        for (ReactiveOperation operation : operations) {
            System.out.println(operation.getName());
            System.out.println(operation.getReturnType());
            System.out.println(operation.getReturnInferredType());
            System.out.println("deprecated: " + operation.isDeprecated());
            if (!operation.getParameters().isEmpty()) {
                System.out.println("===============Parameters=====================");
            }
            for (OperationParameter parameter : operation.getParameters()) {
                System.out.println(parameter.getName());
                System.out.println(parameter.getType());
                System.out.println(parameter.getInferredType());
            }
            System.out.println("================================");
        }
    }
}

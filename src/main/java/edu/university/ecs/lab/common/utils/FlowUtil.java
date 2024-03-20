package edu.university.ecs.lab.common.utils;

import edu.university.ecs.lab.common.models.*;
import edu.university.ecs.lab.common.models.enums.ClassRole;

import java.util.*;
import java.util.stream.Collectors;

public class FlowUtil {
    public static List<Flow> buildFlows(Map<String, MsModel> msModelMap) {
        // 1. get controller name & controller endpoint name
        List<Flow> flows = generateNewFlows(getAllModelControllers(msModelMap));

        for (Flow flow : flows) {
            // 2. get service method call in controller method
            Optional<MethodCall> serviceMethodCall = Optional.ofNullable(findServiceMethodCall(flow));
            if (serviceMethodCall.isPresent()) {
                flow.setServiceMethodCall(serviceMethodCall.get());
                // 3. get service field variable in controller class by method call
                Optional<Field> serviceField = Optional.ofNullable(findServiceField(flow));
                if (serviceField.isPresent()) {
                    flow.setControllerServiceField(serviceField.get());
                    // 4. get service class
                    Optional<JClass> ServiceClass = Optional.ofNullable(findService(flow));
                    if (ServiceClass.isPresent()) {
                        flow.setService(ServiceClass.get());
                        // 5. find service method name
                        Optional<Method> ServiceMethod = Optional.ofNullable(findServiceMethod(flow));
                        if (ServiceMethod.isPresent()) {
                            flow.setServiceMethod(ServiceMethod.get());
                            // 6. find method call in the service
                            Optional<MethodCall> repositoryMethodCall = Optional.ofNullable(findRepositoryMethodCall(flow));
                            if (repositoryMethodCall.isPresent()) {
                                flow.setRepositoryMethodCall(repositoryMethodCall.get());
                                // 7. find repository variable
                                Optional<Field> repositoryField = Optional.ofNullable(findRepositoryField(flow));
                                if (repositoryField.isPresent()) {
                                    flow.setServiceRepositoryField(repositoryField.get());
                                    // 8. find repository class
                                    Optional<JClass> repositoryClass = Optional.ofNullable(findRepository(flow));
                                    if (repositoryClass.isPresent()) {
                                        flow.setRepository(repositoryClass.get());
                                        // 9. find repository method
                                        Optional<Method> repositoryMethod = Optional.ofNullable(findRepositoryMethod(flow));
                                        if (repositoryMethod.isPresent()) {
                                            flow.setRepositoryMethod(repositoryMethod.get());
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return flows;
    }


    private static Map<MsModel, List<JClass>> getAllModelControllers(Map<String, MsModel> msModelMap) {
        Map<MsModel, List<JClass>> controllerMap = new HashMap<>();
        for (MsModel model : msModelMap.values()) {
            controllerMap.put(model, model.getClassList().stream().filter(jClass -> jClass.getRole() == ClassRole.CONTROLLER).collect(Collectors.toList()));
        }
        return controllerMap;
    }

    private static List<Flow> generateNewFlows(Map<MsModel, List<JClass>> controllerMap) {
        List<Flow> flows = new ArrayList<>();
        Flow f;
        for(Map.Entry<MsModel, List<JClass>> controllerList : controllerMap.entrySet()) {
            for(JClass controller : controllerList.getValue()) {
                for (Endpoint endpoint : controller.getEndpoints()) {
                    f = new Flow();
                    f.setController(controller);
                    f.setControllerMethod(endpoint);
                    f.setModel(controllerList.getKey());
                }
            }
        }
        return flows;
    }

    // TODO Should this be find first? Could it not have multiple?
    private static MethodCall findServiceMethodCall(Flow flow) {
        return flow.getController().getMethodCalls().stream()
                .filter(mc -> mc.getParentMethod().equals(flow.getControllerMethod().getMethodName()))
                .findFirst().orElse(null);
    }

    private static Field findServiceField(Flow flow) {
        return flow.getController().getFields().stream()
                .filter(f -> f.getFieldName().equals(flow.getServiceMethodCall().getCalledFieldName()))
                .findFirst().orElse(null);
    }

    private static JClass findService(Flow flow) {
        return flow.getModel().getClassList().stream()
                .filter(jClass -> jClass.getClassName().equals(flow.getControllerServiceField().getFieldType()))
                .findFirst().orElse(null);
    }

    private static Method findServiceMethod(Flow flow) {
        return flow.getService().getMethods().stream()
                .filter(method -> method.getMethodName().equals(flow.getServiceMethodCall().getMethodName()))
                .findFirst().orElse(null);
    }

    private static MethodCall findRepositoryMethodCall(Flow flow) {
        return flow.getService().getMethodCalls().stream()
                .filter(mc -> mc.getParentMethod().equals(flow.getServiceMethod().getMethodName()))
                .findFirst().orElse(null);
    }

    private static Field findRepositoryField(Flow flow) {
        return flow.getService().getFields().stream()
                .filter(f -> f.getFieldName().equals(flow.getRepositoryMethodCall().getCalledFieldName()))
                .findFirst().orElse(null);
    }

    private static JClass findRepository(Flow flow) {
        return flow.getModel().getClassList().stream()
                .filter(jClass -> jClass.getClassName().equals(flow.getServiceRepositoryField().getFieldType()))
                .findFirst().orElse(null);
    }

    private static Method findRepositoryMethod(Flow flow) {
        return flow.getRepository().getMethods().stream()
                .filter(method -> method.getMethodName().equals(flow.getRepositoryMethodCall().getMethodName()))
                .findFirst().orElse(null);
    }

}

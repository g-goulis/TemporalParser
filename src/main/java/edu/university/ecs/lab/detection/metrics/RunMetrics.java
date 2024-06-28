package edu.university.ecs.lab.detection.metrics;

import edu.university.ecs.lab.detection.metrics.model.*;
import edu.university.ecs.lab.detection.metrics.service.MetricCalculator;
import edu.university.ecs.lab.detection.metrics.service.MetricResult;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RunMetrics {

    public static void main(String[] args) {
        String filePath = "./output/IR.json";

        try (FileInputStream inputStream = new FileInputStream(filePath)) {

            JSONTokener tokener = new JSONTokener(inputStream);
            JSONObject root = new JSONObject(tokener);

            System.out.println(root.getString("name"));

            JSONArray microservices = root.getJSONArray("microservices");


            for (int i = 0; i < microservices.length(); i++) {

                IServiceDescriptor serviceDescriptor = new ServiceDescriptor();
                JSONObject microservice = microservices.getJSONObject(i);
                String serviceName = microservice.getString("name");
                JSONArray controllers = microservice.getJSONArray("controllers");

                serviceDescriptor.setServiceName(serviceName);

                for (int j = 0; j < controllers.length(); j++) {
                    JSONObject controller = controllers.getJSONObject(j);
                    JSONArray methods = controller.getJSONArray("methods");

                    List<Operation> operations = new ArrayList<>();

                    for (int k = 0; k < methods.length(); k++) {
                        JSONObject method = methods.getJSONObject(k);
                        Operation operation = new Operation();
                        String operationName = microservice.getString("name") + "::" + method.getString("name");
                        operation.setResponseType(method.getString("returnType"));
                        operation.setName(operationName);

                        JSONArray parameters = method.getJSONArray("parameters");
                        List<Parameter> paramList = new ArrayList<>();
                        for (int l = 0; l < parameters.length(); l++) {
                            JSONObject parameter = parameters.getJSONObject(l);
                            Parameter param = new Parameter();
                            param.setName(parameter.getString("name"));
                            param.setType(parameter.getString("type"));
                            paramList.add(param);
                        }
                        operation.setParamList(paramList);

                        List<String> usingTypes = new ArrayList<>();
                        // Assume annotations can imply using types
                        JSONArray annotations = method.getJSONArray("annotations");
                        for (int m = 0; m < annotations.length(); m++) {
                            JSONObject annotation = annotations.getJSONObject(m);
                            usingTypes.add(annotation.getString("name") + " - " + annotation.getString("contents"));
                        }
                        operation.setUsingTypesList(usingTypes);

                        operations.add(operation);
                    }

                    serviceDescriptor.setServiceOperations(operations);
                }

                List<MetricResult> metricResults = new MetricCalculator().assess(serviceDescriptor);
                System.out.println(metricResults);

            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
package edu.university.ecs.lab.common.models;

import com.google.gson.JsonObject;
import edu.university.ecs.lab.common.models.enums.HttpMethod;
import lombok.Data;


/**
 * Represents an extension of a method call. A rest call exists at the service level and represents
 * a call to an endpoint mapping.
 */
@Data
public class RestCall extends MethodCall {

    /**
     * The URL of the rest call e.g. /api/v1/users/login, May have dynamic parameters
     * which are converted to {?}
     */
    private String url;

    /**
     * The httpMethod of the api endpoint e.g. GET, POST, PUT see semantics.models.enums.httpMethod
     */
    private HttpMethod httpMethod;

    private String microserviceName;

    public RestCall(String methodName, String objectName, String calledFrom, String parameterContents) {
        super(methodName, objectName, calledFrom, parameterContents);
    }

    public RestCall(MethodCall methodCall, String url, HttpMethod httpMethod, String microserviceName) {
        super(methodCall.getMethodName(), methodCall.getObjectName(), methodCall.getCalledFrom(), methodCall.getParameterContents());
        this.url = url;
        this.httpMethod = httpMethod;
        this.microserviceName = microserviceName;
    }

    public JsonObject toJsonObject() {
        JsonObject jsonObject = super.toJsonObject();

        jsonObject.addProperty("url", url);
        jsonObject.addProperty("httpMethod", httpMethod.name());
        jsonObject.addProperty("microserviceName", microserviceName);

        return jsonObject;
    }

}

package specs;

import helpers.CustomAllureListener;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

public class SpecSpecs {

    public static RequestSpecification requestSpec = new RequestSpecBuilder()
            .addFilter(CustomAllureListener.withCustomTemplates())
            .setContentType(ContentType.JSON)
            .addHeader("x-api-key", "reqres-free-v1")
            .build();
}

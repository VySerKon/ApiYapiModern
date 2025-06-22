import io.restassured.http.ContentType;
import models.UserData;
import models.UserRequest;
import models.UserResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


public class ApiYapiTest extends ApiTestBase {


    static Stream<Arguments> dataUsers() {
        return Stream.of(
                Arguments.of(7, "Michael"),
                Arguments.of(8, "Lindsay"),
                Arguments.of(9, "Tobias"),
                Arguments.of(10, "Byron"),
                Arguments.of(11, "George"),
                Arguments.of(12, "Rachel")
        );
    }
    @ParameterizedTest
    @MethodSource("dataUsers")
    @DisplayName("Проверка полного соответствия имён и идентификаторов пользователей")
    void findNameForAllUsers(int userId, String hisName) {
        List<UserData> users = given()
                .queryParam("page", 2)
                .when()
                .get("/users")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getList("data", UserData.class);

        UserData foundUser = users.stream()
                .filter(user -> user.getId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Пользователь не найден!"));
        assertEquals(hisName, foundUser.getFirst_name());
    }

    @Test
    @DisplayName("Проверка корректного сохранения параметров пользователя при создании")
    void checkUserCreation() {
        UserRequest userRequest = UserRequest.builder()
                .name("Shiza")
                .job("Superman")
                .build();
        UserResponse response = given()
                .contentType(ContentType.JSON)
                .header("x-api-key", "reqres-free-v1")
                .body(userRequest)
                .when()
                .post("/users")
                .then()
                .statusCode(201)
                .extract()
                .as(UserResponse.class);
                assertEquals("Shiza", response.getName());
                assertEquals("Superman", response.getJob());
                assertNotNull(response.getId());
                assertNotNull(response.getCreatedAt());
    }

    private int userId;

    @BeforeEach
    void setUp() {
        UserRequest userRequest = UserRequest.builder()
                .name("Shiza")
                .job("Superman")
                .build();
        UserResponse response = given()
                .log().all()
                .contentType(ContentType.JSON)
                .header("x-api-key", "reqres-free-v1")
                .body(userRequest)
                .when()
                .post("/users")
                .then()
                .log().all()
                .extract().
                as(UserResponse.class);
        userId = Integer.parseInt(response.getId());
        System.out.println("Получаем ID: " + userId);
    }

    @Test
    @DisplayName("Обновление пользователя через PUT (с проверкой ответа)")
    void updateUser_PutRequest_ReturnsUpdatedData() {
        UserRequest updatedUser = UserRequest.builder()
                .name("Shiza UPDATED")
                .job("Batman")
                .build();
        UserResponse response = given()
                .log().all()
                .contentType(ContentType.JSON)
                .header("x-api-key", "reqres-free-v1")
                .body(updatedUser)
                .when()
                .put("/users/" + userId)
                .then()
                .log().all()
                .statusCode(200)
                .extract()
                .as(UserResponse.class);
                assertEquals("Shiza UPDATED", response.getName());
                assertEquals("Batman", response.getJob());
                assertNotNull(response.getUpdatedAt());
    }

    @AfterEach
    void tearDown() {
        given()
                .log().all()
                .header("x-api-key", "reqres-free-v1")
                .when()
                .delete("/users/" + userId)
                .then()
                .log().all()
                .statusCode(204);
    }
}




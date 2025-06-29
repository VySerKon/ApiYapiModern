
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
import specs.SpecSpecs;

import java.util.List;
import java.util.stream.Stream;

import static io.qameta.allure.Allure.step;
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
        List<UserData> users = step("Получаем список пользователей", () ->
                given()
                .spec(SpecSpecs.requestSpec)
                .queryParam("page", 2)
                .when()
                .get("/users")
                .then()
                .spec(SpecSpecs.response200)
                .extract()
                .jsonPath()
                .getList("data", UserData.class)
        );

        UserData foundUser = step("Ищем пользователя по ID: " + userId, () ->
                users.stream()
                .filter(user -> user.getId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Пользователь не найден!"))
        );
        step("Проверяем имя пользователя", () -> assertEquals(hisName, foundUser.getFirst_name()));
    }

    @Test
    @DisplayName("Проверка корректного сохранения параметров пользователя при создании")
    void checkUserCreation() {
        UserRequest userRequest = step("Создаём тестового пользователя", () ->
                UserRequest.builder()
                .name("Shiza")
                .job("Superman")
                .build());
        UserResponse response = step("Отправляем POST-запрос", () ->
                given()
                .spec(SpecSpecs.requestSpec)
                .body(userRequest)
                .when()
                .post("/users")
                .then()
                .spec(SpecSpecs.response201)
                .extract()
                .as(UserResponse.class));
        step("Проверяем ответ", () -> {
            assertEquals("Shiza", response.getName());
            assertEquals("Superman", response.getJob());
            assertNotNull(response.getId());
            assertNotNull(response.getCreatedAt());
        });
    }

    private int userId;

    @BeforeEach
    void setUp() {
        UserRequest userRequest = step("Подготавливаем тестовые данные", () ->
                UserRequest.builder()
                .name("Shiza")
                .job("Superman")
                .build());
        UserResponse response = step("Создаём пользователя", () ->
                given()
                .spec(SpecSpecs.requestSpec)
                .log().all()
                .body(userRequest)
                .when()
                .post("/users")
                .then()
                .log().all()
                .extract().
                as(UserResponse.class));
        userId = step("Сохраняем ID пользователя", () ->
                Integer.parseInt(response.getId()));
    }

    @Test
    @DisplayName("Обновление пользователя через PUT (с проверкой ответа)")
    void updateUser_PutRequest_ReturnsUpdatedData() {
        UserRequest updatedUser = step("Подготавливаем данные для обновления", () ->
                UserRequest.builder()
                .name("Shiza UPDATED")
                .job("Batman")
                .build());
        UserResponse response = step("Отправляем PUT-запрос", () ->
                given()
                .spec(SpecSpecs.requestSpec)
                .log().all()
                .body(updatedUser)
                .when()
                .put("/users/" + userId)
                .then()
                .spec(SpecSpecs.response200)
                .extract()
                .as(UserResponse.class));
                step("Проверяем ответ", () -> {
                    assertEquals("Shiza UPDATED", response.getName());
                    assertEquals("Batman", response.getJob());
                    assertNotNull(response.getUpdatedAt());
                });
    }

    @AfterEach
    void tearDown() {
                step("Удаляем пользователя с ID: " + userId, () -> {
                    given()
                            .spec(SpecSpecs.requestSpec)
                            .log().all()
                            .when()
                            .delete("/users/" + userId)
                            .then()
                            .spec(SpecSpecs.response204);
                });
    }
}




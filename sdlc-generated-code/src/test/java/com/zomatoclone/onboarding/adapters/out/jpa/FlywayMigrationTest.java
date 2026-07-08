package com.zomatoclone.onboarding.adapters.out.jpa;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Set;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class FlywayMigrationTest {

  @Container
  static PostgreSQLContainer<?> postgres =
      new PostgreSQLContainer<>("postgres:16-alpine").withReuse(true);

  @Test
  void v1MigrationCreatesRestaurantTablesAndIndex() throws Exception {
    Flyway flyway =
        Flyway.configure()
            .dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
            .locations("classpath:db/migration")
            .cleanDisabled(false)
            .load();

    flyway.clean();
    flyway.migrate();

    try (Connection conn =
        java.sql.DriverManager.getConnection(
            postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())) {
      DatabaseMetaData meta = conn.getMetaData();

      Set<String> tables = new HashSet<>();
      try (ResultSet rs = meta.getTables(null, "public", "%", new String[] {"TABLE"})) {
        while (rs.next()) {
          tables.add(rs.getString("TABLE_NAME"));
        }
      }
      assertThat(tables).contains("restaurants", "restaurant_cuisines");

      Set<String> restaurantColumns = new HashSet<>();
      try (ResultSet rs = meta.getColumns(null, "public", "restaurants", "%")) {
        while (rs.next()) {
          restaurantColumns.add(rs.getString("COLUMN_NAME"));
        }
      }
      assertThat(restaurantColumns)
          .containsExactlyInAnyOrder(
              "id",
              "owner_id",
              "name",
              "description",
              "address_line1",
              "address_line2",
              "city",
              "postal_code",
              "phone",
              "email",
              "opening_hours",
              "status",
              "created_at",
              "updated_at");

      Set<String> cuisineColumns = new HashSet<>();
      try (ResultSet rs = meta.getColumns(null, "public", "restaurant_cuisines", "%")) {
        while (rs.next()) {
          cuisineColumns.add(rs.getString("COLUMN_NAME"));
        }
      }
      assertThat(cuisineColumns).containsExactlyInAnyOrder("restaurant_id", "cuisine");

      Set<String> indexes = new HashSet<>();
      try (ResultSet rs = meta.getIndexInfo(null, "public", "restaurants", false, false)) {
        while (rs.next()) {
          String indexName = rs.getString("INDEX_NAME");
          if (indexName != null) {
            indexes.add(indexName);
          }
        }
      }
      assertThat(indexes).contains("idx_restaurants_list");
    }
  }
}

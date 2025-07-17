resource "google_cloud_run_service" "spring_backend" {
  name     = "grant-search-backend"
  location = var.region
  template {
    spec {
      containers {
        image = var.spring_image
        env {
          name  = "SPRING_DATASOURCE_URL"
          value = "jdbc:postgresql://${google_sql_database_instance.grants_db.connection_name}/grantsearch"
        }
        env {
          name  = "GRANT_MATCH_SERVICE_URL"
          value = var.grant_match_service_url
        }
      }
    }
  }
  traffic { percent = 100; latest_revision = true }
}

resource "google_cloud_run_service" "grant_match" {
  name     = "grant-match"
  location = var.region
  template {
    spec { containers { image = var.grant_match_image } }
  }
  traffic { percent = 100; latest_revision = true }
}

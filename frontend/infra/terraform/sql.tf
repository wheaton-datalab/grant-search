resource "google_sql_database_instance" "grants_db" {
  name             = "grants-db"
  database_version = "POSTGRES_15"
  region           = var.region
  settings { tier = "db-f1-micro" }
}

resource "google_sql_database" "app_db" {
  name     = "grantsearch"
  instance = google_sql_database_instance.grants_db.name
}
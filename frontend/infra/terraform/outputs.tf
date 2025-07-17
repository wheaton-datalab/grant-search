output "spring_backend_url" {
  description = "URL of the Spring Boot Cloud Run service"
  value       = google_cloud_run_service.spring_backend.status[0].url
}

output "grant_match_url" {
  description = "URL of the Grant-Match Cloud Run service"
  value       = google_cloud_run_service.grant_match.status[0].url
}

output "sql_instance_connection" {
  description = "Cloud SQL connection name"
  value       = google_sql_database_instance.grants_db.connection_name
}

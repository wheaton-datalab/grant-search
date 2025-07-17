resource "google_cloud_run_service_iam_member" "spring_invoker" {
  service  = google_cloud_run_service.spring_backend.name
  location = var.region
  role     = "roles/run.invoker"
  member   = "allUsers"
}

resource "google_cloud_run_service_iam_member" "match_invoker" {
  service  = google_cloud_run_service.grant_match.name
  location = var.region
  role     = "roles/run.invoker"
  member   = "allUsers"
}
resource "google_pubsub_topic" "refresh_grants" {
  name = "refresh-grants-topic"
}

resource "google_cloud_scheduler_job" "weekly_refresh" {
  name        = "weekly-refresh-grants"
  description = "Weekly ingestion trigger"
  schedule    = "0 4 * * MON"  # every Monday 04:00 UTC
  time_zone   = "UTC"
  pubsub_target {
    topic_name = google_pubsub_topic.refresh_grants.id
    data       = base64encode("{\"action\":\"refresh\"}")
  }
}

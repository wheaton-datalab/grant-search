resource "google_cloudfunctions2_function" "grant_ingester" {
  name        = "grant-ingester"
  location    = var.region
  build_config {
    runtime = "python312"
    # source: specify your source or containerImage
  }
  event_trigger {
    event_type   = "google.cloud.pubsub.topic.v1.messagePublished"
    pubsub_topic = google_pubsub_topic.refresh_grants.id
  }
}
variable "project_id" {
  description = "GCP project ID"
}

variable "region" {
  description = "GCP region"
  default     = "us-central1"
}

variable "spring_image" {
  description = "Container image URL for Spring Boot backend"
}

variable "grant_match_image" {
  description = "Container image URL for Grant-Match Python service"
}

variable "grant_match_service_url" {
  description = "URL for the Grant-Match service used by Spring"
}

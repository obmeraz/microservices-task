Feature: End-to-end resource upload and processing

  Scenario: Uploaded MP3 is processed and metadata appears in Song Service
    Given the microservices stack is available
    When I upload a valid MP3 file to Resource Service
    Then Song Service eventually returns metadata for the uploaded resource

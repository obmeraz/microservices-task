Feature: Asynchronous resource processing pipeline

  Scenario: Successfully process resource uploaded event
    Given Resource Service holds resource data for id "1"
    When a resource uploaded event with id "1" arrives on the queue
    Then Resource Processor should fetch the resource from Resource Service
    And Resource Processor should store extracted metadata in Song Service

Feature: Resume Upload on Naukri
  As a job seeker
  I want to upload my resume through my profile
  So that recruiters see my latest resume

  Background: User is logged into Naukri
    Given I am logged into Naukri

  @regression @Job_01
  Scenario: Upload new resume through profile section -
    When I navigate to my profile
    And I go to the resume section
    And I delete the old resume
    And I upload my resume
    And I save the resume
    And I refresh the page
    Then the resume upload should be successful
    When I logout of Naukri
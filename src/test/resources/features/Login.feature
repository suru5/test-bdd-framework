@smoke
Feature: Login Functionality on SauceDemo
  
  Background:
    Given the user navigates to the login page

  @smoke @positive
  Scenario: Successful login with valid credentials
    When the user logs in with valid credentials
    Then the login should be successful

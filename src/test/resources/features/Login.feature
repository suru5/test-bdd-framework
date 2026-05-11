@smoke
Feature: Login Functionality on saucedemo
  As a customer on https://www.saucedemo.com
  I want to log in with valid credentials
  So that I can access the shop

  Background:
    Given the user navigates to the login page

  @smoke @positive
  Scenario: Successful login with valid credentials
    When the user enters email "standard_user"
    And the user enters the login password "secret_sauce"
    And the user clicks the sign in button
    Then the login should be successful

# Code review requirements

## Contents
1. [Introduction](#1-introduction)
2. [Code Review Process](#2-code-review-process)
3. [Requirements](#3-requirements)
4. [Check list](#4-check-list)

## 1. Introduction
Code review is a process in which one or more developers are systematically tasked with reviewing the code written by another developer in order to find defects and improve it. Code review should be done by project maintainers considering code quality and safety, sharing best practices and this leads to better collaboration, creating a culture of review, building team confidence in the code.

## 2. Code Review Process

1. **Initiating a Code Review:**
   - All code changes must undergo a review before merging into the main branch.
   - The developer initiates a code review by creating a pull request.
2. **Assigning Reviewers:**
   - The pull request is automatically assigned to maintainers for review.
   - Other team members should be added manually for review.
3. **Reviewing Code:**
   - Reviewers are responsible for thoroughly examining the code changes.
   - Check for adherence to coding standards, best practices, and project guidelines.
4. **Providing Constructive Feedback:**
   - Reviewers must provide clear and constructive feedback on identified issues or improvements.
   - Discussions within the pull request are encouraged to ensure a shared understanding.

## 3. Requirements

  * Review fewer than 400 lines of code at a time.
  * Take your time. Inspection rates should under 500 LOC per hour.
  * Do not review for more than 60 minutes at a time.
  * Set goals and capture metrics.
  * Authors should annotate source code before the review.
  * Use checklists.
  * Establish a process for fixing defects found.
  * Foster a positive code review culture.
  * Embrace the subconscious implications of peer review.
  * Practice lightweight code reviews.
  * Code changes must receive approval from at least two team members before merging.
  * **At least one maintainer's approval is required.**
  * Critical issues raised during the review must be addressed before approval.
  * Non-blocking feedback should be addressed in subsequent iterations.
  * Pull requests should be kept focused on specific features or fixes.
  * Continuous improvement in code quality is encouraged.


## 4. Check list
- [ ] **Coding Standards:**
  - Ensure the code follows the established coding standards.

- [ ] **Functionality:**
  - Verify that the code changes address the intended functionality or issue.

- [ ] **Edge Cases:**
  - Check for handling of edge cases and potential error scenarios.

- [ ] **Testing:**
  - Confirm that appropriate tests have been added or updated.

- [ ] **Documentation:**
  - Validate that code changes are well-documented, including inline comments and README updates.

- [ ] **Security:**
  - Assess the code for potential security vulnerabilities.
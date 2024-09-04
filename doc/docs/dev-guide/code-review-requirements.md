# Code review requirements

How to conduct code reviews in the LPVS project.

---

## Introduction
Code review is a process in which one or more developers are systematically tasked with reviewing 
the code written by another developer in order to find defects and improve it. Code review should 
be done by project maintainers considering code quality and safety, sharing best practices and this 
leads to better collaboration, creating a culture of review, building team confidence in the code.

---

## Code Review Process

**Initiating a Code Review**

   - All code changes must undergo a review before merging into the main branch.
   - The developer initiates a code review by creating a pull request.

**Assigning Reviewers**

   - The pull request is automatically assigned to maintainers for review.
   - Other team members should be added manually for review.

**Reviewing Code**

   - Reviewers are responsible for thoroughly examining the code changes.
   - Check for adherence to coding standards, best practices, and project guidelines.

**Providing Constructive Feedback**

   - Reviewers must provide clear and constructive feedback on identified issues or improvements.
   - Discussions within the pull request are encouraged to ensure a shared understanding.

---

## Requirements

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

---

## Self-verification Checklist

- [ ] **Coding Standards**: ensure the code follows the established coding standards.
- [ ] **Functionality**: verify that the code changes address the intended functionality or issue.
- [ ] **Edge Cases**: check for handling of edge cases and potential error scenarios.
- [ ] **Testing**: confirm that appropriate tests have been added or updated.
- [ ] **Documentation**: validate that code changes are well-documented, including inline comments and README updates.
- [ ] **Security**: assess the code for potential security vulnerabilities.
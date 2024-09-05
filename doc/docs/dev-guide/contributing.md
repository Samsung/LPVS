# Contributing

An introduction to contributing to the LPVS project.

---

We welcome contributions! By contributing, you can help improve this project and make it better for everyone.

## Ways to Contribute

Here are some ways you can contribute to this project:

- **Report Bugs & Request Features** 🐛💡: Let us know if you find any bugs or have ideas
  for new features by [creating an issue].
- **Vulnerability Reporting** 🚨: If you discover a vulnerability in our project, please report it according to 
  the [Security guideline].
- **Code Contributions** 🛠️: Fix bugs, add new features, or improve the existing code.
- **Documentation** 📝: Improve our documentation by correcting typos, clarifying instructions,
  or adding new content.
- **Code Reviews** 🔍: Review other contributors' [pull requests] to maintain code quality.

---

## Code Contribution

If you want to contribute to the LPVS project and make it better, your help is very welcome. 
Contributing is also a great way to learn more about social coding on GitHub, new technologies, 
and how to make constructive bug reports, feature requests, and the noblest of all contributions: 
a good, clean pull request.

!!! note

    You can use templates to create a description of your pull request or issue, which will greatly 
    simplify the LPVS team's work on reviewing and incorporating your code. However, using these 
    templates is not mandatory, and we will always welcome any help.

To make a clean pull request, follow the steps below:

* [Fork](http://help.github.com/fork-a-repo/) the LPVS repository on GitHub and clone your fork 
to your development environment.

```bash
git clone https://github.com/YOUR-GITHUB-USERNAME/LPVS.git
```

!!! warning

      If you have trouble setting up Git with GitHub in Linux or are getting errors like `Permission 
      Denied (publickey)`, you must [set up your Git installation to work with GitHub](http://help.github.com/linux-set-up-git/).

* Add the main LPVS repository as an additional Git remote called `upstream`.

```bash
git remote add upstream https://github.com/samsung/lpvs
```

* Make sure there is an issue created for the task you are working on. All new features and bug 
fixes should have an associated issue to provide a single point of reference for discussion and documentation. 
If the issue already exists, leave a comment on that issue indicating that you intend to work on it. 
If it doesn't exist, open a new issue for your task. 

!!! note

      For small changes or documentation issues, creating an issue is not necessary, 
      and a pull request is sufficient.

* Fetch the latest code from the main LPVS branch.

```bash
git fetch upstream
```

!!! warning

      You should start at this point for every new contribution to make sure you are working 
      on the latest code.

* Create a new branch for your feature based on the current LPVS main branch. Each separate bug fix 
or feature addition should have its own branch. Branch names should be descriptive and start with 
the number of the corresponding issue, if applicable. If you're not fixing a specific issue, you 
can skip the number. 

```bash
git checkout upstream/<NAMED_RELEASE>
git checkout -b 999-name-of-your-branch-goes-here
```

!!! info

    Above, <NAMED_RELEASE> can be '1.0.0', etc. - see the list of releases or `main` branch.

* Write your code and make the necessary changes.

  - Follow the coding conventions and style guidelines used in the LPVS project.
  - Write clear, concise, and well-documented code.
  - Include unit tests to ensure the correctness of your code.
  - If you're adding a new feature, consider updating the relevant documentation and examples.
  - If you're fixing a bug, provide a clear explanation of the issue and how your code resolves it.

* Commit your changes with a descriptive commit message. Make sure to mention the issue number 
with `#XXX` so that GitHub will automatically link your commit with the issue. Additionally, use 
appropriate commit message prefixes to categorize your changes.

    - **feat**: Introduce a new feature or enhancement.
    - **fix**: Address a bug or resolve an issue.
    - **chore**: Perform routine tasks or maintenance.
    - **docs**: Make changes to documentation.
    - **style**: Implement code style changes (e.g., formatting).
    - **test**: Modify or add tests.
    - **refactor**: Implement code refactoring.
    - **perf**: Performance Improvements.
    - **build**: Any changes in build conditions.
    - **ci**: Implement any continuous integration changes.
    - **revert**: Revert to previous code state.

For example:

```bash
git add path/to/my/file
git commit -m "feat: A brief description of this new feature which resolves #42" --signoff
git commit -m "fix: A brief description of this bug fix which fixes #42" --signoff
git commit -m "chore: A brief description of routine tasks or maintenance" --signoff
git commit -m "docs: A brief description of documentation changes" --signoff
git commit -m "style: A brief description of code style changes (e.g., formatting)" --signoff
git commit -m "test: A brief description of changes related to testing" --signoff
git commit -m "refactor: A brief description of code refactoring" --signoff
git commit -m "perf: A brief description of performance improvements" --signoff
git commit -m "build: A brief description of build conditions" --signoff
git commit -m "ci: A brief description of continuous integration changes" --signoff
git commit -m "revert: A brief description of revert previous code state" --signoff
```

* Pull the latest LPVS code from upstream into your branch.

```bash
git rebase upstream/main
```

* Push your code to your forked repository.

```bash
git push -u origin my-feature
```

!!! note

	`-u` parameter ensures that your branch will now automatically push and pull from the GitHub branch. That means if you type `git push` next time, it will know where to push to.

* Open a pull request against the upstream repository. Go to your repository on GitHub and click `Pull Request`. 
Choose your branch on the right and enter some more details in the comment box. To link the pull request 
to the issue, include `#999` in the pull request comment, where 999 is the issue number.

!!! info

    Each pull-request should fix a single change.

* Someone from the LPVS team will review your code, and you might be asked to make some changes. If requested, 
make the necessary changes and push them to your branch. The pull request will be updated automatically.

* Once your code is accepted, it will be merged into the main branch and become part of the next LPVS release. 
If your code is not accepted, don't be discouraged. LPVS aims to meet specific requirements and priorities, 
and your contribution will still be available on GitHub as a reference for others.

* After your contribution is merged or declined, you can delete the branch you've worked on from your local 
repository and your forked repository:

```bash
git checkout main
git branch -D my-feature
git push origin --delete my-feature
```

---

## Code of Conduct

Everyone interacting in the LPVS project's codebases and issue trackers is expected to follow the [Code of Conduct].

[Code of Conduct]: https://github.com/Samsung/LPVS?tab=coc-ov-file
[creating an issue]: https://github.com/Samsung/LPVS/issues/new/choose
[Security guideline]: https://github.com/Samsung/LPVS?tab=security-ov-file
[pull requests]: https://github.com/Samsung/LPVS/pulls
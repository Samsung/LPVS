## Contributing

If you want to contribute to the LPVS project and make it better, your help is very welcome. Contributing is also a great way to learn more about social coding on GitHub, new technologies, and how to make constructive bug reports, feature requests, and the noblest of all contributions: a good, clean pull request.

You can use templates to create a description of your [pull request](PULL_REQUEST_TEMPLATE.md) or [issue](ISSUE_TEMPLATE.md), which will greatly simplify the LPVS team's work on reviewing and incorporating your code. However, using these templates is not mandatory, and we will always welcome any help.

To make a clean pull request, follow the steps below:

1. [Fork](http://help.github.com/fork-a-repo/) the LPVS repository on GitHub and clone your fork to your development environment:
   ```sh
   git clone https://github.com/YOUR-GITHUB-USERNAME/LPVS.git
   ```
   If you have trouble setting up Git with GitHub in Linux or are getting errors like "Permission Denied (publickey)", you must [set up your Git installation to work with GitHub](http://help.github.com/linux-set-up-git/).

2. Add the main LPVS repository as an additional Git remote called "upstream":
   ```sh
   git remote add upstream https://github.com/samsung/lpvs
   ```

3. Make sure there is an issue created for the task you are working on. All new features and bug fixes should have an associated issue to provide a single point of reference for discussion and documentation. If the issue already exists, leave a comment on that issue indicating that you intend to work on it. If it doesn't exist, open a new issue for your task. 

    > For small changes or documentation issues, creating an issue is not necessary, and a pull request is sufficient.

4. Fetch the latest code from the main LPVS branch:
   ```sh
   git fetch upstream
   ```
    You should start at this point for every new contribution to make sure you are working on the latest code.

5. Create a new branch for your feature based on the current LPVS main branch:

    Each separate bug fix or feature addition should have its own branch. Branch names should be descriptive and start with the number of the corresponding issue, if applicable. If you're not fixing a specific issue, you can skip the number. 
	```sh
	git checkout upstream/<NAMED_RELEASE>
	git checkout -b 999-name-of-your-branch-goes-here
	```
    Above, <NAMED_RELEASE> can be '1.0.0', etc. - see the list of releases or `main` branch.

6. Write your code and make the necessary changes.
   - Follow the coding conventions and style guidelines used in the LPVS project.
   - Write clear, concise, and well-documented code.
   - Include unit tests to ensure the correctness of your code.
   - If you're adding a new feature, consider updating the relevant documentation and examples.
   - If you're fixing a bug, provide a clear explanation of the issue and how your code resolves it.

    Feel free to reach out if you have any further questions or need additional assistance!

7. Commit your changes with a descriptive commit message. Make sure to mention the issue number with `#XXX` so that GitHub will automatically link your commit with the issue. Additionally, use appropriate commit message prefixes to categorize your changes.

   - **feat**: Introduce a new feature or enhancement.
   - **fix**: Address a bug or resolve an issue.
   - **chore**: Perform routine tasks or maintenance.
   - **docs**: Make changes to documentation.
   - **style**: Implement code style changes (e.g., formatting).
   - **test**: Modify or add tests.

   For example:

   ```sh
   git add path/to/my/file
   git commit -m "feat: A brief description of this new feature which resolves #42" --signoff
   git commit -m "fix: A brief description of this bug fix which fixes #42" --signoff
   git commit -m "chore: A brief description of routine tasks or maintenance" --signoff
   git commit -m "docs: A brief description of documentation changes" --signoff
   git commit -m "style: A brief description of code style changes (e.g., formatting)" --signoff
   git commit -m "test: A brief description of changes related to testing" --signoff
   ```

8. Pull the latest LPVS code from upstream into your branch:
	```sh
	git rebase upstream/main
	```

9. Push your code to your forked repository:
	```sh
	git push -u origin my-feature
	```
	`-u` parameter ensures that your branch will now automatically push and pull from the GitHub branch. That means if you type `git push` next time, it will know where to push to.

10. Open a pull request against the upstream repository. Go to your repository on GitHub and click "Pull Request". Choose your branch on the right and enter some more details in the comment box. To link the pull request to the issue, include `#999` in the pull request comment, where 999 is the issue number.
	> Note that each pull-request should fix a single change.

11. Someone from the LPVS team will review your code, and you might be asked to make some changes. If requested, make the necessary changes and push them to your branch. The pull request will be updated automatically.

12. Once your code is accepted, it will be merged into the main branch and become part of the next LPVS release. If your code is not accepted, don't be discouraged. LPVS aims to meet specific requirements and priorities, and your contribution will still be available on GitHub as a reference for others.

13. After your contribution is merged or declined, you can delete the branch you've worked on from your local repository and your forked repository:
	```sh
	git checkout main
	git branch -D my-feature
	git push origin --delete my-feature
	```

Thank you for your contribution to LPVS!
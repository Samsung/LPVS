# Coding style

Explanation of the used coding style.

---

The LPVS project uses the [Google Java Style](https://google.github.io/styleguide/javaguide.html).

Each Pull Request is checked for compliance with the coding style. If a conflict occurs, a recommendation can be found in the [Check Java Format action](https://github.com/Samsung/LPVS/actions/workflows/java-format-checker.yml).

Example:

```bash
diff --git a/src/main/java/com/lpvs/service/LPVSDetectService.java b/src/main/java/com/lpvs/service/LPVSDetectService.java
index 360e21a..880dd2a 100644
--- a/src/main/java/com/lpvs/service/LPVSDetectService.java
+++ b/src/main/java/com/lpvs/service/LPVSDetectService.java
@@ -84,7 +84,9 @@ public class LPVSDetectService {
                         this.getInternalQueueByPullRequest(HtmlUtils.htmlEscape(trigger));
 
                 List<LPVSFile> scanResult =
-                        this.runScan(webhookConfig, LPVSDetectService.getPathByPullRequest(webhookConfig));
+                        this.runScan(
+                                webhookConfig,
+                                LPVSDetectService.getPathByPullRequest(webhookConfig));
 
                 List<LPVSLicenseService.Conflict<String, String>> detectedConflicts =
                         licenseService.findConflicts(webhookConfig, scanResult);
Error: Process completed with exit code 1.
```

When preparing a Pull Request, you can run a command that will check and correct the coding style.

```bash
java -jar google-java-format-1.23.0-all-deps.jar --aosp --skip-javadoc-formatting --skip-reflowing-long-strings --skip-sorting-imports --replace -i $(git ls-files|grep \.java$)
```

!!! note

    Download `google-java-format-1.23.0-all-deps.jar` [here](https://github.com/google/google-java-format/releases/download/v1.23.0/google-java-format-1.23.0-all-deps.jar).

!!! info

    `google-java-format` official repository [link](https://github.com/google/google-java-format).

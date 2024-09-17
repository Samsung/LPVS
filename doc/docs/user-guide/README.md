# User Guide

How to configure and run LPVS.

---

The LPVS User Guide provides documentation for users on how to configure and run LPVS.
You can jump directly to the pages listed below, or use the *next* and *previous* buttons
in the navigation bar at the top of the page to move through the documentation in order.

There are 2 modes of LPVS operation:
- Service mode - LPVS works as a service that is started by the system and runs continuously.
- Command line mode - LPVS is executed from the command line and runs until it completes its task.

## Service mode

- [GitHub webhook configuration](service/webhook.md)
- [Run service using pre-built Docker image](service/docker.md)
- [Scanner installation](service/scanner.md)
- [Database configuration](service/database.md)
- [Build and run service from the source code](service/build-and-run.md)

## Command line mode

- [Single scan of the pull request](cli/cli_pr.md)
- [Single scan of local files](cli/cli_file.md)

## Configuration

- [Configuration options](config/options.md)
- [Database customization](config/database.md)

## Performance monitoring
- [Tools](performance.md)

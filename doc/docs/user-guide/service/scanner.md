# Scanner installation

How to install scan engine ScanOSS.

---

The ScanOSS Python package provides a simple easy to consume library for interacting with ScanOSS APIs/Engine.

To install ScanOSS and dependencies, use the command:

```bash
pip3 install --require-hashes -r requirements.txt
```

!!! info

    File `requirements.txt` could be found at the [root folder](https://github.com/Samsung/LPVS/blob/main/requirements.txt) 
    of the LPVS repository.

Make sure that the `PATH` variable is added to the environment:

```bash
export PATH="$HOME/.local/bin:$PATH"
```

For more details, please refer to the [official guideline](https://github.com/scanoss/scanoss.py#installation).

!!! warning

    If installing on Ubuntu 2023.04, Fedora 38, Debian 11, etc. a few additional steps are required before 
    installing ScanOSS. More details can be found [here](https://itsfoss.com/externally-managed-environment/).

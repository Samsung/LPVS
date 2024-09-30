# FAQ

Frequently asked questions and useful information.

---

## Useful utilities

### Swagger

* [Swagger](https://swagger.io/)
* [Swagger Editor](https://editor.swagger.io/)

### PlantUML

* [PlantUML Language Reference Guide](http://plantuml.com/guide)
* [PlantText - Online Editor](https://www.planttext.com/)

---

## How to sign a release using GPG?

A good example of a release signature can be found on the [link](https://wiki.debian.org/Creating%20signed%20GitHub%20releases). However, 5 and 6 items are described separately.

### Creating a key pair

```bash
gpg --full-generate-key
```

During the execution of this command, you will need to enter additional data:

* Please select what kind of key you want: **1**
* What keysize do you want? (3072): **4096**
* Please specify how long the key should be valid: **0**
* Key is valid for? (0): **0**
* Is this correct? (y/N): **y**
* Real name: **LPVS**
* Email address: **john.doe@example.com**
* Comment: **Keys for LPVS**

In this case, the result will be the next

```bash
gpg (GnuPG) 2.2.4; Copyright (C) 2017 Free Software Foundation, Inc.
This is free software: you are free to change and redistribute it.
There is NO WARRANTY, to the extent permitted by law.

Please select what kind of key you want:
   (1) RSA and RSA (default)
   (2) DSA and Elgamal
   (3) DSA (sign only)
   (4) RSA (sign only)
Your selection? 1
RSA keys may be between 1024 and 4096 bits long.
What keysize do you want? (3072) 4096
Requested keysize is 4096 bits
Please specify how long the key should be valid.
         0 = key does not expire
      <n>  = key expires in n days
      <n>w = key expires in n weeks
      <n>m = key expires in n months
      <n>y = key expires in n years
Key is valid for? (0) 0
Key does not expire at all
Is this correct? (y/N) y

GnuPG needs to construct a user ID to identify your key.

Real name: LPVS
Email address: john.doe@example.com
Comment: Keys for LPVS
You selected this USER-ID:
    "LPVS (Keys for LPVS) <john.doe@example.com>"

Change (N)ame, (C)omment, (E)mail or (O)kay/(Q)uit? O
We need to generate a lot of random bytes. It is a good idea to perform
some other action (type on the keyboard, move the mouse, utilize the
disks) during the prime generation; this gives the random number
generator a better chance to gain enough entropy.
We need to generate a lot of random bytes. It is a good idea to perform
some other action (type on the keyboard, move the mouse, utilize the
disks) during the prime generation; this gives the random number
generator a better chance to gain enough entropy.
gpg: key BE13B1D440E813F0 marked as ultimately trusted
gpg: revocation certificate stored as '/home/virtual-pc/.gnupg/openpgp-revocs.d/D3C7C06AC34BDA9A41388E76BE13B1D440E813F0.rev'
public and secret key created and signed.

pub   rsa4096 2021-10-15 [SC]
      D3C7C06AC34BDA9A41388E76BE13B1D440E813F0
uid                      LPVS (Keys for LPVS) <john.doe@example.com>
sub   rsa4096 2021-10-15 [E]
``` 

### Create and sign a tag with your created key

Set your GPG signing key in Git

```bash
git config --global user.signingkey <key-ID>
```

Create and sign the tag

```bash
git tag -s <tag>
```

### Sign the release tarball with your created key

```bash
gpg --armor --detach-sign lpvs-vx.x.x.tar.gz
```

!!! note

    If you have multiple keys, you must specify the key that will be used by adding the option `-u <key-ID>`

###  Extraction a copy of a key pair from local gpg keyring

```bash
gpg --output lpvs-public.pgp --armor --export john.doe@example.com 
gpg --output lpvs-private.pgp --armor --export-secret-key john.doe@example.com
```

!!! note

    Only the signature file (`lpvs-vx.x.x.tar.gz.asc`) and the public key (`lpvs-public.pgp`) must be loaded as an artifact to release.

### To sign the release using another computer

For this need to download the private key (`lpvs-private.pgp`) by the next command:

```bash
gpg --import lpvs-private.pgp
```

---

## How to use GPG to verify signed release?

To perform the verification, you need the following:
* signed file – for example `lpvs-vx.x.x.tar.gz`
* signature file – accompanying file with “.asc” extension (Ex. `lpvs-vx.x.x.tar.gz.asc`)
* public key – for example `lpvs-public.pgp`

### Import the public key to your keystore

```bash
gpg --import <public key>
```

### Verification signed file

```bash
gpg --verify <signature file> <signed file>
```

!!! note

    If you have multiple keys, you must specify the key that will be used by adding the option `-u <key-ID>`

---

## DCO via the command line

The most popular way to do DCO is to sign off your username and email address in the git command line.

First, configure your local git install.

```bash
$ git config --global user.name "John Doe" 
$ git config --global user.email johndoe@example.com
```

Obviously, you should use your own name and the email address associated with your GitHub user account.

Now, every time you commit new code in git, just add a signoff statement via the `-s` flag.

```bash
$ git commit -s -m "This is my commit message"
```

That’s it. Git adds your sign-off message in the commit message, and you contribution (commit) is now DCO compliant.

---

## How to generate Python requirements file with hashes?

To generate a Python `requirements.txt` file with hashes, which ensures that the same versions of packages are installed across different environments, you can use the `pip-compile` tool from the `pip-tools` package. Here's a step-by-step guide on how to achieve this:

### Steps:

1. **Install pip-tools**

First, install `pip-tools` to manage your `requirements.txt` and add hashes.

``` bash
pip install pip-tools
```

2. **Create `requirements.in` file:**

Add your packages to a `requirements.in` file. This file will be used as input to generate the final `requirements.txt` file with hashes.
In case you need to use exact version of a package, you can specify it in this file.

Example `requirements.in`:

``` in
mkdocs==1.6.1
pymdown-extensions==10.9
```

3. **Compile the `requirements.txt` with hashes**

Use `pip-compile` with `--generate-hashes` flag to create a `requirements.txt` file includes secure hashes.
``` bash
pip-compile --generate-hashes
```

!!! note

    If you want to use custom names of input and output requirements file, specify them in command line like this:

    ``` bash
    pip-compile --output-file=custom-requirements.txt --generate-hashes custom-requirements.in
    ```

    - Without `--output-file`: It will always create `requirements.txt` file.
    - With `--output-file`: It will specify any custom output file name.

4. **Result**

It will generate a `requirements.txt` (or `custom-requirements.txt`) file with hashes for each package, ensuring the integrity and security of the installed packages.

Example output in `requirements.txt`:

``` txt
mkdocs==1.6.1 \
    --hash=sha256:... \
    --hash=sha256:...
...
pymdown-extensions==10.9 \
    --hash=sha256:... \
    --hash=sha256:...
...
```

# Quick Start Guide

Minimal configuration to set up and run the LPVS GitHub service locally.

---

## Setting up your project to interact with _LPVS_ service

To enable _LPVS_ license scanning for your project, you need to set up GitHub Webhooks.

* Create a personal Github access token (`personal-token`):

  - Follow the instructions [here](/doc/docs/user-guide/service/webhook.md#create-a-personal-github-access-token) to create a personal access token with the necessary permissions.

!!! warning

    Pay attention that the token must be copied immediately after creation, because you will not be able to see it later!!

* Get a personal ngrok auth token to expose your local service (`auth-token`):

  - The ngrok agent authenticates with an authtoken. Your authtoken is available on the ngrok [portal](https://dashboard.ngrok.com/get-started/your-authtoken).

---

## Using pre-built _LPVS_ Docker images

This section explains how to download and run pre-built _LPVS_ Docker image with ngrok reverse proxy.

For the Docker deployment scenario, you may need to set additional parameters described in [docker section](/doc/docs/user-guide/service/docker.md).

### Setting up _LPVS_ Docker environment variables

* Open `docker-compose-quick.yml` file.

* In the `environment` part of the `lpvs` service, find `## Github data for fetching code` and fill in the github `login` and personal `token` that was generated earlier

```yaml
- github.login=<github-login>
- github.token=<personal-token>
```

* In the `environment` part of the `ngrok` service, find `## Ngrok Auth token` and fill personal `token` from [Ngrok portal](https://dashboard.ngrok.com/get-started/your-authtoken).

```yaml
- NGROK_AUTHTOKEN=<auth-token>
```

### Running _LPVS_ and MySQL Docker images with Docker Compose

* Start the _LPVS_ services:

```bash
docker compose -f docker-compose-quick.yml up -d
```

* To stop the _LPVS_ services, use next command:

```bash
docker compose -f docker-compose-quick.yml down
```

## Setting up your project to interact with _LPVS_

Configure the [webhook](/doc/docs/user-guide/service/webhook.md#configure-the-webhook-in-your-github-repository-settings) in your GitHub repository settings

Configuration from your project side is now complete!
You can now create a new pull request or update an existing one with commits. _LPVS_ will automatically start scanning and provide comments about the licenses found in the project.

![result](../../img/webhook/result.png)
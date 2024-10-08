# GitHub webhook configuration

How to configure GitHub project for interaction with LPVS.

---

To enable LPVS license scanning for your project, you need to set up GitHub Webhooks:

## Create a personal GitHub access token

Follow the instructions [here](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/creating-a-personal-access-token#creating-a-fine-grained-personal-access-token) 
to create a personal access token (`personal-token`) with the necessary permissions.

!!! warning

    Pay attention that the token must be copied immediately after creation, 
    because you will not be able to see it later!

---

## (Optional) Configure Ngrok reverse proxy

To configure GitHub access to a personal server, you need to expose the URL to an external API. If the server has a dedicated IP address or domain, this step can be omitted.

- Install Ngrok and connect your account from [Ngrok guide](https://ngrok.com/docs/getting-started/#step-2-install-the-ngrok-agent) (follow steps 1 and 2).

- If Ngrok is included in the docker compose file, `auth-token` can be found on [Ngrok portal](https://dashboard.ngrok.com/get-started/your-authtoken).

- Run Ngrok using the command:

```bash
ngrok http 7896
```

## Configure the webhook in your GitHub repository settings

Follow the next steps:

- Go to `Settings` -> `Webhooks`.

![step1](../../img/webhook/step_1_1.png)

- Click on `Add webhook`.

![step2](../../img/webhook/step_1_2.png)

- Fill in the `Payload URL` with URL where LPVS is running.

!!! note

    If you're using Ngrok, the `Payload URL` can be found on localhost: `http://127.0.0.1:4040/`.
    
    It will look like `https://50be-62-205-136-206.ngrok-free.app/`.

- Specify the content type as `application/json`.
- Fill in the `Secret` field with the passphrase: `LPVS`.
- Save the same passphrase in `github.secret` of the LPVS backend `application.properties` or `docker-compose.yml` files.

![step3](../../img/webhook/step_1_3.png)

- Select `Let me select individual events` -> `Pull requests` (make sure only `Pull requests` is selected).
- Set the webhook to `Active`.
- Click `Add Webhook`.

![step4](../../img/webhook/step_1_4.png)

Configuration from your project side is now completed.

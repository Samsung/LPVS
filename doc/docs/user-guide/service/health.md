# Health Check API

The License Pre-Validation Service (LPVS) provides a health check endpoint to monitor the status of the service, specifically to check the current load on the processing queue.

## Endpoint

- **URL:** `/health`
- **Method:** `GET`

## Description

This endpoint allows you to check the health of the LPVS service by retrieving the current number of items in the processing queue. This can be used for monitoring purposes to ensure the service is running and to gauge its current workload.

A high or continuously growing queue length might indicate that the service is under heavy load or that there are issues with processing items.

## Response

The endpoint returns a JSON object with a single key, `queueLength`, which represents the total number of items currently in the processing queue (persisted in the database).

### Success Response

- **Code:** `200 OK`
- **Content:**
  ```json
  {
    "queueLength": 5
  }
  ```

### Response Schema

| Key         | Type   | Description                                      |
|-------------|--------|--------------------------------------------------|
| `queueLength` | integer| The current number of items in the processing queue. |

## Example Usage

You can check the service health using a tool like `curl`:

```bash
curl -X GET http://localhost:8080/health
```

**Example Output:**

```json
{
  "queueLength": 0
}
```

This output indicates that the service is running and there are currently no items in the processing queue.

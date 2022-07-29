# Additional information and tips for Docker usage

#### There are three ways to execute the Docker container with LPVS after the image was built by command `docker build -t lpvs .` :
 
1. Terminal mode: the log is shown in the terminal in real-time, but the terminal must be open for running the application in the container:
   
   ```bash
   docker run -p 7896:7896 --name lpvs lpvs:latest 
   ```
 
2. Background mode: the container is not going to be restarted after reboot, to see the log additional commands should be used: `docker logs -f lpvs`:

    ```bash
    docker run -d -p 7896:7896 --name lpvs lpvs:latest
    ```

3. Background mode with constant usage: the container is going to be restarted after the reboot (other behavior is similar to the background mode):

    ```bash
    docker run -d -p 7896:7896 --restart unless-stopped --name lpvs lpvs:latest
    ```

  ***It is better (for disk space economy) to stop and start the same container which is created by the command `docker run`.*** 

#### Useful Docker commands
    
To stop the running container use the following commands:
    
   ```bash
   docker stop lpvs
   ```

To start the stopped container use the following commands:
    
   ```bash
   docker start lpvs
   ```

To clean unused containers (if the command `docker run` was used a few times), _only stopped containers will be deleted_:
    
   ```bash
   docker rm lpvs
   ```

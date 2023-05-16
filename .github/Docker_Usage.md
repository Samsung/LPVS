
#### Using docker-compose
 
For using docker deploy scenario, environment variables may be filled in file `docker-compose.yml`.

In this case, values in `docker-compose.yml` file overwrite values mentioned in `application.properties`.

Values for these variables need to be filled according to `README.md` instructions.

If you plan to use other than `root` database user that reflecting in files `application.properties` or `docker-compose.yml` as:
```
 spring.datasource.username=user
 spring.datasource.password=password  
```
then you needed to add two fields in `docker-compose.yml` file in section `environment` near `MYSQL_ROOT_PASSWORD` value:
```
  -MYSQL_USER: user
  -MYSQL_PASSWORD: password
```
Otherwise, if only `root` user is used
```
 spring.datasource.username=root
 spring.datasource.password=rootpassword  
```
than only need to fill one field
```
  -MYSQL_ROOT_PASSWORD:rootpassword
```
 But in both cases `MYSQL_ROOT_PASSWORD` need to be filled.
 
 
Also directory of keeping MySQL data may be changed, line:
```
 - ./mysql-lpvs-data:/var/lib/mysql # db storage -by default it is directory in root of repository with name mysql-lpvs-data
```

Start docker-compose:
- for old version of docker-compose
   ```bash
   docker-compose up -d --build
   ```
- for new version of docker-compose
   ```bash
   docker compose up
   ```   
   
Stop docker-compose:
- for old version of docker-compose:
   ```bash
   docker-compose down -v --rmi local
   ```  
- for new version of docker-compose:
   ```bash
   docker compose down
   ``` 


#### Using docker-compose
 
Please fill all needed lines in file `application-properties` according to README.md instructions.

If you would like to use another than root user for DB that reflecting in fields:

 spring.datasource.username=user
 
 spring.datasource.password=password  
 
 in file application.properties,
 
 needed to add 2 fields in docker-compose.yml file in section environment before or after MYSQL_ROOT_PASSWORD values:
 
  -MYSQL_USER: user
 
  -MYSQL_PASSWORD: password
 
Else if only root used- i.e.

 spring.datasource.username=root
 
 spring.datasource.password=rootpassword  
 
than only need to fill field

  -MYSQL_ROOT_PASSWORD:rootpassword

 But in both variants MYSQL_ROOT_PASSWORD field need to filled.
 
 
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

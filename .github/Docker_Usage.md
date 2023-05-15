
#### Using docker-compose
 
Please fill these lines in file docker-compose.yml with correct data(mandatory):
 - spring.datasource.username=root (may be not filled if this field already filled in application.properties)
 - spring.datasource.password=     (may be not filled if this field already filled in application.properties)
 - github.login=
 - github.token=
 - MYSQL_ROOT_PASSWORD:
 
 Take in attention that value in spring.datasource.password= for user root must be the same as in the field  MYSQL_ROOT_PASSWORD.
 
Other lines in 2 sections environment may be changed(optionally).

Also directory of keeping MySQL data may be changed, line:
 - ./mysql-lpvs-data:/var/lib/mysql # db storage -by default it is directory in root of repository with name mysql-lpvs-data

Start docker-compose (for old version of docker-compose):
   ```bash
   docker-compose up -d --build
   ```
Start docker-compose (for new version of docker-compose):
   ```bash
   docker compose up
   ```   
   
   
 Stop docker-compose(for old version of docker-compose):
   ```bash
   docker-compose down -v --rmi local
   ```  
  Stop docker-compose(for new version of docker-compose):
   ```bash
   docker-compose down -v --rmi local
   ```  
 

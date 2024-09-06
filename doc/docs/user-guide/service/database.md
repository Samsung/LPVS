# Database configuration

How to install and configure the LPVS database.

---

## Installation

Install MySQL server locally using the command:

```bash
sudo apt install mysql-server
```

---

## Configuration

* Start the MySQL server:

```bash
sudo service mysql start
```

* Open the MySQL command line interface:

```bash
sudo mysql
```

* Run the following commands in the MySQL command line interface to create the necessary database and user:

```sql
mysql> create database lpvs;
mysql> create user username;
mysql> grant all on lpvs.* to username;
mysql> alter user username identified by 'password';
mysql> exit;
```

* (**Optional**) If you have an existing dump file, import it into the newly created database using the command:

```bash
mysql -u[username] -p[password] < /path/to/dump/file/database_dump.sql
```

* (**Optional**) Fill in the `lpvs_license_list` and `lpvs_license_conflicts` tables according to the [Database customization guideline](../config/database.md).

* Update the properties related to the database operation in the profile file according to the [guideline](../config/options.md).

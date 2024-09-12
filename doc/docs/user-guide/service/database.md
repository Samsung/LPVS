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

* Run the following commands in the MySQL command line interface to create the necessary database and user.

!!! note

    Replace 'username' and 'password' with your preferred values. However, in the example below, they are kept as placeholders for the sake of clarity:

```sql
create database lpvs;
create user username;
grant all on lpvs.* to username;
alter user username identified by 'password';
exit;
```

* (**Optional**) If using the provided dump file, make sure to run the following command from the repository's base folder. If using a different dump file, specify its path in the command. After running the command, you will be prompted to enter the password set in the previous step:

```bash
mysql -u [username] -p < /src/main/resources/database_dump.sql
```

* (**Optional**) Fill in the `lpvs_license_list` and `lpvs_license_conflicts` tables according to the [Database customization guideline](../config/database.md).

* Update the properties related to the database operation in the profile file according to the [guideline](../config/options.md).

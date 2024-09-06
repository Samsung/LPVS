# Database customization

Customization of the database with licenses and license conflicts.

---

The LPVS database consists of several tables. You have to pay attention to two tables: `lpvs_license_list` 
and `lpvs_license_conflicts`. These tables are used to store information about licenses and their conflicts 
respectively. Here's a brief description of each table.

### Table `lpvs_license_list`

This table stores license information.

```sql
CREATE TABLE IF NOT EXISTS lpvs_license_list (
    id bigint NOT NULL AUTO_INCREMENT,
    license_usage varchar(255) DEFAULT NULL,
    license_name varchar(255) NOT NULL,
    license_spdx varchar(255) NOT NULL,
    license_alternative_names longtext DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE (license_spdx)
);
```

The meanings of each field are as follows:
- `id`: Unique identifier which auto increments.
- `license_usage`: Indicates the possibility to use the license in your code and could be `PERMITTED`, `RESTRICTED`, `PROHIBITED`, or `UNREVIEWED`.
- `license_name`: Name of the license.
- `license_spdx`: SPDX identifier of the license.
- `license_alternative_names`: Alternative names of the license.

### Table `lpvs_license_conflicts`

This table stores license conflicts information. 

```sql
CREATE TABLE IF NOT EXISTS lpvs_license_conflicts (
    id bigint NOT NULL AUTO_INCREMENT,
    conflict_license_id bigint NOT NULL,
    repository_license_id bigint NOT NULL,
    PRIMARY KEY (id),
    KEY (conflict_license_id),
    KEY (repository_license_id),
    FOREIGN KEY (conflict_license_id) REFERENCES lpvs_license_list (id),
    FOREIGN KEY (repository_license_id) REFERENCES lpvs_license_list (id)
);
```

The meanings of each field are as follows:
- `id`: Unique identifier which auto increments.
- `conflict_license_id`: ID of the conflicting license.
- `repository_license_id`: License ID of the repository.

!!! warning

    Both tables `lpvs_license_list` and `lpvs_license_conflicts` should be filled by the user manually. 

When a new license is detected by LPVS, it will be added to the table `lpvs_license_list` with 
`license_usage` status set to `UNREVIEWED`, indicating that it has not been reviewed yet.

!!! info

    Sample MySQL dump file is located in the [repository](https://github.com/Samsung/LPVS/blob/main/src/main/resources/database_dump.sql) 
    and can be used as a reference.

CREATE SCHEMA IF NOT EXISTS lpvs;
USE lpvs;

CREATE TABLE IF NOT EXISTS lpvs_license_list (
  id bigint NOT NULL AUTO_INCREMENT,
  license_usage varchar(255) DEFAULT NULL,
  license_name varchar(255) NOT NULL,
  license_spdx varchar(255) NOT NULL,
  license_alternative_names longtext DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE (license_spdx)
);

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

CREATE TABLE IF NOT EXISTS lpvs_pull_requests (
  id bigint NOT NULL AUTO_INCREMENT,
  queue_id bigint DEFAULT NULL,
  scan_date datetime NOT NULL,
  user varchar(255) DEFAULT NULL,
  repository_name varchar(255) NOT NULL,
  url longtext NOT NULL,
  diff_url longtext,
  status varchar(255) DEFAULT NULL,
  pull_request_head varchar(255) DEFAULT NULL,
  pull_request_base varchar(255) DEFAULT NULL,
  sender varchar(255) DEFAULT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS lpvs_detected_license (
  id bigint NOT NULL AUTO_INCREMENT,
  pull_request_id bigint DEFAULT NULL,
  license_id bigint DEFAULT NULL,
  conflict_id bigint DEFAULT NULL,
  repository_license_id bigint DEFAULT NULL,
  file_path longtext,
  match_type varchar(255) DEFAULT NULL,
  match_value varchar(255) DEFAULT NULL,
  match_lines varchar(255) DEFAULT NULL,
  component_file_path longtext,
  component_file_url longtext,
  component_name varchar(255) DEFAULT NULL,
  component_lines varchar(255) DEFAULT NULL,
  component_url longtext,
  component_version varchar(255) DEFAULT NULL,
  component_vendor varchar(255) DEFAULT NULL,
  issue bit DEFAULT NULL,
  PRIMARY KEY (id),
  KEY (pull_request_id),
  KEY (license_id),
  KEY (repository_license_id),
  KEY (conflict_id),
  FOREIGN KEY (conflict_id) REFERENCES lpvs_license_conflicts (id),
  FOREIGN KEY (license_id) REFERENCES lpvs_license_list (id),
  FOREIGN KEY (pull_request_id) REFERENCES lpvs_pull_requests (id),
  FOREIGN KEY (repository_license_id) REFERENCES lpvs_license_list (id)
);

CREATE TABLE IF NOT EXISTS lpvs_queue (
  id bigint NOT NULL AUTO_INCREMENT,
  action bigint NOT NULL,
  attempts int DEFAULT '0',
  scan_date datetime DEFAULT NULL,
  user_id varchar(255) DEFAULT NULL,
  review_system_type varchar(255) DEFAULT NULL,
  repository_url longtext,
  pull_request_url longtext,
  pull_request_api_url longtext,
  pull_request_diff_url longtext,
  status_callback_url longtext,
  commit_sha varchar(255) DEFAULT NULL,
  pull_request_base varchar(255) DEFAULT NULL,
  pull_request_head varchar(255) DEFAULT NULL,
  sender varchar(255) DEFAULT NULL,
  PRIMARY KEY (id)
);


INSERT INTO lpvs_license_list (id, license_name, license_spdx, license_alternative_names, license_usage) VALUES
(1, 'GNU General Public License v3.0 only','GPL-3.0-only','','PROHIBITED'),
(2, 'OpenSSL License','OpenSSL','OPENSSL_LICENSE,SSLeay license and OpenSSL License','PERMITTED'),
(3, 'GNU Lesser General Public License v2.0 or later','LGPL-2.0-or-later','','RESTRICTED'),
(4, 'MIT License','MIT','Bouncy Castle Licence,The MIT License,The MIT License (MIT)','PERMITTED'),
(5, 'Apache License 2.0','Apache-2.0','Android-Apache-2.0,Apache 2,Apache 2.0,Apache 2.0 license,Apache License (v2.0),Apache License v2,Apache License v2.0,Apache License Version 2.0,Apache License Version 2.0 January 2004,Apache Public License 2.0,Apache Software License (Apache 2.0),Apache Software License (Apache License 2.0),Apache Software License - Version 2.0,Apache v2,Apache v2.0,Apache Version 2.0,Apache-2.0 License,APACHE2,ASF 2.0,http://www.apache.org/licenses/LICENSE-2.0.txt,https://www.apache.org/licenses/LICENSE-2.0,https://www.apache.org/licenses/LICENSE-2.0.txt,the Apache License ASL Version 2.0,The Apache License Version 2.0,The Apache Software License Version 2.0','PERMITTED'),
(6, 'GNU General Public License v2.0 only','GPL-2.0-only','','RESTRICTED'),
(7, 'GNU Lesser General Public License v3.0 or later','LGPL-3.0-or-later','GNU Lesser General Public License v3 or later (LGPLv3+),Lesser General Public License version 3 or greater,LGPLv3+','PROHIBITED');

INSERT INTO lpvs_license_conflicts (conflict_license_id, repository_license_id) VALUES
(1, 3), (1, 6), (2, 6), (2, 3), (3, 5), (3, 7), (5, 6), (6, 7);

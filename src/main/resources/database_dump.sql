CREATE SCHEMA IF NOT EXISTS lpvs;
USE lpvs;

CREATE TABLE IF NOT EXISTS licenses (
  id bigint NOT NULL AUTO_INCREMENT,
  license_usage varchar(255) DEFAULT NULL,
  license_name varchar(255) NOT NULL,
  license_spdx varchar(255) NOT NULL,
  license_alternative_names longtext DEFAULT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS license_conflicts (
  id bigint NOT NULL AUTO_INCREMENT,
  conflict_license_id bigint NOT NULL,
  repository_license_id bigint NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS pull_requests (
  id bigint NOT NULL AUTO_INCREMENT,
  scan_date datetime NOT NULL,
  user varchar(255) DEFAULT NULL,
  repository_name varchar(255) NOT NULL,
  url longtext NOT NULL,
  diff_url longtext,
  status varchar(255) DEFAULT NULL,
  pull_request_head varchar(255) NOT NULL,
  pull_request_base varchar(255) NOT NULL,
  sender varchar(255) NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS detected_license (
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
  PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS queue (
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
  PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS member (
  id bigint PRIMARY KEY NOT NULL AUTO_INCREMENT,
  email varchar(255) NOT NULL,
  name varchar(255) NOT NULL,
  nickname varchar(255) DEFAULT NULL,
  provider varchar(255) NOT NULL,
  organization varchar(255) DEFAULT NULL,
  UNIQUE (email,provider)
);

INSERT INTO licenses (id, license_alternative_names, license_name, license_spdx, license_usage) VALUES
(2,NULL,'GNU General Public License v3.0 only','GPL-3.0-only','PROHIBITED'),
(3,NULL,'OpenSSL License','OpenSSL','PERMITTED'),
(4,NULL,'GNU Lesser General Public License v2.1 or later','GPL-2.0-or-later','RESTRICTED'),
(531, NULL, 'MIT License', 'MIT', 'PERMITTED'),
(529, NULL, 'Apache License 2.0', 'Apache-2.0', 'PERMITTED'),
(257, NULL, 'GNU General Public License v2.0 only', 'GPL-2.0-only', 'RESTRICTED'),
(289, NULL, 'GNU Lesser General Public License v3.0 or later', 'LGPL-3.0-or-later', 'PROHIBITED');
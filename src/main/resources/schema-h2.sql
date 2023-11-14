CREATE SCHEMA IF NOT EXISTS lpvs;
USE lpvs;

DROP TABLE IF EXISTS licenses;
DROP TABLE IF EXISTS license_conflicts;
DROP TABLE IF EXISTS pull_requests;
DROP TABLE IF EXISTS detected_license;
DROP TABLE IF EXISTS queue;
DROP TABLE IF EXISTS member;

CREATE TABLE licenses (
  id bigint NOT NULL AUTO_INCREMENT,
  license_usage varchar DEFAULT NULL,
  license_name varchar NOT NULL,
  license_spdx varchar NOT NULL,
  license_alternative_names longtext DEFAULT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE license_conflicts (
  id bigint NOT NULL AUTO_INCREMENT,
  conflict_license_id bigint NOT NULL,
  repository_license_id bigint NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE pull_requests (
  id bigint NOT NULL AUTO_INCREMENT,
  scan_date datetime NOT NULL,
  user_string varchar DEFAULT NULL,
  repository_name varchar NOT NULL,
  url longtext NOT NULL,
  diff_url longtext,
  status varchar DEFAULT NULL,
  pull_request_head varchar NOT NULL,
  pull_request_base varchar NOT NULL,
  sender varchar NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE detected_license (
  id bigint NOT NULL AUTO_INCREMENT,
  pull_request_id bigint DEFAULT NULL,
  license_id bigint DEFAULT NULL,
  conflict_id bigint DEFAULT NULL,
  repository_license_id bigint DEFAULT NULL,
  file_path longtext,
  match_type varchar DEFAULT NULL,
  match_value varchar DEFAULT NULL,
  match_lines varchar DEFAULT NULL,
  component_file_path longtext,
  component_file_url longtext,
  component_name varchar DEFAULT NULL,
  component_lines varchar DEFAULT NULL,
  component_url longtext,
  component_version varchar DEFAULT NULL,
  component_vendor varchar DEFAULT NULL,
  issue bit DEFAULT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE queue (
  id bigint NOT NULL AUTO_INCREMENT,
  action bigint NOT NULL,
  attempts int DEFAULT '0',
  scan_date datetime DEFAULT NULL,
  user_id varchar DEFAULT NULL,
  review_system_type varchar DEFAULT NULL,
  repository_url longtext,
  pull_request_url longtext,
  pull_request_api_url longtext,
  pull_request_diff_url longtext,
  status_callback_url longtext,
  commit_sha varchar DEFAULT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE member (
  id bigint PRIMARY KEY NOT NULL AUTO_INCREMENT,
  email varchar NOT NULL,
  name varchar NOT NULL,
  nickname varchar DEFAULT NULL,
  provider varchar NOT NULL,
  organization varchar DEFAULT NULL,
  UNIQUE (email,provider)
);

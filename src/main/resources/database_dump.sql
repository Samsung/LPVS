CREATE DATABASE  IF NOT EXISTS `lpvs` /*!40100 DEFAULT CHARACTER SET latin1 */;
USE `lpvs`;
-- MySQL dump 10.13  Distrib 8.0.26, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: lpvs
-- ------------------------------------------------------
-- Server version	5.6.51-log

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `license_conflicts`
--

DROP TABLE IF EXISTS `license_conflicts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `license_conflicts` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `conflict_license` bigint(20) NOT NULL,
  `repository_license` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKrtmiqvoah4x5lprxu186vp0i` (`conflict_license`),
  KEY `FKbgosahkomx4ns2e7jyics99cl` (`repository_license`),
  CONSTRAINT `FKbgosahkomx4ns2e7jyics99cl` FOREIGN KEY (`repository_license`) REFERENCES `licenses` (`id`),
  CONSTRAINT `FKrtmiqvoah4x5lprxu186vp0i` FOREIGN KEY (`conflict_license`) REFERENCES `licenses` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `license_conflicts`
--

LOCK TABLES `license_conflicts` WRITE;
/*!40000 ALTER TABLE `license_conflicts` DISABLE KEYS */;
INSERT INTO `license_conflicts` VALUES (1,4,1),(2,4,3),(3,4,5);
/*!40000 ALTER TABLE `license_conflicts` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `licenses`
--

DROP TABLE IF EXISTS `licenses`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `licenses` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `license_usage` varchar(255) DEFAULT NULL,
  `license_name` varchar(255) NOT NULL,
  `license_spdx` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `spdx_id` (`license_spdx`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `licenses`
--

LOCK TABLES `licenses` WRITE;
/*!40000 ALTER TABLE `licenses` DISABLE KEYS */;
INSERT INTO `licenses` VALUES (1,'PERMITTED','Apache License 2.0','Apache-2.0'),(2,'PROHIBITED','GNU General Public License v3.0 only','GPL-3.0-only'),(3,'PERMITTED','OpenSSL License','OpenSSL'),(4,'RESTRICTED','GNU Lesser General Public License v2.1 or later','GPL-2.0-or-later'),(5,'PERMITTED','MIT License','MIT');
/*!40000 ALTER TABLE `licenses` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2023-01-19 11:18:17

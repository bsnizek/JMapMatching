/*
 Navicat PostgreSQL Data Transfer

 Source Server         : localhost
 Source Server Version : 90004
 Source Host           : localhost
 Source Database       : bikeability
 Source Schema         : public

 Target Server Version : 90004
 File Encoding         : utf-8

 Date: 11/08/2011 18:36:15 PM
*/

-- ----------------------------
--  Table structure for "sourceroute"
-- ----------------------------
DROP TABLE IF EXISTS "sourceroute";
CREATE TABLE "sourceroute" (
	"id" int4 NOT NULL,
	"respondentid" int4
)
WITH (OIDS=FALSE);
ALTER TABLE "sourceroute" OWNER TO "biker";

-- ----------------------------
--  Records of "sourceroute"
-- ----------------------------
BEGIN;
INSERT INTO "sourceroute" VALUES ('0', '0');
COMMIT;

-- ----------------------------
--  Primary key structure for table "sourceroute"
-- ----------------------------
ALTER TABLE "sourceroute" ADD CONSTRAINT "sourceroute_pkey" PRIMARY KEY ("id") NOT DEFERRABLE INITIALLY IMMEDIATE;


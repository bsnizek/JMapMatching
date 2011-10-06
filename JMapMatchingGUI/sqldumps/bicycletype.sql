/*
 Navicat PostgreSQL Data Transfer

 Source Server         : localhost
 Source Server Version : 90004
 Source Host           : localhost
 Source Database       : bikeability
 Source Schema         : public

 Target Server Version : 90004
 File Encoding         : utf-8

 Date: 10/06/2011 18:22:00 PM
*/

-- ----------------------------
--  Table structure for "bicycletype"
-- ----------------------------
DROP TABLE IF EXISTS "bicycletype";
CREATE TABLE "bicycletype" (
	"id" int2 NOT NULL,
	"descr" char(16)
)
WITH (OIDS=FALSE);
ALTER TABLE "bicycletype" OWNER TO "postgres";

-- ----------------------------
--  Records of "bicycletype"
-- ----------------------------
BEGIN;
INSERT INTO "bicycletype" VALUES ('0', null);
INSERT INTO "bicycletype" VALUES ('1', 'no              ');
INSERT INTO "bicycletype" VALUES ('2', 'track           ');
INSERT INTO "bicycletype" VALUES ('3', 'dismount        ');
INSERT INTO "bicycletype" VALUES ('4', 'yes             ');
INSERT INTO "bicycletype" VALUES ('5', 'designated      ');
INSERT INTO "bicycletype" VALUES ('6', 'permissive      ');
COMMIT;

-- ----------------------------
--  Primary key structure for table "bicycletype"
-- ----------------------------
ALTER TABLE "bicycletype" ADD CONSTRAINT "cyclewaytype_copy_pkey" PRIMARY KEY ("id") NOT DEFERRABLE INITIALLY IMMEDIATE;


/*
 Navicat PostgreSQL Data Transfer

 Source Server         : localhost
 Source Server Version : 90004
 Source Host           : localhost
 Source Database       : bikeability
 Source Schema         : public

 Target Server Version : 90004
 File Encoding         : utf-8

 Date: 10/06/2011 18:22:18 PM
*/

-- ----------------------------
--  Table structure for "foottype"
-- ----------------------------
DROP TABLE IF EXISTS "foottype";
CREATE TABLE "foottype" (
	"id" int2 NOT NULL,
	"descr" char(16)
)
WITH (OIDS=FALSE);
ALTER TABLE "foottype" OWNER TO "postgres";

-- ----------------------------
--  Records of "foottype"
-- ----------------------------
BEGIN;
INSERT INTO "foottype" VALUES ('0', null);
INSERT INTO "foottype" VALUES ('1', 'unknown         ');
INSERT INTO "foottype" VALUES ('2', 'no              ');
INSERT INTO "foottype" VALUES ('3', 'destination     ');
INSERT INTO "foottype" VALUES ('4', 'yes             ');
INSERT INTO "foottype" VALUES ('5', 'permissive      ');
INSERT INTO "foottype" VALUES ('6', 'designated      ');
COMMIT;

-- ----------------------------
--  Primary key structure for table "foottype"
-- ----------------------------
ALTER TABLE "foottype" ADD CONSTRAINT "cyclewaytype_copy_pkey2" PRIMARY KEY ("id") NOT DEFERRABLE INITIALLY IMMEDIATE;


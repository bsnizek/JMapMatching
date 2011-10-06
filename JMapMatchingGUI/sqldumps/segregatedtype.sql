/*
 Navicat PostgreSQL Data Transfer

 Source Server         : localhost
 Source Server Version : 90004
 Source Host           : localhost
 Source Database       : bikeability
 Source Schema         : public

 Target Server Version : 90004
 File Encoding         : utf-8

 Date: 10/06/2011 18:22:28 PM
*/

-- ----------------------------
--  Table structure for "segregatedtype"
-- ----------------------------
DROP TABLE IF EXISTS "segregatedtype";
CREATE TABLE "segregatedtype" (
	"id" int2 NOT NULL,
	"descr" char(16)
)
WITH (OIDS=FALSE);
ALTER TABLE "segregatedtype" OWNER TO "postgres";

-- ----------------------------
--  Records of "segregatedtype"
-- ----------------------------
BEGIN;
INSERT INTO "segregatedtype" VALUES ('0', null);
INSERT INTO "segregatedtype" VALUES ('1', 'no              ');
INSERT INTO "segregatedtype" VALUES ('2', 'yes             ');
COMMIT;

-- ----------------------------
--  Primary key structure for table "segregatedtype"
-- ----------------------------
ALTER TABLE "segregatedtype" ADD CONSTRAINT "highwaytype_copy_pkey" PRIMARY KEY ("id") NOT DEFERRABLE INITIALLY IMMEDIATE;


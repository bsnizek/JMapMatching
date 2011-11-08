/*
 Navicat PostgreSQL Data Transfer

 Source Server         : localhost
 Source Server Version : 90004
 Source Host           : localhost
 Source Database       : bikeability
 Source Schema         : public

 Target Server Version : 90004
 File Encoding         : utf-8

 Date: 11/08/2011 18:36:04 PM
*/

-- ----------------------------
--  Table structure for "respondent"
-- ----------------------------
DROP TABLE IF EXISTS "respondent";
CREATE TABLE "respondent" (
	"id" int4 NOT NULL
)
WITH (OIDS=FALSE);
ALTER TABLE "respondent" OWNER TO "biker";

-- ----------------------------
--  Records of "respondent"
-- ----------------------------
BEGIN;
INSERT INTO "respondent" VALUES ('0');
COMMIT;

-- ----------------------------
--  Primary key structure for table "respondent"
-- ----------------------------
ALTER TABLE "respondent" ADD CONSTRAINT "respondent_pkey" PRIMARY KEY ("id") NOT DEFERRABLE INITIALLY IMMEDIATE;


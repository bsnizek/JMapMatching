/*
 Navicat PostgreSQL Data Transfer

 Source Server         : localhost
 Source Server Version : 90004
 Source Host           : localhost
 Source Database       : bikeability
 Source Schema         : public

 Target Server Version : 90004
 File Encoding         : utf-8

 Date: 09/20/2011 17:31:17 PM
*/

-- ----------------------------
--  Table structure for "respondent"
-- ----------------------------
DROP TABLE IF EXISTS "respondent";
CREATE TABLE "respondent" (
	"id" int4 NOT NULL
)
WITH (OIDS=FALSE);
ALTER TABLE "respondent" OWNER TO "postgres";

-- ----------------------------
--  Records of "respondent"
-- ----------------------------
BEGIN;
INSERT INTO "respondent" VALUES ('58349181');
INSERT INTO "respondent" VALUES ('57290090');
INSERT INTO "respondent" VALUES ('57139404');
INSERT INTO "respondent" VALUES ('58098029');
COMMIT;

-- ----------------------------
--  Primary key structure for table "respondent"
-- ----------------------------
ALTER TABLE "respondent" ADD CONSTRAINT "respondent_pkey" PRIMARY KEY ("id") NOT DEFERRABLE INITIALLY IMMEDIATE;


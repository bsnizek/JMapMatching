/*
 Navicat PostgreSQL Data Transfer

 Source Server         : localhost
 Source Server Version : 90004
 Source Host           : localhost
 Source Database       : bikeability
 Source Schema         : public

 Target Server Version : 90004
 File Encoding         : utf-8

 Date: 09/20/2011 17:31:45 PM
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
INSERT INTO "sourceroute" VALUES ('12158', '58349181');
INSERT INTO "sourceroute" VALUES ('15230', '57290090');
INSERT INTO "sourceroute" VALUES ('61931', '57139404');
INSERT INTO "sourceroute" VALUES ('61987', '57139404');
INSERT INTO "sourceroute" VALUES ('80928', '58098029');
INSERT INTO "sourceroute" VALUES ('81209', '58098029');
COMMIT;

-- ----------------------------
--  Primary key structure for table "sourceroute"
-- ----------------------------
ALTER TABLE "sourceroute" ADD CONSTRAINT "sourceroute_pkey" PRIMARY KEY ("id") NOT DEFERRABLE INITIALLY IMMEDIATE;


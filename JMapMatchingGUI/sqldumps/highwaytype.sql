/*
 Navicat PostgreSQL Data Transfer

 Source Server         : localhost
 Source Server Version : 90004
 Source Host           : localhost
 Source Database       : bikeability
 Source Schema         : public

 Target Server Version : 90004
 File Encoding         : utf-8

 Date: 10/06/2011 18:22:22 PM
*/

-- ----------------------------
--  Table structure for "highwaytype"
-- ----------------------------
DROP TABLE IF EXISTS "highwaytype";
CREATE TABLE "highwaytype" (
	"id" int2 NOT NULL,
	"descr" char(16)
)
WITH (OIDS=FALSE);
ALTER TABLE "highwaytype" OWNER TO "biker";

-- ----------------------------
--  Records of "highwaytype"
-- ----------------------------
BEGIN;
INSERT INTO "highwaytype" VALUES ('0', null);
INSERT INTO "highwaytype" VALUES ('1', 'primary         ');
INSERT INTO "highwaytype" VALUES ('2', 'secondary       ');
INSERT INTO "highwaytype" VALUES ('3', 'unclassified    ');
INSERT INTO "highwaytype" VALUES ('4', 'footway         ');
INSERT INTO "highwaytype" VALUES ('5', 'track           ');
INSERT INTO "highwaytype" VALUES ('6', 'service         ');
INSERT INTO "highwaytype" VALUES ('7', 'cycleway        ');
INSERT INTO "highwaytype" VALUES ('8', 'living_street   ');
INSERT INTO "highwaytype" VALUES ('9', 'path            ');
INSERT INTO "highwaytype" VALUES ('10', 'tertiary        ');
INSERT INTO "highwaytype" VALUES ('11', 'residential     ');
INSERT INTO "highwaytype" VALUES ('12', 'steps           ');
INSERT INTO "highwaytype" VALUES ('13', 'pedestrian      ');
INSERT INTO "highwaytype" VALUES ('14', 'primary_link    ');
COMMIT;

-- ----------------------------
--  Primary key structure for table "highwaytype"
-- ----------------------------
ALTER TABLE "highwaytype" ADD CONSTRAINT "cyclewaytype_copy_pkey1" PRIMARY KEY ("id") NOT DEFERRABLE INITIALLY IMMEDIATE;


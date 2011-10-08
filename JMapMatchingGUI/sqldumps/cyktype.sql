/*
 Navicat PostgreSQL Data Transfer

 Source Server         : localhost
 Source Server Version : 90004
 Source Host           : localhost
 Source Database       : bikeability
 Source Schema         : public

 Target Server Version : 90004
 File Encoding         : utf-8

 Date: 10/06/2011 18:22:10 PM
*/

-- ----------------------------
--  Table structure for "cyktype"
-- ----------------------------
DROP TABLE IF EXISTS "cyktype";
CREATE TABLE "cyktype" (
	"id" int2 NOT NULL,
	"descr" char(64)
)
WITH (OIDS=FALSE);
ALTER TABLE "cyktype" OWNER TO "biker";

-- ----------------------------
--  Records of "cyktype"
-- ----------------------------
BEGIN;
INSERT INTO "cyktype" VALUES ('0', 'Street with no facility                                         ');
INSERT INTO "cyktype" VALUES ('1', 'Bike path along street                                          ');
INSERT INTO "cyktype" VALUES ('2', 'Bike lane along street                                          ');
INSERT INTO "cyktype" VALUES ('3', 'Path, exclusively for bikes                                     ');
INSERT INTO "cyktype" VALUES ('4', 'Path, shared bikes and pedestrians                              ');
COMMIT;

-- ----------------------------
--  Primary key structure for table "cyktype"
-- ----------------------------
ALTER TABLE "cyktype" ADD CONSTRAINT "EnvType_copy_pkey" PRIMARY KEY ("id") NOT DEFERRABLE INITIALLY IMMEDIATE;


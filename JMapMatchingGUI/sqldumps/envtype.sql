/*
 Navicat PostgreSQL Data Transfer

 Source Server         : localhost
 Source Server Version : 90004
 Source Host           : localhost
 Source Database       : bikeability
 Source Schema         : public

 Target Server Version : 90004
 File Encoding         : utf-8

 Date: 10/06/2011 18:22:13 PM
*/

-- ----------------------------
--  Table structure for "envtype"
-- ----------------------------
DROP TABLE IF EXISTS "envtype";
CREATE TABLE "envtype" (
	"id" int2 NOT NULL,
	"descr" char(64)
)
WITH (OIDS=FALSE);
ALTER TABLE "envtype" OWNER TO "biker";

-- ----------------------------
--  Records of "envtype"
-- ----------------------------
BEGIN;
INSERT INTO "envtype" VALUES ('0', 'NULL                                                            ');
INSERT INTO "envtype" VALUES ('1', 'Primary Road                                                    ');
INSERT INTO "envtype" VALUES ('2', 'Secondary Road                                                  ');
INSERT INTO "envtype" VALUES ('3', 'Others                                                          ');
INSERT INTO "envtype" VALUES ('4', 'Residential street (detached or semi-detached housing)          ');
INSERT INTO "envtype" VALUES ('5', 'Residential street (multistory housing)                         ');
INSERT INTO "envtype" VALUES ('6', 'Shopping street (multistory housing with shops)                 ');
INSERT INTO "envtype" VALUES ('7', 'Path, exclusively for bikes                                     ');
INSERT INTO "envtype" VALUES ('8', 'Path, shared bikes and pedestrians                              ');
COMMIT;

-- ----------------------------
--  Primary key structure for table "envtype"
-- ----------------------------
ALTER TABLE "envtype" ADD CONSTRAINT "foottype_copy_pkey" PRIMARY KEY ("id") NOT DEFERRABLE INITIALLY IMMEDIATE;


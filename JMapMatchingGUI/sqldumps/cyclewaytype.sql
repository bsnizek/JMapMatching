/*
 Navicat PostgreSQL Data Transfer

 Source Server         : localhost
 Source Server Version : 90004
 Source Host           : localhost
 Source Database       : bikeability
 Source Schema         : public

 Target Server Version : 90004
 File Encoding         : utf-8

 Date: 10/06/2011 18:22:04 PM
*/

-- ----------------------------
--  Table structure for "cyclewaytype"
-- ----------------------------
DROP TABLE IF EXISTS "cyclewaytype";
CREATE TABLE "cyclewaytype" (
	"id" int2 NOT NULL,
	"descr" char(16)
)
WITH (OIDS=FALSE);
ALTER TABLE "cyclewaytype" OWNER TO "postgres";

-- ----------------------------
--  Records of "cyclewaytype"
-- ----------------------------
BEGIN;
INSERT INTO "cyclewaytype" VALUES ('0', null);
INSERT INTO "cyclewaytype" VALUES ('1', 'track           ');
INSERT INTO "cyclewaytype" VALUES ('2', 'undefined       ');
INSERT INTO "cyclewaytype" VALUES ('3', 'opposite_track  ');
INSERT INTO "cyclewaytype" VALUES ('4', 'path            ');
INSERT INTO "cyclewaytype" VALUES ('5', 'segregated      ');
INSERT INTO "cyclewaytype" VALUES ('6', 'no              ');
INSERT INTO "cyclewaytype" VALUES ('7', 'opposite        ');
INSERT INTO "cyclewaytype" VALUES ('8', 'lane            ');
INSERT INTO "cyclewaytype" VALUES ('9', 'none            ');
INSERT INTO "cyclewaytype" VALUES ('10', 'opposite_lane   ');
INSERT INTO "cyclewaytype" VALUES ('11', 'shared          ');
INSERT INTO "cyclewaytype" VALUES ('12', 'yes             ');
COMMIT;

-- ----------------------------
--  Primary key structure for table "cyclewaytype"
-- ----------------------------
ALTER TABLE "cyclewaytype" ADD CONSTRAINT "cyclewaytype_pkey" PRIMARY KEY ("id") NOT DEFERRABLE INITIALLY IMMEDIATE;


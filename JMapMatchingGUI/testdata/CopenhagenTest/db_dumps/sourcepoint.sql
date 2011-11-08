/*
 Navicat PostgreSQL Data Transfer

 Source Server         : localhost
 Source Server Version : 90004
 Source Host           : localhost
 Source Database       : bikeability
 Source Schema         : public

 Target Server Version : 90004
 File Encoding         : utf-8

 Date: 11/08/2011 18:24:09 PM
*/

-- ----------------------------
--  Table structure for "sourcepoint"
-- ----------------------------
DROP TABLE IF EXISTS "sourcepoint";
CREATE TABLE "sourcepoint" (
	"id" int4 NOT NULL,
	"geom" "geometry",
	"sourcerouteid" int4
)
WITH (OIDS=FALSE);
ALTER TABLE "sourcepoint" OWNER TO "biker";

-- ----------------------------
--  Primary key structure for table "sourcepoint"
-- ----------------------------
ALTER TABLE "sourcepoint" ADD CONSTRAINT "sourcepoint_pkey" PRIMARY KEY ("id") NOT DEFERRABLE INITIALLY IMMEDIATE;


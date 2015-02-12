-- version 1.3

-- ------ --
-- TABLES --
DROP TABLE pictures CASCADE CONSTRAINTS;
DROP TABLE products CASCADE CONSTRAINTS;
DROP TABLE sectors CASCADE CONSTRAINTS;
DROP TABLE regals CASCADE CONSTRAINTS;
DROP TABLE routers CASCADE CONSTRAINTS;
DROP TABLE paths CASCADE CONSTRAINTS;

-- keep regal info / place of regal in the store
CREATE TABLE regals (
  regal_id INT PRIMARY KEY,
  category VARCHAR(20) UNIQUE NOT NULL,
  placement SDO_GEOMETRY
);

-- keep sector info / place of sector in regal
CREATE TABLE sectors(
  sector_id INT PRIMARY KEY,
  sector_name VARCHAR(20) NOT NULL,
  sector_placement SDO_GEOMETRY,
  regal_id INT REFERENCES regals(regal_id) ON DELETE CASCADE,
  sector_id_inregal INT
);

-- keep product info / place of product in regal
CREATE TABLE products (
  product_id INT PRIMARY KEY,
  name VARCHAR(50) NOT NULL,
  manufacturer VARCHAR(50) NOT NULL,
  regal_id INT REFERENCES regals(regal_id) ON DELETE CASCADE,
  sector_id INT REFERENCES sectors(sector_id) ON DELETE CASCADE,
  valid_from DATE,
  valid_to DATE
);

-- extend product info for one or few pictures of product
CREATE TABLE pictures (
  picture_id INT PRIMARY KEY,
  product_id INT REFERENCES products(product_id) ON DELETE CASCADE,
  description VARCHAR(50),
  picture ORDSYS.ORDImage,
  picture_si ORDSYS.SI_StillImage,
  picture_ac ORDSYS.SI_AverageColor,
  picture_ch ORDSYS.SI_ColorHistogram,
  picture_pc ORDSYS.SI_PositionalColor,
  picture_tx ORDSYS.SI_Texture
);

-- wifi access points
CREATE TABLE routers (
  router_id INT PRIMARY KEY,
  router_placement SDO_GEOMETRY,
  router_radius SDO_GEOMETRY
);

-- path across the warehouse
CREATE TABLE paths (
  path_id INT PRIMARY KEY,
  geometry SDO_GEOMETRY
);



-- SEQUENCES --
-- --------- --
DROP SEQUENCE regals_seq;
DROP SEQUENCE sectors_seq;
DROP SEQUENCE products_seq;
DROP SEQUENCE pictures_seq;
DROP SEQUENCE routers_seq;
DROP SEQUENCE paths_seq;

CREATE SEQUENCE regals_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE sectors_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE products_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE pictures_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE routers_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE paths_seq START WITH 1 INCREMENT BY 1;

-- ------------- --
-- INIT GEOMETRY --
DELETE FROM USER_SDO_GEOM_METADATA WHERE TABLE_NAME = 'REGALS' AND COLUMN_NAME = 'PLACEMENT';
DELETE FROM USER_SDO_GEOM_METADATA WHERE TABLE_NAME = 'SECTORS' AND COLUMN_NAME = 'SECTOR_PLACEMENT';
DELETE FROM USER_SDO_GEOM_METADATA WHERE TABLE_NAME = 'ROUTERS' AND COLUMN_NAME = 'ROUTER_PLACEMENT';
DELETE FROM USER_SDO_GEOM_METADATA WHERE TABLE_NAME = 'ROUTERS' AND COLUMN_NAME = 'ROUTER_RADIUS';
DELETE FROM USER_SDO_GEOM_METADATA WHERE TABLE_NAME = 'PATHS' AND COLUMN_NAME = 'GEOMETRY';

-- init store map
INSERT INTO USER_SDO_GEOM_METADATA VALUES (
  'regals', 'placement',
  SDO_DIM_ARRAY( -- coordinates X,Y in range 0-1000 with 0.1 point accuracy
      SDO_DIM_ELEMENT('X', 0, 1000, 0.1),
      SDO_DIM_ELEMENT('Y', 0, 1000, 0.1)
  ),
  NULL -- local coordinates system
);

-- init sector map
INSERT INTO USER_SDO_GEOM_METADATA VALUES(
  'sectors', 'sector_placement',
  SDO_DIM_ARRAY( -- coordinatex X,Y in range 0-100 with 0.1 point accuracy
    SDO_DIM_ELEMENT('X',0,100,0.1),
    SDO_DIM_ELEMENT('Y',0,100,0.1)
  ),
  NULL -- local coordinates system
);

-- init router map
INSERT INTO USER_SDO_GEOM_METADATA VALUES(
  'routers', 'router_placement',
  SDO_DIM_ARRAY( -- coordinatex X,Y in range 0-1000 with 0.1 point accuracy
      SDO_DIM_ELEMENT('X',0,1000,0.1),
      SDO_DIM_ELEMENT('Y',0,1000,0.1)
  ),
  NULL -- local coordinates system
);

-- init router range map
INSERT INTO USER_SDO_GEOM_METADATA VALUES(
  'routers', 'router_radius',
  SDO_DIM_ARRAY( -- coordinatex X,Y in range 0-1000 with 0.1 point accuracy
      SDO_DIM_ELEMENT('X',0,1000,0.1),
      SDO_DIM_ELEMENT('Y',0,1000,0.1)
  ),
  NULL -- local coordinates system
);

-- init paths map
INSERT INTO USER_SDO_GEOM_METADATA VALUES(
  'paths', 'geometry',
  SDO_DIM_ARRAY( -- coordinatex X,Y in range 0-1000 with 0.1 point accuracy
      SDO_DIM_ELEMENT('X',0,1000,0.1),
      SDO_DIM_ELEMENT('Y',0,1000,0.1)
  ),
  NULL -- local coordinates system
);

-- CREATE INDEXES
CREATE INDEX regal_placement_idx ON regals(placement) INDEXTYPE IS MDSYS.SPATIAL_INDEX;
CREATE INDEX sector_placement_idx ON sectors(sector_placement) INDEXTYPE IS MDSYS.SPATIAL_INDEX;
CREATE INDEX router_placement_idx ON routers(router_placement) INDEXTYPE IS MDSYS.SPATIAL_INDEX;
CREATE INDEX router_radius_idx ON routers(router_radius) INDEXTYPE IS MDSYS.SPATIAL_INDEX;

-- ------ --
-- PROCEDURES --
DROP PROCEDURE ROTATE_IMAGE;

CREATE OR REPLACE PROCEDURE ROTATE_IMAGE
  (pict_id IN NUMBER)
IS
  img ORDSYS.ORDImage;
  BEGIN
    SELECT PICTURE INTO img FROM PICTURES
    WHERE PICTURE_ID = pict_id FOR UPDATE;

    img.process('rotate=90');

    UPDATE PICTURES SET PICTURE = img WHERE PICTURE_ID = pict_id;

    COMMIT;
  END;


COMMIT;
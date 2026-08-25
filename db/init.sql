-- collab 전용 데이터베이스를 한 번만 만든다.
-- 서버에 이미 있는 다른 데이터베이스(biddy_* 등)와 섞이지 않게 분리한다.
--
--   psql -h 1.234.196.160 -p 15432 -U postgres -f db/init.sql
--
-- PostgreSQL은 CREATE DATABASE에 IF NOT EXISTS가 없다.
-- 이미 있으면 "already exists" 오류가 나는데, 그대로 두면 된다.
CREATE DATABASE collab;

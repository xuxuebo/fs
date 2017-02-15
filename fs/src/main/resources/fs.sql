CREATE TABLE "public"."t_fs_file" (
"id" varchar(32) PRIMARY KEY,
"corp_code" varchar(50)  NOT NULL,
"create_by" varchar(32) ,
"create_time" timestamp(6) NOT NULL,
"update_by" varchar(32),
"update_time" timestamp(6) NOT NULL,
"app_code" varchar(50) NOT NULL,
"business_code" varchar(50),
"business_dir" varchar(50),
"business_id" varchar(50) NOT NULL,
"durations" varchar(500),
"file_size" int8 NOT NULL,
"processor" varchar(10) NOT NULL,
"server_code" varchar(20) NOT NULL,
"status" varchar(20) NOT NULL,
"stored_file_name" varchar(200) NOT NULL,
"sub_file_count" int4,
"sub_file_counts" varchar(100),
"suffix" varchar(10) NOT NULL,
"video_levels" varchar(50),
"process_msg" varchar(500)
)
WITH (OIDS=FALSE);

CREATE TABLE "public"."t_fs_server" (
"id" varchar(32) PRIMARY KEY,
"corp_code" varchar(50) NOT NULL,
"create_by" varchar(32),
"create_time" timestamp(6) NOT NULL,
"update_by" varchar(32),
"update_time" timestamp(6) NOT NULL,
"download" bool,
"host" varchar(50) NOT NULL,
"secret" varchar(200),
"server_code" varchar(20) NOT NULL,
"server_name" varchar(100)NOT NULL,
"sign_level" varchar(10) NOT NULL,
"upload" bool,
"vbox" bool
)
WITH (OIDS=FALSE);

--下面两句sql要根据实际情况填写字段
INSERT INTO "public"."t_fs_server" ("id", "corp_code", "create_by", "create_time", "update_by", "update_time", "download", "host", "secret", "server_code", "server_name", "sign_level", "upload", "vbox") VALUES ('402881d6583dd57a01583dd57dd60000', 'default', NULL, now(), NULL, now(), 't', '文件服务器的域名或者地址', '文件服务器的秘钥', '0000', 'qgutech', '文件服务器的校验级别', 't', 'f');
INSERT INTO "public"."t_fs_server" ("id", "corp_code", "create_by", "create_time", "update_by", "update_time", "download", "host", "secret", "server_code", "server_name", "sign_level", "upload", "vbox") VALUES ('402881d6583dd57a01583dd57dd60001', 'default', NULL, now(), NULL, now(), 'f', '文件转化服务器的域名或者地址', '文件转化服务器的秘钥', '0000', 'qgutech-win', '文件转化服务器的校验级别', 'f', 'f');


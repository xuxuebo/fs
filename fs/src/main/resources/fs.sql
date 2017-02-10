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

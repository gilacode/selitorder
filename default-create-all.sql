create table api_client (
  id                            bigint auto_increment not null,
  created_by                    varchar(255) not null,
  client_id                     varchar(255) not null,
  client_secret                 varchar(255) not null,
  admin_email                   varchar(255) not null,
  status                        varchar(7) not null,
  created_at                    datetime(6) not null,
  constraint ck_api_client_status check ( status in ('ACTIVE','SUSPEND')),
  constraint uq_api_client_client_id unique (client_id),
  constraint pk_api_client primary key (id)
);

create table audit_txn (
  id                            bigint auto_increment not null,
  created_by                    varchar(255) not null,
  audit_mod                     varchar(255) not null,
  audit_action                  varchar(255) not null,
  audit_ref_key                 varchar(255) not null,
  audit_log                     longtext,
  created_at                    datetime(6) not null,
  constraint pk_audit_txn primary key (id)
);

create table configuration (
  id                            bigint auto_increment not null,
  property_key                  varchar(255),
  property_desc                 varchar(255),
  string_value                  varchar(255),
  additional_properties         longtext,
  parent_key                    varchar(255),
  deletable                     tinyint(1) default 0 not null,
  created_at                    datetime(6) not null,
  version                       bigint not null,
  constraint uq_configuration_property_key unique (property_key),
  constraint pk_configuration primary key (id)
);

create table log_trx (
  id                            bigint auto_increment not null,
  log_type                      varchar(7) not null,
  log_group                     varchar(255) not null,
  reference_key                 varchar(255) not null,
  message_key                   longtext,
  created_at                    datetime(6) not null,
  version                       bigint not null,
  constraint ck_log_trx_log_type check ( log_type in ('ERROR','WARNING','INFO','DEBUG')),
  constraint pk_log_trx primary key (id)
);

create table email_trx (
  id                            bigint auto_increment not null,
  from_addr                     varchar(255) not null,
  to_addrs                      varchar(255) not null,
  email_subject                 varchar(255) not null,
  body_text                     longtext not null,
  body_html                     longtext,
  email_group                   varchar(255) not null,
  reference_key                 varchar(255) not null,
  cids                          varchar(255),
  status                        varchar(9) not null,
  created_at                    datetime(6) not null,
  constraint ck_email_trx_status check ( status in ('NEW','SCHEDULED','QUEUE','DELIVERED')),
  constraint uq_email_trx_reference_key unique (reference_key),
  constraint pk_email_trx primary key (id)
);

create table file_index (
  id                            bigint auto_increment not null,
  folder_name                   varchar(255) not null,
  reference_key                 varchar(255) not null,
  file_path                     varchar(255) not null,
  file_url                      varchar(255) not null,
  mime_type                     varchar(255) not null,
  file_ext                      varchar(255) not null,
  created_at                    datetime(6) not null,
  constraint uq_file_index_folder_name_reference_key unique (folder_name,reference_key),
  constraint pk_file_index primary key (id)
);

create table job (
  id                            bigint auto_increment not null,
  job_name                      varchar(255) not null,
  job_desc                      varchar(255),
  group_name                    varchar(255) not null,
  group_desc                    varchar(255),
  job_interval                  varchar(13) not null,
  triggered_at                  varchar(255),
  last_executed                 datetime(6),
  created_at                    datetime(6) not null,
  version                       bigint not null,
  constraint ck_job_job_interval check ( job_interval in ('EVERY_15_SEC','EVERY_1_MIN','EVERY_15_MIN','EVERY_1_HOUR','EVERY_6_HOUR','EVERY_12_HOUR','DAILY')),
  constraint uq_job_job_name unique (job_name),
  constraint pk_job primary key (id)
);

create table job_trx (
  id                            bigint auto_increment not null,
  batch_id                      varchar(255),
  job_id                        bigint,
  success                       integer,
  cancelled                     integer,
  processed                     integer,
  elapsed                       bigint,
  created_at                    datetime(6) not null,
  version                       bigint not null,
  constraint uq_job_trx_batch_id unique (batch_id),
  constraint pk_job_trx primary key (id)
);

create table nav_head (
  id                            bigint auto_increment not null,
  title                         varchar(255) not null,
  icon_class                    varchar(255) not null,
  created_at                    datetime(6) not null,
  constraint uq_nav_head_title unique (title),
  constraint pk_nav_head primary key (id)
);

create table nav_map (
  id                            bigint auto_increment not null,
  permission_id                 bigint not null,
  reference_key                 varchar(255) not null,
  created_at                    datetime(6) not null,
  constraint uq_nav_map_permission_id_reference_key unique (permission_id,reference_key),
  constraint pk_nav_map primary key (id)
);

create table nav_menu (
  id                            bigint auto_increment not null,
  code                          varchar(255) not null,
  title                         varchar(255) not null,
  full_path                     varchar(255) not null,
  icon_class                    varchar(255) not null,
  menu_location                 varchar(12),
  header_id                     bigint not null,
  menu_status                   varchar(8),
  sequence                      integer not null,
  created_at                    datetime(6) not null,
  constraint ck_nav_menu_menu_location check ( menu_location in ('PROFILE','LEFT_SIDEBAR')),
  constraint ck_nav_menu_menu_status check ( menu_status in ('ACTIVE','DISABLED')),
  constraint uq_nav_menu_code unique (code),
  constraint pk_nav_menu primary key (id)
);

create table nav_perm (
  id                            bigint auto_increment not null,
  code                          varchar(255) not null,
  title                         varchar(255) not null,
  user_type                     varchar(11) not null,
  menu_id                       bigint not null,
  auto_register                 tinyint(1) default 0 not null,
  created_at                    datetime(6) not null,
  constraint ck_nav_perm_user_type check ( user_type in ('SUPER_ADMIN','USER','API_CLIENT','API_USER')),
  constraint uq_nav_perm_code unique (code),
  constraint pk_nav_perm primary key (id)
);

create table nav_tmpl (
  id                            bigint auto_increment not null,
  permission_id                 bigint not null,
  template_name                 varchar(255) not null,
  created_at                    datetime(6) not null,
  constraint uq_nav_tmpl_permission_id_template_name unique (permission_id,template_name),
  constraint pk_nav_tmpl primary key (id)
);

create table notif_txn (
  id                            varchar(40) not null,
  created_by                    varchar(255) not null,
  category                      varchar(10) not null,
  channel_desc                  varchar(255) not null,
  recipient_id                  varchar(255) not null,
  recipient_username            varchar(255) not null,
  topic_subject                 varchar(255),
  notif_message                 longtext,
  delivery_status               varchar(7) not null,
  redirect_url                  varchar(255),
  already_read                  tinyint(1) default 0 not null,
  created_at                    datetime(6) not null,
  constraint ck_notif_txn_category check ( category in ('SMS','EMAIL','MOBILE_APP','WEB')),
  constraint ck_notif_txn_delivery_status check ( delivery_status in ('QUEUE','SENT','UNKNOWN')),
  constraint pk_notif_txn primary key (id)
);

create table sec_user (
  username                      varchar(255) not null,
  user_category                 varchar(255),
  display_name                  varchar(255) not null,
  email                         varchar(255) not null,
  password                      varchar(255) not null,
  require_change_pwd            tinyint(1) default 0 not null,
  photo_url                     varchar(255),
  mobile_no                     varchar(255),
  remember_me_key               varchar(255),
  user_type                     varchar(11) not null,
  last_login                    datetime(6),
  user_status                   varchar(6),
  created_at                    datetime(6) not null,
  version                       bigint not null,
  constraint ck_sec_user_user_type check ( user_type in ('SUPER_ADMIN','USER','API_CLIENT','API_USER')),
  constraint ck_sec_user_user_status check ( user_status in ('NEW','ACTIVE','LOCKED')),
  constraint uq_sec_user_email unique (email),
  constraint uq_sec_user_remember_me_key unique (remember_me_key),
  constraint pk_sec_user primary key (username)
);

create table uq_code (
  id                            bigint auto_increment not null,
  uq_code                       varchar(255) not null,
  reference_key                 varchar(255),
  constraint uq_uq_code_uq_code unique (uq_code),
  constraint pk_uq_code primary key (id)
);

create index ix_audit_txn_created_by on audit_txn (created_by);
create index ix_audit_txn_audit_mod on audit_txn (audit_mod);
create index ix_audit_txn_audit_action on audit_txn (audit_action);
create index ix_audit_txn_audit_ref_key on audit_txn (audit_ref_key);
create index ix_configuration_parent_key on configuration (parent_key);
create index ix_email_trx_email_group on email_trx (email_group);
create index ix_notif_txn_created_by on notif_txn (created_by);
create index ix_notif_txn_recipient_username on notif_txn (recipient_username);
create index ix_sec_user_user_category on sec_user (user_category);
create index ix_job_trx_job_id on job_trx (job_id);
alter table job_trx add constraint fk_job_trx_job_id foreign key (job_id) references job (id) on delete restrict on update restrict;

create index ix_nav_map_permission_id on nav_map (permission_id);
alter table nav_map add constraint fk_nav_map_permission_id foreign key (permission_id) references nav_perm (id) on delete restrict on update restrict;

create index ix_nav_menu_header_id on nav_menu (header_id);
alter table nav_menu add constraint fk_nav_menu_header_id foreign key (header_id) references nav_head (id) on delete restrict on update restrict;

create index ix_nav_perm_menu_id on nav_perm (menu_id);
alter table nav_perm add constraint fk_nav_perm_menu_id foreign key (menu_id) references nav_menu (id) on delete restrict on update restrict;

create index ix_nav_tmpl_permission_id on nav_tmpl (permission_id);
alter table nav_tmpl add constraint fk_nav_tmpl_permission_id foreign key (permission_id) references nav_perm (id) on delete restrict on update restrict;


alter table job_trx drop foreign key fk_job_trx_job_id;
drop index ix_job_trx_job_id on job_trx;

alter table nav_map drop foreign key fk_nav_map_permission_id;
drop index ix_nav_map_permission_id on nav_map;

alter table nav_menu drop foreign key fk_nav_menu_header_id;
drop index ix_nav_menu_header_id on nav_menu;

alter table nav_perm drop foreign key fk_nav_perm_menu_id;
drop index ix_nav_perm_menu_id on nav_perm;

alter table nav_tmpl drop foreign key fk_nav_tmpl_permission_id;
drop index ix_nav_tmpl_permission_id on nav_tmpl;

drop table if exists api_client;

drop table if exists audit_txn;

drop table if exists configuration;

drop table if exists log_trx;

drop table if exists email_trx;

drop table if exists file_index;

drop table if exists job;

drop table if exists job_trx;

drop table if exists nav_head;

drop table if exists nav_map;

drop table if exists nav_menu;

drop table if exists nav_perm;

drop table if exists nav_tmpl;

drop table if exists notif_txn;

drop table if exists sec_user;

drop table if exists uq_code;

drop index ix_audit_txn_created_by on audit_txn;
drop index ix_audit_txn_audit_mod on audit_txn;
drop index ix_audit_txn_audit_action on audit_txn;
drop index ix_audit_txn_audit_ref_key on audit_txn;
drop index ix_configuration_parent_key on configuration;
drop index ix_email_trx_email_group on email_trx;
drop index ix_notif_txn_created_by on notif_txn;
drop index ix_notif_txn_recipient_username on notif_txn;
drop index ix_sec_user_user_category on sec_user;

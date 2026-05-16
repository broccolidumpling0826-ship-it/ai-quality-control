-- 确保 admin 账号具备 ADMIN 角色（可重复执行）
USE ai_quality_control;

UPDATE sys_user
SET role = 'ADMIN', status = 1, username = '系统管理员'
WHERE user_no = 'admin';

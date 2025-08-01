-- 为todos表添加start_time字段
ALTER TABLE todos ADD COLUMN start_time TIMESTAMP NULL COMMENT '开始时间' AFTER assignee_id;

-- 为现有数据添加索引
CREATE INDEX idx_start_time ON todos(start_time);
CREATE INDEX idx_due_date ON todos(due_date);
CREATE INDEX idx_date_range ON todos(start_time, due_date); 
-- 初始用户：HR 与求职者
INSERT INTO sys_user (username, password, real_name, role, phone, email, company, position, skills, experience, education) VALUES
('hr_zhang', '123456', '张建国', 'HR', '13800001111', 'zhang@huawetech.com', '华纬科技有限公司', '招聘经理', 'Java,HRBP', '8年', '本科'),
('hr_li',   '123456', '李婷',   'HR', '13800002222', 'li@ruixingsoft.com', '瑞兴软件股份', 'HRBP',     '招聘,校招', '5年', '硕士'),
('hr_wang', '123456', '王强',   'HR', '13800003333', 'wang@tianhecloud.com', '天河云科技', '技术招聘', 'Java,大数据', '6年', '本科'),
('zhangsan','123456', '张三',   'SEEKER', '13900001111', 'zhangsan@qq.com', '', '', 'Java,Vue,MySQL,SpringBoot', '1年', '本科'),
('lisi',    '123456', '李四',   'SEEKER', '13900002222', 'lisi@qq.com',     '', '', 'Python,数据分析,SQL',      '应届', '硕士');

-- 初始岗位
INSERT INTO job (title, company, city, salary_min, salary_max, experience, education, tags, description, hr_id, hr_name) VALUES
('Java开发工程师', '华纬科技有限公司', '武汉', 15, 25, '3-5年', '本科', 'Java,SpringBoot,MySQL,微服务',
 '负责公司核心业务系统后端开发，参与微服务架构设计与重构，要求熟悉Java生态、Spring全家桶，有高并发经验优先。', 1, '张建国'),
('前端开发工程师(Vue)', '瑞兴软件股份', '武汉', 12, 20, '1-3年', '本科', 'Vue3,ElementPlus,JavaScript',
 '负责公司SaaS产品前端开发，与产品、UI协作完成页面实现，要求熟悉Vue3全家桶、组件化开发、工程化工具。', 2, '李婷'),
('大数据开发工程师', '天河云科技', '武汉', 20, 35, '3-5年', '本科', 'Hadoop,Spark,Flink,SQL',
 '负责数据平台与实时计算链路开发，参与数据仓库建模与任务调度，要求熟悉Hadoop生态、Spark/Flink。', 3, '王强'),
('Java实习生', '华纬科技有限公司', '武汉', 3, 5, '应届', '本科', 'Java,SSM,MySQL',
 '面向2026届毕业生，提供导师一对一带教，参与真实业务模块开发，表现优异者转正。', 1, '张建国'),
('UI/UX设计师', '瑞兴软件股份', '武汉', 10, 18, '1-3年', '本科', 'Figma,Sketch,交互设计',
 '负责B端产品视觉与交互设计，输出高保真原型与设计规范，要求有C端或B端设计经验。', 2, '李婷'),
('测试开发工程师', '天河云科技', '武汉', 12, 22, '1-3年', '本科', 'Python,自动化测试,Jmeter',
 '负责自动化测试框架搭建与接口测试，参与CI/CD流程，要求熟悉Python及常用测试工具。', 3, '王强');

-- 初始消息
INSERT INTO message (session_id, from_user, to_user, content) VALUES
('1_4', 1, 4, '你好张三，看到你投递了Java开发岗位，方便聊聊吗？'),
('1_4', 4, 1, '您好张经理，我对这个岗位很感兴趣，随时可以面试。'),
('2_5', 2, 5, '你好李四，我们正在招数据分析方向的同学，有兴趣了解下吗？');

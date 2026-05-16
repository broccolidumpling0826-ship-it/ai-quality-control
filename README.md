初次启动：
1. 建立 SSH 隧道 ssh -L 3307:127.0.0.1:3306 -L 6380:127.0.0.1:6379 -N -f ubuntu@118.25.58.101
2. 一键初始化：bash scripts/setup-dev.sh
3. 启动后端：cd backend && mvn spring-boot:run -Dspring.profiles.active=dev
4. 启动前端（新终端）：cd frontend && npm install && npm run dev                                                      
   访问: http://localhost:5173  |  API文档: http://localhost:8080/doc.html
   默认账号: admin / Admin123456

日常开发流程（第一次之后）：                                                                                                             
终端1：建隧道（如果还没建）
ssh -L 3307:127.0.0.1:3306 -L 6380:127.0.0.1:6379 -N -f ubuntu@118.25.58.101
终端2：启动后端
cd backend && mvn spring-boot:run -Dspring.profiles.active=dev
终端3：启动前端
cd frontend && npm run dev
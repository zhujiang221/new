# 用户主页统计API接口说明

## 需要创建的后端接口

### 1. 获取主页统计数据
**接口路径**: `/user/home/statistics`  
**请求方式**: GET  
**需要权限**: 用户登录

**响应格式**:
```json
{
  "pendingAppointments": 3,  // 待就诊预约数
  "petLogs": 28,              // 宠物日志数
  "unreadMessages": 5         // 未读消息数
}
```

**实现说明**:
- `pendingAppointments`: 查询当前用户状态为"待就诊"的预约数量（status=1或2）
- `petLogs`: 查询当前用户的所有宠物日志总数
- `unreadMessages`: 查询当前用户的未读通知消息数（isRead=0）

---

### 2. 获取近期预约列表
**接口路径**: `/user/home/recentAppointments`  
**请求方式**: GET  
**需要权限**: 用户登录

**请求参数**:
- `limit` (可选): 返回数量限制，默认3

**响应格式**:
```json
[
  {
    "id": 1,
    "appointmentTypeName": "常规检查",
    "doctorName": "李医生",
    "appDate": "2026-01-20",
    "timeSlot": "10:00",
    "status": 1
  },
  {
    "id": 2,
    "appointmentTypeName": "疫苗接种",
    "doctorName": "王医生",
    "appDate": "2026-01-22",
    "timeSlot": "14:30",
    "status": 1
  }
]
```

**实现说明**:
- 查询当前用户最近的预约记录，按预约日期排序
- 只返回未来或最近的预约（可以限制为未来30天内）
- 需要关联查询预约类型表和医生表获取名称

---

### 3. 获取健康趋势数据
**接口路径**: `/user/home/healthTrends`  
**请求方式**: GET  
**需要权限**: 用户登录

**响应格式**:
```json
{
  "weightStatus": "normal",        // normal/warning/danger
  "activityStatus": "excellent",   // excellent/good/normal/poor
  "dietStatus": "normal",          // excellent/good/normal/poor
  "weightProgress": 75,            // 0-100
  "activityProgress": 90,          // 0-100
  "dietProgress": 60               // 0-100
}
```

**实现说明**:
- `weightStatus`: 根据最近30天的体重数据计算，与健康标准对比
- `activityStatus`: 根据最近30天的活动量数据计算
- `dietStatus`: 根据最近30天的饮食规律数据计算
- `weightProgress`: 体重监测进度百分比
- `activityProgress`: 活动量进度百分比
- `dietProgress`: 饮食规律进度百分比

**计算逻辑**:
1. 从宠物日志表(pet_daily)获取最近30天的数据
2. 与健康标准表(standard)对比
3. 根据对比结果计算状态和进度

---

## 建议的实现位置

在 `UserController.java` 中添加以下方法：

```java
@GetMapping("/home/statistics")
public ResultMap getHomeStatistics() {
    // 实现统计数据获取逻辑
}

@GetMapping("/home/recentAppointments")
public ResultMap getRecentAppointments(@RequestParam(defaultValue = "3") Integer limit) {
    // 实现近期预约获取逻辑
}

@GetMapping("/home/healthTrends")
public ResultMap getHealthTrends() {
    // 实现健康趋势获取逻辑
}
```

## 数据库查询示例

### 待就诊预约数
```sql
SELECT COUNT(*) FROM apply 
WHERE userId = ? AND status IN (1, 2)
```

### 宠物日志数
```sql
SELECT COUNT(*) FROM pet_daily pd
INNER JOIN pet p ON pd.petId = p.id
WHERE p.userId = ?
```

### 未读消息数
```sql
SELECT COUNT(*) FROM notification_message
WHERE receiverId = ? AND isRead = 0
```

### 近期预约
```sql
SELECT a.id, at.name as appointmentTypeName, u.name as doctorName, 
       a.appDate, a.timeSlot, a.status
FROM apply a
INNER JOIN appointment_type at ON a.appointmentTypeId = at.id
INNER JOIN user u ON a.doctorId = u.id
WHERE a.userId = ? AND a.appDate >= CURDATE()
ORDER BY a.appDate ASC, a.timeSlot ASC
LIMIT ?
```

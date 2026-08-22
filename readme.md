# Learning Management System

## Overview
<hr style="border: 0.1px solid white;">

    Design a learning management system, where instructor will create course and user can learn from those courses

## Requirements
<hr style="border: 0.1px solid white;">

- ### Functional
    1. Courses will have modules and each module will have lessons
    2. An instructor will be managing the course, he can create, update the courses
    3. Student/User will be enrolling the course, and he should be able to track his progress
    4. LMS portal should have capabilities to sign-up/log-in & role based access control over resources and action

## Entities
<hr style="border: 0.1px solid white;">

- User
- Course
- Module
- Lesson
- Enrollment
- Progress

## Database Design
<hr style="border: 0.1px solid white;">

<details>
    <summary>
        User Table
    </summary>

| Column         | Type          | Default  | Nullable  | Constraint |  
|:---------------|:--------------|:---------|:----------|:-----------|
| id             | BIGINT        | -        | No        | PK         |
| name           | VARCHAR(100)  | null     | No        | -          |
| dob            | DATE          | null     | No        | -          |
| mobile_no      | VARCHAR(10)   | null     | No        | UK1        |
| email_id       | VARCHAR(100)  | null     | No        | UK2        |
| password_hash  | VARCHAR(100)  | null     | No        | -          |
| role           | ENUM          | null     | No        | -          |
| created_at     | TIMESTAMP     | -        | No        | -          |

</details>

<details>
    <summary>
        Course Table
    </summary>

| Column         | Type          | Default  | Nullable  | Constraint  |
|:---------------|:--------------|:---------|:----------|:------------|
| id             | BIGINT        | -        | No        | PK          |
| title          | VARCHAR(100)  | null     | No        | -           |
| description    | VARCHAR(200)  | null     | No        | -           |
| instructor_id  | BIGINT        | -        | Yes       | FK          |
| tags           | JSONB         | []       | No        | -           |
| is_active      | Boolean       | true     | No        | -           |
| created_at     | TIMESTAMP     | -        | No        | -           |

</details>

<details>
    <summary>
        Module Table
    </summary>

| Column      | Type          | Default  | Nullable  | Constraint  |
|:------------|:--------------|:---------|:----------|:------------|
| id          | BIGINT        | -        | No        | PK          |
| course_id   | BIGINT        | -        | No        | FK          |
| title       | VARCHAR(50)   | -        | No        | -           |
| description | VARCHAR(100)  | -        | Yes       | FK          |
| sequence    | INT           | null     | Yes       | -           |
| is_active   | Boolean       | true     | No        | -           |
| created_at  | TIMESTAMP     | -        | No        | -           |

</details>

<details>
    <summary>
        Lessons Table
    </summary>

| Column       | Type          | Default  | Nullable  | Constraint  |
|:-------------|:--------------|:---------|:----------|:------------|
| id           | BIGINT        | -        | No        | PK          |
| module_id    | BIGINT        | -        | No        | FK          |
| content_type | ENUM          | -        | No        | -           |
| content_link | VARCHAR(100)  | -        | No        | -           |
| sequence     | INT           | null     | Yes       | -           |
| is_active    | Boolean       | true     | No        | -           |
| created_at   | TIMESTAMP     | -        | No        | -           |

</details>

<details>
    <summary>
        Enrollments Table
    </summary>

| Column        | Type          | Default  | Nullable  | Constraint  |
|:--------------|:--------------|:---------|:----------|:------------|
| id            | BIGINT        | -        | No        | PK          |
| user_id       | BIGINT        | -        | No        | FK          |
| course_id     | BIGINT        | -        | No        | FK          |
| course_completion_status | ENUM          | -        | No        | -           |
| enrolled_at   | TIMESTAMP     | -        | No        | -           |

</details>

<details>
    <summary>
        Progress Table
    </summary>

| Column        | Type          | Default  | Nullable  | Constraint  |
|:--------------|:--------------|:---------|:----------|:------------|
| id            | BIGINT        | -        | No        | PK          |
| user_id       | BIGINT        | -        | No        | FK          |
| lesson_id     | BIGINT        | -        | No        | FK          |
| lesson_status | ENUM          | -        | No        | -           |
| started_at    | TIMESTAMP     | -        | No        | -           |
| completed_at  | TIMESTAMP     | -        | No        | -           |

</details>

## Enumerations
<hr style="border: 0.1px solid white;">

<details>
    <summary>
        User Role Enum
    </summary>

| Value         | Description            |  
|:--------------|:-----------------------|
| ADMIN         | admin role             |
| INSTRUCTOR    | course instructor role |
| USER          | user role              |

</details>

<details>
    <summary>
        Course Status Enum
    </summary>

| Value      | Description                   |  
|:-----------|:------------------------------|
| COMPLETE   | course is completed by user   |
| INCOMPLETE | course is incomplete for user |

</details>

<details>
    <summary>
        Lesson Status Enum
    </summary>

| Value     | Description                   |  
|:----------|:------------------------------|
| UNSTARTED | lesson is not started by user |
| STARTED   | lesson is started by user     |
| FINISHED  | lesson is finished by user    |

</details>

<details>
    <summary>
        Content type Enum
    </summary>

| Value | Description                |  
|:------|:---------------------------|
| MP3   | content is audio only      |
| MP4   | content is audio and video |
| PDF   | content is in pdf format   |
| TEXT  | content is in text format  |

</details>

## API Design
<hr style="border: 0.1px solid white;">

## Courses

---

<details>
  <summary>
    <b>Create Course</b>
  </summary>

#### End Point
```text 
[POST] /courses
```

#### Request Body
```jsonc
{
    "courseTitle": "",
    "courseDescription": "",
    "courseTags": [],         // optional
    "instructorId": 1
}
```

#### Response Body
```json
{
  "message" : "",
  "data" : {
    "courseId": 1,
    "courseTitle": "",
    "curseDescription": "",
    "courseTags": [],
    "instructorId": 1,
    "instructorName": "",
    "isActive": true,
    "createdAt": ""
  }
}
```

</details>

---

<details>
  <summary>
    <b>Update Course</b>
  </summary>

#### End Point
```text 
[PUT] /courses/{courseId}
```

#### Request Body
```jsonc
{
    "courseTitle": "",       // optional
    "courseDescription": "", // optional
    "courseTags": [],        // optional
    "isActive": false        // optiona;
}
```

#### Response Body
```json
{
  "message": "",
  "data" : {
    "courseId": 1,
    "courseTitle": "",
    "curseDescription": "",
    "courseTags": [],
    "instructorId": 1,
    "instructorName": "",
    "isActive": false,
    "createdAt": ""
  }
}
```

</details>

---

<details>
  <summary>
    <b>Get Course By courseId</b>
  </summary>

#### End Point
```text 
[GET] /courses/{courseId}
```     

#### Query Param
| Param          | Type    | Required | Default | Description                          |
|----------------|---------|----------|---------|--------------------------------------|
| includeModules | boolean | false    | false   | used to get modules for given course |

#### Response Body
```json
{
  "message": "",
  "data": {
    "courseId": 1,
    "courseTitle": "",
    "curseDescription": "",
    "courseTags": [],
    "instructorId": 1,
    "instructorName": "",
    "modules": [],
    "isActive": false,
    "createdAt": ""
  }
}
```

</details>

---

<details>
  <summary>
    <b>List Courses</b>
  </summary>

#### End Point
```text 
[GET] /courses
```

#### Query Param
| Param    | Type    | Required | Default | Description                                 |
|----------|---------|----------|---------|---------------------------------------------|
| active   | boolean | false    | false   | used to get in-active and active course     | 
| pageNo   | int     | true     | 1       | used to indicate page number for pagination |
| pageSize | int     | true     | 10      | used to indicate page size for pagination   |


#### Response Body
```json
{
  "message": "fetched list of courses",
  "data": [
    {
      "courseId": 1,
      "courseTitle": "",
      "curseDescription": "",
      "courseTags": [],
      "instructorId": 1,
      "instructorName": "",
      "isActive": false,
      "createdAt": ""
    }
  ],
  "page": 1,
  "size": 10,
  "total": 100
}
```

</details>

---

<details>
  <summary>
    <b>Delete Course By courseId</b>
  </summary>

#### End Point
```text 
[DELETE] /courses/{courseId}
```

#### Response Body
```json
{
  "message" : "Course with id 1 in-activated successfully",
  "data" : {}
}
```

</details>

<hr style="border: 0.1px solid grey;">

## Modules

---

<details>
  <summary>
    <b>Create Module</b>
  </summary>

#### End Point
```text 
[POST] /modules
```

#### Request Body
```jsonc
{
    "courseId": 1,
    "moduleTitle": "",
    "moduleDescription": "",
    "sequence": 1
}
```

#### Response Body
```json
{
  "message" : "",
  "data" : {
    "moduleId": 1,
    "courseId": 1,
    "moduleTitle": "",
    "moduleDescription": "",
    "sequence": 1,
    "isActive": true,
    "createdAt": ""
  }
}
```

</details>

---

<details>
  <summary>
    <b>Update Module</b>
  </summary>

#### End Point
```text 
[PUT] /modules/{moduleId}
```

#### Request Body
```jsonc
{   
    "moduleTitle": "".
    "moduleDescription": "",
    "sequence": 2.
    "isActive": false
}
```

#### Response Body
```json
{
  "message": "",
  "data" : {
    "moduleId": 1,
    "courseId": 1,
    "moduleTitle": "",
    "moduleDescription": "",
    "sequence": 2,
    "isActive": true,
    "createdAt": ""
  }
}
```

</details>

---

<details>
  <summary>
    <b>Get Module By moduleId</b>
  </summary>

#### End Point
```text 
[GET] /modules/{moduleId}
```

#### Query Param
| Param          | Type    | Required | Default | Description                                 |
|----------------|---------|----------|---------|---------------------------------------------|
| includeLessons | boolean | false    | false   | used to get lessons for given module        |

#### Response Body
```json
{
  "message": "",
  "data": {
    "moduleId": 1,
    "courseId": 1,
    "moduleTitle": "",
    "moduleDescription": "",
    "sequence": 1,
    "lessons": [],
    "isActive": true,
    "createdAt": ""
  }
}
```

</details>

---

<details>
  <summary>
    <b>List Modules</b>
  </summary>

#### End Point
```text 
[GET] /modules
```

#### Query Param
| Param    | Type     | Required | Default | Description                                  |
|----------|----------|----------|---------|----------------------------------------------|
| active   | boolean  | false    | true    | used to get in-active and active course      | 
| pageNo   | int      | true     | 1       | used to indicate page number for pagination  |
| pageSize | int      | true     | 10      | used to indicate page size for pagination    |
| courseId | int      | true     | null    | course for which modules are fetched         |

#### Response Body
```json
{
  "message": "fetched list of courses",
  "data": [
    {
      "moduleId": 1,
      "courseId": 1,
      "moduleTitle": "",
      "moduleDescription": "",
      "sequence": 1,
      "isActive": true,
      "createdAt": ""
    }
  ],
  "page": 1,
  "size": 10,
  "total": 100
}
```

</details>

---

<details>
  <summary>
    <b>Delete Module By moduleId</b>
  </summary>

#### End Point
```text 
[DELETE] /modules/{moduleId}
```

#### Response Body
```json
{
  "message" : "Module with id 1 in-activated successfully",
  "data" : {}
}
```

</details>

<hr style="border: 0.1px solid grey;">

## Lessons

---

<details>
  <summary>
    <b>Create Lessons</b>
  </summary>

#### End Point
```text 
[POST] /lessons
```

#### Request Body
```jsonc
{
    "moduleId": 1,
    "contentType": "",
    "contentLink": "",
    "sequence": 1
}
```

#### Response Body
```json
{
  "message" : "",
  "data" : {
    "lessonId": 1,
    "moduleId": 1,
    "contentType": "",
    "contentLink": "",
    "sequence": 1,
    "isActive": true,
    "createdAt": ""
  }
}
```

</details>

---

<details>
  <summary>
    <b>Update Lesson</b>
  </summary>

#### End Point
```text 
[PUT] /lessons/{lessonId}
```

#### Request Body
```jsonc
{
    "moduleId": 1,
    "contentType": "",
    "contentLink": "",
    "sequence": 1,
    "isActive": true,
  }
```

#### Response Body
```json
{
  "message": "",
  "data" : {
    "lessonId": 1,
    "moduleId": 1,
    "contentType": "",
    "contentLink": "",
    "sequence": 1,
    "isActive": true,
    "createdAt": ""
  }
}
```

</details>

---

<details>
  <summary>
    <b>Get Lesson By lessonId</b>
  </summary>

#### End Point
```text 
[GET] /lessons/{lessonId}
```

#### Response Body
```json
{
  "message": "",
  "data": {
    "lessonId": 1,
    "moduleId": 1,
    "contentType": "",
    "contentLink": "",
    "sequence": 1,
    "isActive": true,
    "createdAt": ""
  }
}
```

</details>

---

<details>
  <summary>
    <b>List Lessons</b>
  </summary>

#### End Point
```text 
[GET] /lessons
```

#### Query Param
| Param    | Type     | Required | Default| Description                                  |
|----------|----------|----------|--------|----------------------------------------------|
| active   | boolean  | false    | true   | used to get in-active and active course      | 
| pageNo   | int      | true     | 1      | used to indicate page number for pagination  |
| pageSize | int      | true     | 10     | used to indicate page size for pagination    |
| moduleId | int      | true     | null   | indicates module for which lesson is fetched |
| userId   | int      | true     | null   | indicates user for which lesson is fetched   |

#### Response Body
```json
{
  "message": "fetched list of lessons for module",
  "data": [
    {
      "lessonId": 1,
      "moduleId": 1,
      "contentType": "",
      "contentLink": "",
      "sequence": 1,
      "isActive": true,
      "createdAt": ""
    }
  ],
  "page": 1,
  "size": 10,
  "total": 100
}
```

```json
{
  "message": "fetched list of lessons for module and user",
  "data": [
    {
      "lessonId": 1,
      "moduleId": 1,
      "userId": 1,
      "lessonStatus": "UNSTARTED",
      "contentType": "",
      "contentLink": "",
      "sequence": 1,
      "isActive": true,
      "createdAt": ""
    }
  ],
  "page": 1,
  "size": 10,
  "total": 100
}
```

</details>

---

<details>
  <summary>
    <b>Delete Lesson By lessonId</b>
  </summary>

#### End Point
```text 
[DELETE] /lessons/{lessonId}
```

#### Response Body
```json
{
  "message" : "Lesson with id 1 in-activated successfully",
  "data" : {}
}
```

</details>

<hr style="border: 0.1px solid grey;">

## Enrollments

---

<details>
  <summary>
    <b>Create Enrollments</b>
  </summary>

#### End Point
```text 
[POST] /enrollments
```

#### Request Body
```jsonc
{
    "userId": 1,
    "courseId": 1
}
```

#### Response Body
```json
{
  "message" : "Enrolled to course successfully",
  "data" : {
    "id": 1,
    "courseId": 1,
    "userId": 1,
    "courseCompletionStatus": "INCOMPLETE",
    "enrolledAt": ""
  }
}
```

</details>

---

<details>
  <summary>
    <b>List Enrollments</b>
  </summary>

#### End Point
```text 
[GET] /enrollments
```

#### Query Param
| Param    | Type     | Required | Default| Description                                      |
|----------|----------|----------|--------|--------------------------------------------------|
| pageNo   | int      | true     | 1      | used to indicate page number for pagination      |
| pageSize | int      | true     | 10     | used to indicate page size for pagination        |
| userId   | int      | true     | null   | indicates user for which enrollments are fetched |

#### Response Body
```json
{
  "message": "fetched list of enrollment for user",
  "data": [
    {
      "id": 1,
      "courseId": 1,
      "userId": 1,
      "courseTitle": "",
      "courseCompletionStatus": "COMPLETE",
      "enrolledAt": ""
    }
  ],
  "page": 1,
  "size": 10,
  "total": 100
}
```

</details>

<hr style="border: 0.1px solid grey;">

## Progress

---

<details>
  <summary>
    <b>Update Progress</b>
  </summary>

#### End Point
```text 
[PUT] /progress/{progressId}
```

#### Request Body
```jsonc
{
    "lessonId": 1,
    "userId": 1,
    "lessonStatus": "STARTED",
}
```

#### Response Body
```json
{
  "message": "progress updated successfully",
  "data" : {
    "lessonId": 1,
    "userId": 1,
    "lessonStatus": ""
  }
}
```

</details>

<hr style="border: 0.1px solid grey;">


## Other
7. Business logic
8. Data Flow / Sequence
9. Edge Cases
10. Security
11. Performance Considerations
12. Future Enhancements
13. Assumptions







